package com.aianik.anik.ai.feature.agent.chain;

import cn.hutool.core.util.StrUtil;
import com.aianik.anik.ai.ClientInstanceManager;
import com.aianik.anik.ai.route.ClientRouteStrategy;
import com.aianik.anik.ai.route.ClientRouteStrategyManager;
import com.aianik.anik.ai.route.RouteStrategyType;
import com.aianik.anik.ai.common.enums.CommonStatusEnum;
import com.aianik.anik.ai.memory.dto.ShortTermHistoryQuery;
import com.aianik.anik.ai.memory.store.ShortTermMemoryStore;
import com.aianik.anik.ai.persistence.agent.po.AgentPO;
import com.aianik.anik.ai.persistence.app.mapper.AppMapper;
import com.aianik.anik.ai.persistence.app.po.AppPO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import com.aianik.anik.ai.common.dto.agent.ChatDispatchRequest;

import java.util.List;
import java.util.stream.Collectors;

/**
 * After the system prompts and tool data are ready, the short-term history, long-term memory and target Client are added for {@link LlmCallHandler} to assemble and distribute the request.
 */
@Slf4j
@Component
@Order(75)
@RequiredArgsConstructor
public class ContextCollectorHandler implements AgentChatHandler {

    private static final int DEFAULT_SHORT_TERM_WINDOW = 20;

    private final ShortTermMemoryStore shortTermMemoryStore;
    private final ClientInstanceManager instanceManager;
    private final ClientRouteStrategyManager routeStrategyManager;
    private final AppMapper appMapper;

    @Override
    public void handle(AgentChatContext ctx) {
        if (ctx.isTerminated()) {
            return;
        }

        AgentPO agent = ctx.getAgent();
        String appId = agent.getAppId();
        if (appId == null || appId.isBlank()) {
            sendError(ctx, "Agent is not configured with appId and cannot be distributed remotely.");
            return;
        }

        AppPO app = findEnabledApp(appId);
        if (app == null) {
            sendError(ctx, "App does not exist or is disabled: " + appId);
            return;
        }

        List<ClientInstanceManager.ClientInstanceInfo> candidates = instanceManager.getAliveInstances(appId);
        if (candidates.isEmpty()) {
            sendError(ctx, "No client instance available: " + appId);
            return;
        }

        String routeKey = StrUtil.isNotBlank(app.getRouteStrategy()) ? app.getRouteStrategy() : RouteStrategyType.LEAST_LOAD;
        ClientRouteStrategy routeStrategy = routeStrategyManager.get(routeKey);
        ClientInstanceManager.ClientInstanceInfo target = routeStrategy.select(candidates, ctx.getConversationId());
        ctx.setTargetClient(target);

        int shortTermWindow = agent.getShortTermMemorySize() != null && agent.getShortTermMemorySize() > 0
                ? agent.getShortTermMemorySize()
                : DEFAULT_SHORT_TERM_WINDOW;
        boolean shortTermEnabled = !Boolean.FALSE.equals(agent.getMemoryEnabled());
        if (shortTermEnabled) {
            shortTermMemoryStore.append(ctx.getConversationId(), "user", ctx.getContent(), shortTermWindow);
            ctx.setHistoryMessages(loadHistoryMessages(ctx, shortTermWindow));
        } else {
            ctx.setHistoryMessages(List.of());
        }

        log.info("Context collected for dispatch: appId={}, client={}:{}, historySize={}, shortTermEnabled={}, memoryPresent={}",
                appId, target.getHostIp(), target.getGrpcPort(),
                ctx.getHistoryMessages().size(),
                shortTermEnabled,
                ctx.getMemoryContext() != null && !ctx.getMemoryContext().isEmpty());
    }

    private AppPO findEnabledApp(String appId) {
        return appMapper.selectOne(
                new LambdaQueryWrapper<AppPO>()
                        .eq(AppPO::getAppId, appId)
                        .eq(AppPO::getStatus, CommonStatusEnum.ENABLED.getValue()));
    }

    private List<ChatDispatchRequest.HistoryMessage> loadHistoryMessages(AgentChatContext ctx, int windowSize) {
        try {
            ShortTermHistoryQuery query = ShortTermHistoryQuery.builder()
                    .conversationId(ctx.getConversationId())
                    .agentId(ctx.getAgentId())
                    .userId(ctx.getUser().getId())
                    .shortTermMemorySize(windowSize)
                    .build();
            var history = shortTermMemoryStore.loadHistory(query, windowSize);
            if (history == null) {
                return List.of();
            }
            return history.stream()
                    .map(msg -> ChatDispatchRequest.HistoryMessage.builder()
                            .role(msg.getRole())
                            .content(msg.getContent())
                            .build())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Failed to load history messages for dispatch", e);
            return List.of();
        }
    }

    private void sendError(AgentChatContext ctx, String message) {
        try {
            ctx.getEmitter().send("[ERROR] " + message);
            ctx.getEmitter().complete();
        } catch (Exception e) {
            log.warn("Failed to send error to emitter", e);
        }
        ctx.setTerminated(true);
    }
}
