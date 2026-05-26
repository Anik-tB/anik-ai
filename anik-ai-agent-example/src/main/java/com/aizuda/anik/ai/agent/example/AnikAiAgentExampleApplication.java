package com.aizuda.anik.ai.agent.example;

import com.aizuda.anik.ai.agent.starter.EnableAnikAiAgent;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Anik-AI Agent client example
 * <p>
 * After startup, it automatically connects to the server, registers heartbeats, and receives Chat distribution requests.
 *
 * <pre>
 * Usage steps:
 * 1. Create an application on the server-side "Application Management" page and obtain the app-id and token.
 * 2. Modify server-host, app-id, token in application.yml
 * 3. Launch this application
 * 4. Create an Agent on the server side and associate it with this application
 * 5. Send the message and observe the log to confirm that Chat is distributed to this client for execution.
 * </pre>
 */
@EnableAnikAiAgent
@SpringBootApplication
public class AnikAiAgentExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnikAiAgentExampleApplication.class, args);
    }
}
