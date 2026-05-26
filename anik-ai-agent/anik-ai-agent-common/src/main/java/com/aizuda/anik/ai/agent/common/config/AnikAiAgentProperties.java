package com.aizuda.anik.ai.agent.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "anik-ai")
public class AnikAiAgentProperties {

    private boolean enabled = true;

    /** Application ID*/
    private String appId;

    /** Authentication token */
    private String token;

    /** Specify the client port*/
    private int port = 17889;

    /**
     * Specify the client IP, the default is the local IP
     */
    private String host;

    /** Maximum number of concurrent conversations*/
    private int maxConcurrentChats = 10;

    /** Skill file temporary directory */
    private String skillTempDir = "/tmp/anik-ai-agent/skills";

    /**
     * Server configuration
     */
    private ServerConfig server = new ServerConfig();

    @Data
    public static class ServerConfig {
        /**
         * The address of the server. If the server cluster is deployed, configure the domain name here.
         */
        private String host = "127.0.0.1";

        /**
         * The port number of the server rpc
         */
        private int port = 18888;

        /** Timeout (milliseconds) */
        private long timeout = 5000;

        /** Number of retries */
        private int retryTimes = 3;

        /** Retry interval (milliseconds) */
        private int retryInterval = 1000;
    }

}
