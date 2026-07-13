package com.aianik.anik.ai.agent.core.advisor;

/**
 * The parameter key used in {@link org.springframework.ai.chat.client.ChatClientRequest#context()}.
 */
public final class ClientAdvisorKeys {

    /** {@link com.aianik.anik.ai.common.dto.agent.ChatDispatchRequest} */
    public static final String DISPATCH = "anik.dispatch";

    /** {@link com.aianik.anik.ai.agent.common.report.ClientTraceBuffer} (optional, for interceptor use) */
    public static final String TRACE_BUFFER = "anik.traceBuffer";

    public static final String STREAM_STATE = "anik.streamState";

    /** java.util.function.Consumer<String> text chunk*/
    public static final String CHUNK_CONSUMER = "anik.chunkConsumer";

    /** java.util.function.Consumer<String> think chunk*/
    public static final String THINKING_CONSUMER = "anik.thinkingConsumer";

    private ClientAdvisorKeys() {
    }
}
