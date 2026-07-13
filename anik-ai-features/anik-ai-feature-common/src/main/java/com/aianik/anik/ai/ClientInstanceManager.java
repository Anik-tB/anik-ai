package com.aianik.anik.ai;

import cn.hutool.core.collection.CollUtil;
import com.aianik.anik.ai.common.util.JsonUtil;
import com.aianik.anik.ai.persistence.app.mapper.AppMapper;
import com.aianik.anik.ai.persistence.app.mapper.ClientNodeMapper;
import com.aianik.anik.ai.persistence.app.po.AppPO;
import com.aianik.anik.ai.persistence.app.po.ClientNodePO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 * client instance manager
 * <p>
 * Responsibilities:
 * 1. Maintain active instance mapping and gRPC Channel in memory
 * 2. Enqueue heartbeat data and flush disks in batches regularly (LinkedBlockingDeque mode)
 * 3. Pull remote nodes from DB regularly to realize client discovery between clusters
 * 4. DB is downgraded when reading to ensure that cross-node distribution is available
 *
 * @author openanik
 * @date 2025-04-08
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ClientInstanceManager {

    // ==================== Constants ====================

    /** Example source */
    public enum InstanceSource {
        /** Direct heartbeat to this server*/
        LOCAL,
        /** Load from DB (heartbeat to other server nodes) */
        REMOTE
    }

    /** Expiration threshold (milliseconds). Instances that have not received heartbeats beyond this time are considered expired */
    private static final long EXPIRE_THRESHOLD_MS = 40_000;

    /** DB row expiration offset (seconds), aligned with EXPIRE_THRESHOLD_MS */
    private static final int EXPIRE_OFFSET_SECONDS = 40;

    /**Maximum registration queue capacity*/
    private static final int REGISTER_QUEUE_CAPACITY = 1024;

    /** The maximum number of drains in a single time */
    private static final int DRAIN_BATCH_SIZE = 256;

    /** Application status：enable */
    private static final int APP_STATUS_ENABLED = 1;

    // gRPC Channel parameter
    private static final int GRPC_KEEP_ALIVE_TIME_SECONDS = 30;
    private static final int GRPC_KEEP_ALIVE_TIMEOUT_SECONDS = 10;
    private static final int GRPC_IDLE_TIMEOUT_MINUTES = 5;
    private static final int GRPC_MAX_INBOUND_MESSAGE_SIZE = 10 * 1024 * 1024;

    // ==================== Dependencies ====================

    private final AppMapper appMapper;
    private final ClientNodeMapper clientNodeMapper;

    // ==================== Core data structure ====================

    /** active instance cache, key = "{appId}/{hostId}"*/
    private final ConcurrentHashMap<String, ClientInstanceInfo> liveInstances = new ConcurrentHashMap<>();

    /** Heartbeat registration queue: receive heartbeat data and flush disks to DB in batches at regular intervals */
    private final LinkedBlockingDeque<ClientRegistration> registerQueue = new LinkedBlockingDeque<>(REGISTER_QUEUE_CAPACITY);

    // ==================== Heartbeat registration (write path) ====================

    /**
     * Register or renew client instance (heartbeat call)
     * <p>
     * 1. renew memory cache (real-time)
     * 2. Join the queue and wait for batch flushing (asynchronous)
     */
    public void registerOrUpdate(ClientRegistration registration) {
        String key = buildInstanceKey(registration.getAppId(), registration.getHostId());

        liveInstances.compute(key, (k, existing) -> {
            if (existing == null) {
                log.info("New client registration: appId={}, hostId={}, {}:{}",
                        registration.getAppId(), registration.getHostId(),
                        registration.getHostIp(), registration.getGrpcPort());
                ClientInstanceInfo info = new ClientInstanceInfo(
                        registration.getAppId(), registration.getHostId(),
                        registration.getHostIp(), registration.getGrpcPort());
                applyRegistration(info, registration);
                info.setSource(InstanceSource.LOCAL);
                info.setChannel(createChannel(info.getHostIp(), info.getGrpcPort()));
                return info;
            }

            applyRegistration(existing, registration);
            existing.setSource(InstanceSource.LOCAL);
            ensureChannelAlive(existing);
            return existing;
        });

        // Heartbeat priority is placed at the head of the queue, consistent with anik-job
        registerQueue.offerFirst(registration);
    }

    private void applyRegistration(ClientInstanceInfo info, ClientRegistration reg) {
        info.setMaxConcurrent(reg.getMaxConcurrent());
        info.setActiveChats(reg.getActiveChats());
        info.setSupportedProviders(reg.getSupportedProviders());
        info.setLabels(reg.getLabels());
        info.setLastHeartbeatTime(System.currentTimeMillis());
    }

    // ==================== Instance query (read path) ====================

    /**
     * Get all active instances of the specified application
     * <p>
     * Priority is given to reading from the memory cache; if the cache is empty, the query DB is downgraded.
     * Register the results in the local cache and try again (single retry, no risk of recursion).
     */
    public List<ClientInstanceInfo> getAliveInstances(String appId) {
        List<ClientInstanceInfo> cached = getAliveInstancesFromCache(appId);
        if (CollUtil.isNotEmpty(cached)) {
            return cached;
        }

        //DB downgrade: Query all unexpired client nodes under the appId
        List<ClientNodePO> dbNodes = clientNodeMapper.selectList(
                new LambdaQueryWrapper<ClientNodePO>()
                        .eq(ClientNodePO::getAppId, appId)
                        .gt(ClientNodePO::getExpireDt, LocalDateTime.now()));
        if (CollUtil.isEmpty(dbNodes)) {
            return List.of();
        }

        log.info("DB downgrade found {} client nodes: appId={}", dbNodes.size(), appId);
        for (ClientNodePO node : dbNodes) {
            registerFromDb(node);
        }

        // Re-fetch from cache (node ​​that already contains DB load)
        return getAliveInstancesFromCache(appId);
    }

    private List<ClientInstanceInfo> getAliveInstancesFromCache(String appId) {
        long now = System.currentTimeMillis();
        return liveInstances.values().stream()
                .filter(i -> i.getAppId().equals(appId))
                .filter(i -> isInstanceAlive(i, now))
                .toList();
    }

    private boolean isInstanceAlive(ClientInstanceInfo info, long currentTime) {
        return (currentTime - info.getLastHeartbeatTime() < EXPIRE_THRESHOLD_MS)
                && info.getChannel() != null
                && !info.getChannel().isShutdown();
    }

    // ==================== DB → Cache Registration ====================

    /**
     * Register DB rows to local cache
     * <p>
     * Core protection logic:
     * - LOCAL entries are not covered (local heartbeat data is more authoritative)
     * - REMOTE entries only refresh the heartbeat time (keep-alive)
     * - New entries are created for gRPC Channel and marked as REMOTE
     */
    private void registerFromDb(ClientNodePO node) {
        String key = buildInstanceKey(node.getAppId(), node.getHostId());

        liveInstances.compute(key, (k, existing) -> {
            if (existing != null && existing.getSource() == InstanceSource.LOCAL) {
                // Local heartbeat data is more authoritative and does not cover
                return existing;
            }

            if (existing != null) {
                //REMOTE entry: refresh heartbeat time + renew dynamic field
                existing.setLastHeartbeatTime(System.currentTimeMillis());
                existing.setMaxConcurrent(node.getMaxConcurrent() != null ? node.getMaxConcurrent() : existing.getMaxConcurrent());
                existing.setActiveChats(node.getActiveChats() != null ? node.getActiveChats() : existing.getActiveChats());
                ensureChannelAlive(existing);
                return existing;
            }

            // Create new REMOTE entry
            ClientInstanceInfo info = new ClientInstanceInfo(
                    node.getAppId(), node.getHostId(),
                    node.getHostIp(), node.getGrpcPort());
            info.setSource(InstanceSource.REMOTE);
            info.setMaxConcurrent(node.getMaxConcurrent() != null ? node.getMaxConcurrent() : 10);
            info.setActiveChats(node.getActiveChats() != null ? node.getActiveChats() : 0);
            info.setSupportedProviders(node.getSupportedProviders());
            info.setLabels(parseLabels(node.getLabels()));
            info.setLastHeartbeatTime(System.currentTimeMillis());
            info.setChannel(createChannel(node.getHostIp(), node.getGrpcPort()));
            log.info("Load remote client from DB: appId={}, hostId={}, {}:{}",
                    node.getAppId(), node.getHostId(), node.getHostIp(), node.getGrpcPort());
            return info;
        });
    }

    private Map<String, String> parseLabels(String labelsJson) {
        if (labelsJson == null || labelsJson.isBlank()) {
            return null;
        }
        try {
            return JsonUtil.parseHashMap(labelsJson);
        } catch (Exception e) {
            log.warn("Failed to parse labels JSON: {}", labelsJson, e);
            return null;
        }
    }

    // ==================== Scheduled task: Batch flash disk ====================

    /**
     * Batchly flush the heartbeat data in the registration queue to the DB every 5 seconds
     * <p>
     * Refer to the batch write mode of anik-job AbstractRegister.refreshExpireAt():
     * drain queue → deduplicate by (appId, hostId) → divided into two groups: insert/update → batch execution
     */
    @Scheduled(fixedDelay = 5_000)
    public void flushRegistrationQueue() {
        List<ClientRegistration> batch = drainQueue();
        if (batch.isEmpty()) {
            return;
        }

        // Press (appId, hostId) to remove duplicates and keep the latest
        Map<String, ClientRegistration> deduped = new LinkedHashMap<>();
        for (ClientRegistration reg : batch) {
            deduped.put(buildInstanceKey(reg.getAppId(), reg.getHostId()), reg);
        }

        List<ClientNodePO> nodes = deduped.values().stream()
                .map(this::toClientNodePO)
                .toList();

        // Query existing records in DB
        Set<String> existingKeys = queryExistingKeys(nodes);

        List<ClientNodePO> inserts = new ArrayList<>();
        List<ClientNodePO> updates = new ArrayList<>();
        for (ClientNodePO node : nodes) {
            String key = buildInstanceKey(node.getAppId(), node.getHostId());
            if (existingKeys.contains(key)) {
                updates.add(node);
            } else {
                inserts.add(node);
            }
        }

        batchInsert(inserts);
        batchUpdate(updates);
    }

    private List<ClientRegistration> drainQueue() {
        ClientRegistration first = registerQueue.poll();
        if (first == null) {
            return List.of();
        }
        List<ClientRegistration> batch = new ArrayList<>();
        batch.add(first);
        registerQueue.drainTo(batch, DRAIN_BATCH_SIZE - 1);
        return batch;
    }

    private ClientNodePO toClientNodePO(ClientRegistration reg) {
        return ClientNodePO.builder()
                .appId(reg.getAppId())
                .hostId(reg.getHostId())
                .hostIp(reg.getHostIp())
                .grpcPort(reg.getGrpcPort())
                .maxConcurrent(reg.getMaxConcurrent())
                .activeChats(reg.getActiveChats())
                .supportedProviders(reg.getSupportedProviders())
                .labels(reg.getLabels() != null ? JsonUtil.toJsonString(reg.getLabels()) : null)
                .expireDt(LocalDateTime.now().plusSeconds(EXPIRE_OFFSET_SECONDS))
                .build();
    }

    private Set<String> queryExistingKeys(List<ClientNodePO> nodes) {
        Set<String> hostIds = new HashSet<>();
        Set<String> appIds = new HashSet<>();
        for (ClientNodePO node : nodes) {
            hostIds.add(node.getHostId());
            appIds.add(node.getAppId());
        }

        List<ClientNodePO> existing = clientNodeMapper.selectList(
                new LambdaQueryWrapper<ClientNodePO>()
                        .select(ClientNodePO::getAppId, ClientNodePO::getHostId)
                        .in(ClientNodePO::getAppId, appIds)
                        .in(ClientNodePO::getHostId, hostIds));

        Set<String> keys = new HashSet<>();
        for (ClientNodePO e : existing) {
            keys.add(buildInstanceKey(e.getAppId(), e.getHostId()));
        }
        return keys;
    }

    private void batchInsert(List<ClientNodePO> inserts) {
        if (inserts.isEmpty()) {
            return;
        }
        try {
            clientNodeMapper.insertBatch(inserts);
        } catch (DuplicateKeyException ignored) {
            // Duplication caused by concurrent writing, ignored
        } catch (Exception e) {
            log.error("Batch insertion of client nodes failed", e);
        }
    }

    private void batchUpdate(List<ClientNodePO> updates) {
        if (updates.isEmpty()) {
            return;
        }
        try {
            clientNodeMapper.updateBatchExpireAt(updates);
        } catch (Exception e) {
            log.error("Batch update of client nodes failed", e);
        }
    }

    // ===========updateBatchExpireAt========== Scheduled task: cross-server warm-up ====================

    /**
     * Pull all unexpired client nodes from DB every 10 seconds and warm up the local cache
     * <p>
     * Solve the cluster deployment scenario: the client heartbeat only goes to one server, and other servers are discovered through DB sharing.
     * registerFromDb internally protects LOCAL entries from being overwritten.
     */
    @Scheduled(fixedDelay = 10_000)
    public void pullRemoteNodesFromDb() {
        try {
            List<ClientNodePO> aliveNodes = clientNodeMapper.selectList(
                    new LambdaQueryWrapper<ClientNodePO>()
                            .gt(ClientNodePO::getExpireDt, LocalDateTime.now()));
            if (CollUtil.isEmpty(aliveNodes)) {
                return;
            }

            for (ClientNodePO node : aliveNodes) {
                registerFromDb(node);
            }
        } catch (Exception e) {
            log.error("Failed to pull remote client node from DB", e);
        }
    }

    // ==================== Scheduled tasks: expiration cleanup ====================

    /**
     * Clean up expired memory instances every 10 seconds
     */
    @Scheduled(fixedDelay = 10_000)
    public void expireStaleInstances() {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<String, ClientInstanceInfo>> it = liveInstances.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<String, ClientInstanceInfo> entry = it.next();
            ClientInstanceInfo info = entry.getValue();

            if (shouldExpireInstance(info, now)) {
                log.info("Client expiration removal: {} (source={})", entry.getKey(), info.getSource());
                shutdownChannel(info.getChannel());
                it.remove();
            }
        }
    }

    private boolean shouldExpireInstance(ClientInstanceInfo info, long currentTime) {
        return (currentTime - info.getLastHeartbeatTime() > EXPIRE_THRESHOLD_MS)
                || (info.getChannel() != null && info.getChannel().isTerminated());
    }

    /**
     * Clean out expired node rows in DB every 30 seconds
     */
    @Scheduled(fixedDelay = 30_000)
    public void cleanExpiredNodes() {
        try {
            clientNodeMapper.delete(new LambdaQueryWrapper<ClientNodePO>()
                    .lt(ClientNodePO::getExpireDt, LocalDateTime.now().minusSeconds(EXPIRE_OFFSET_SECONDS)));
        } catch (Exception e) {
            log.error("Failed to clean up expired client nodes", e);
        }
    }

    // ==================== Token verification ====================

    /**
     * Verify appId + token
     */
    public boolean validateToken(String appId, String token) {
        AppPO app = appMapper.selectOne(
                new LambdaQueryWrapper<AppPO>()
                        .eq(AppPO::getAppId, appId)
                        .eq(AppPO::getStatus, APP_STATUS_ENABLED));
        return app != null && app.getToken().equals(token);
    }

    // ==================== Instance removal ====================

    /**
     * Actively remove instances
     */
    public void removeInstance(String appId, String hostId) {
        String key = buildInstanceKey(appId, hostId);
        ClientInstanceInfo removed = liveInstances.remove(key);
        if (removed != null) {
            shutdownChannel(removed.getChannel());
        }
    }

    // ==================== Tool methods ====================

    private String buildInstanceKey(String appId, String hostId) {
        return appId + "/" + hostId;
    }

    private void ensureChannelAlive(ClientInstanceInfo info) {
        if (info.getChannel() == null || info.getChannel().isShutdown()) {
            info.setChannel(createChannel(info.getHostIp(), info.getGrpcPort()));
        }
    }

    private ManagedChannel createChannel(String hostIp, int grpcPort) {
        return NettyChannelBuilder.forAddress(hostIp, grpcPort)
                .usePlaintext()
                .keepAliveTime(GRPC_KEEP_ALIVE_TIME_SECONDS, TimeUnit.SECONDS)
                .keepAliveTimeout(GRPC_KEEP_ALIVE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .idleTimeout(GRPC_IDLE_TIMEOUT_MINUTES, TimeUnit.MINUTES)
                .maxInboundMessageSize(GRPC_MAX_INBOUND_MESSAGE_SIZE)
                .build();
    }

    private void shutdownChannel(ManagedChannel channel) {
        if (channel != null && !channel.isShutdown()) {
            try {
                channel.shutdown();
            } catch (Exception e) {
                log.warn("Failed to close gRPC Channel", e);
            }
        }
    }

    // ==================== Data class ====================

    /**
     * Client instance information
     */
    @Data
    public static class ClientInstanceInfo {
        private final String appId;
        private final String hostId;
        private final String hostIp;
        private final int grpcPort;
        private volatile ManagedChannel channel;
        private volatile int maxConcurrent;
        private volatile int activeChats;
        private volatile String supportedProviders;
        private volatile Map<String, String> labels;
        private volatile long lastHeartbeatTime;
        /** Instance source: LOCAL=local heartbeat, REMOTE=DB loading*/
        @Getter
        private volatile InstanceSource source;

        public ClientInstanceInfo(String appId, String hostId, String hostIp, int grpcPort) {
            this.appId = appId;
            this.hostId = hostId;
            this.hostIp = hostIp;
            this.grpcPort = grpcPort;
            this.lastHeartbeatTime = System.currentTimeMillis();
            this.source = InstanceSource.LOCAL;
        }
    }

    /**
     * Client-side registration information (data reported by heartbeat)
     */
    @Data
    public static class ClientRegistration {
        private String appId;
        private String hostId;
        private String hostIp;
        private int grpcPort;
        private int maxConcurrent;
        private int activeChats;
        private String supportedProviders;
        private Map<String, String> labels;
    }
}
