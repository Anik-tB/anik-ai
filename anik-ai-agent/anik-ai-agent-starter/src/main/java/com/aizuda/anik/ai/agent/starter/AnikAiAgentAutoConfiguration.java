package com.aizuda.anik.ai.agent.starter;

import com.aizuda.anik.ai.agent.common.rpc.RpcClient;
import com.aizuda.anik.ai.agent.common.rpc.GrpcClientInvokeHandler;
import com.aizuda.anik.ai.agent.common.config.AnikAiAgentProperties;
import com.aizuda.anik.ai.agent.common.context.AgentChatContextThreadLocalAccessor;
import com.aizuda.anik.ai.agent.common.rpc.GrpcChannelProvider;
import com.aizuda.anik.ai.agent.common.handler.ClientHeartbeatScheduler;
import com.aizuda.anik.ai.agent.common.rpc.ClientGrpcServer;
import com.aizuda.anik.ai.agent.core.ClientRequestDispatcher;
import com.aizuda.anik.ai.agent.core.advisor.InterceptorChainAdvisor;
import com.aizuda.anik.ai.agent.core.advisor.MemoryInjectionAdvisor;
import com.aizuda.anik.ai.agent.core.advisor.StreamChunkForwarderAdvisor;
import com.aizuda.anik.ai.agent.core.advisor.ThinkingCollectorAdvisor;
import com.aizuda.anik.ai.agent.core.advisor.TokenUsageCollectorAdvisor;
import com.aizuda.anik.ai.agent.core.executor.ClientChatExecutor;
import com.aizuda.anik.ai.agent.core.grpc.handler.ChatDispatchStreamingHandler;
import com.aizuda.anik.ai.agent.core.grpc.handler.PingRequestHandler;
import com.aizuda.anik.ai.agent.core.interceptor.AnikAiInterceptor;
import com.aizuda.anik.ai.agent.core.interceptor.impl.LoggingInterceptor;
import com.aizuda.anik.ai.agent.core.resolver.ClientMemoryToolResolver;
import com.aizuda.anik.ai.agent.core.resolver.ClientRagToolResolver;
import com.aizuda.anik.ai.agent.core.resolver.ClientSkillToolResolver;
import com.aizuda.anik.ai.agent.core.resolver.CustomToolCallbackProvider;
import com.aizuda.anik.ai.common.grpc.handler.GrpcRequestDispatcher;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import reactor.core.publisher.Hooks;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.List;

import com.aizuda.anik.ai.agent.common.counter.ActiveChatCounter;

/**
 * Anik-AI Agent client automatic configuration
 *
 * @author openanik
 * @date 2025-04-08
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(AnikAiAgentProperties.class)
@ConditionalOnProperty(prefix = "anik-ai", name = "enabled", havingValue = "true", matchIfMissing = false)
public class AnikAiAgentAutoConfiguration {

    private ClientHeartbeatScheduler heartbeatTask;
    private ClientGrpcServer clientGrpcServer;
    private GrpcChannelProvider grpcChannelProvider;

    // ==================== Core Components ====================

    @Bean
    public ActiveChatCounter activeChatCounter() {
        return new ActiveChatCounter();
    }

    @Bean
    public MemoryInjectionAdvisor memoryInjectionAdvisor() {
        return new MemoryInjectionAdvisor();
    }

    @Bean
    public InterceptorChainAdvisor interceptorChainAdvisor(List<AnikAiInterceptor> interceptors) {
        return new InterceptorChainAdvisor(interceptors);
    }

    @Bean
    public ThinkingCollectorAdvisor thinkingCollectorAdvisor() {
        return new ThinkingCollectorAdvisor();
    }

    @Bean
    public TokenUsageCollectorAdvisor tokenUsageCollectorAdvisor() {
        return new TokenUsageCollectorAdvisor();
    }

    @Bean
    public StreamChunkForwarderAdvisor streamChunkForwarderAdvisor() {
        return new StreamChunkForwarderAdvisor();
    }

    @Bean
    @ConditionalOnProperty(prefix = "anik-ai.agent", name = "logging-interceptor", havingValue = "true")
    public LoggingInterceptor loggingInterceptor() {
        return new LoggingInterceptor();
    }

    @Bean
    public ClientChatExecutor clientChatExecutor(Environment env,
                                                 MemoryInjectionAdvisor memoryInjectionAdvisor,
                                                 InterceptorChainAdvisor interceptorChainAdvisor,
                                                 TokenUsageCollectorAdvisor tokenUsageCollectorAdvisor,
                                                 ThinkingCollectorAdvisor thinkingCollectorAdvisor,
                                                 StreamChunkForwarderAdvisor streamChunkForwarderAdvisor) {

        Hooks.enableAutomaticContextPropagation();
        return new ClientChatExecutor(null,
                resolveActiveProfile(env),
                memoryInjectionAdvisor,
                interceptorChainAdvisor,
                tokenUsageCollectorAdvisor,
                thinkingCollectorAdvisor,
                streamChunkForwarderAdvisor);
    }

    @Bean
    @ConditionalOnMissingBean
    public GrpcChannelProvider grpcChannelProvider() {
        return new GrpcChannelProvider();
    }

    @Bean
    @ConditionalOnMissingBean
    public RpcClient serverCallback(
            GrpcChannelProvider channelProvider,
            AnikAiAgentProperties properties) {

        InvocationHandler handler = new GrpcClientInvokeHandler(channelProvider, properties);
        return (RpcClient) Proxy.newProxyInstance(
                RpcClient.class.getClassLoader(),
                new Class<?>[]{RpcClient.class},
                handler
        );
    }

    @Bean
    public ClientRagToolResolver clientRagToolResolver(RpcClient rpcClient) {
        return new ClientRagToolResolver(rpcClient);
    }

    @Bean
    public ClientMemoryToolResolver clientMemoryToolResolver() {
        return new ClientMemoryToolResolver();
    }

    @Bean
    public ClientSkillToolResolver clientSkillToolResolver(RpcClient rpcClient,
                                                           AnikAiAgentProperties props) {
        String skillTempDir = props.getSkillTempDir() != null ? props.getSkillTempDir() : "/tmp/anik-ai-agent/skills";
        return new ClientSkillToolResolver(rpcClient, skillTempDir);
    }

    @Bean
    public CustomToolCallbackProvider customToolCallbackProvider(ApplicationContext applicationContext) {
        return new CustomToolCallbackProvider(applicationContext);
    }

    @Bean
    public PingRequestHandler pingRequestHandler(ActiveChatCounter activeChatCounter) {
        return new PingRequestHandler(activeChatCounter);
    }

    @Bean
    public ChatDispatchStreamingHandler chatDispatchStreamingHandler(ClientChatExecutor clientChatExecutor,
                                                                     ActiveChatCounter activeChatCounter,
                                                                     ClientSkillToolResolver skillToolResolver,
                                                                     ClientRagToolResolver ragToolResolver,
                                                                     ClientMemoryToolResolver memoryToolResolver,
                                                                     CustomToolCallbackProvider customToolCallbackProvider) {
        return new ChatDispatchStreamingHandler(
                clientChatExecutor, activeChatCounter, skillToolResolver,
                ragToolResolver, memoryToolResolver, customToolCallbackProvider);
    }

    @Bean
    public ClientRequestDispatcher clientRequestDispatcher(GrpcRequestDispatcher grpcRequestDispatcher) {
        return new ClientRequestDispatcher(grpcRequestDispatcher);
    }

    //==================== Life cycle ====================

    @EventListener(ContextRefreshedEvent.class)
    public void onApplicationReady(ContextRefreshedEvent event) {
        AnikAiAgentProperties props = event.getApplicationContext().getBean(AnikAiAgentProperties.class);
        ClientRequestDispatcher dispatcher = event.getApplicationContext().getBean(ClientRequestDispatcher.class);
        grpcChannelProvider = event.getApplicationContext().getBean(GrpcChannelProvider.class);

        //Start the client gRPC service
        clientGrpcServer = new ClientGrpcServer(
                props.getPort(),
                dispatcher.unaryHandler(),
                dispatcher.streamingHandler());
        try {
            clientGrpcServer.start();
        } catch (Exception e) {
            log.error("Failed to start client gRPC server on port {}", props.getPort(), e);
            return;
        }

        // Start heartbeat
        ActiveChatCounter counter = event.getApplicationContext().getBean(ActiveChatCounter.class);
        heartbeatTask = new ClientHeartbeatScheduler(props, counter, grpcChannelProvider);
        heartbeatTask.start();

        log.info("Anik-AI Agent started: appId={}, grpcPort={}, server={}:{}",
                props.getAppId(), props.getPort(), props.getServer().getHost(), props.getServer().getPort());
    }

    @PreDestroy
    public void shutdown() {
        if (heartbeatTask != null) heartbeatTask.stop();
        if (clientGrpcServer != null) clientGrpcServer.stop();
        log.info("Anik-AI Agent shutdown");
    }

    // ==================== Tool methods ====================

    /**
     * Resolve the current activation's profile from Spring Environment for observability environment fields
     */
    private static String resolveActiveProfile(org.springframework.core.env.Environment env) {
        String[] profiles = env.getActiveProfiles();
        if (profiles.length > 0) {
            return profiles[0];
        }
        String[] defaults = env.getDefaultProfiles();
        if (defaults.length > 0) {
            return defaults[0];
        }
        return "default";
    }
}
