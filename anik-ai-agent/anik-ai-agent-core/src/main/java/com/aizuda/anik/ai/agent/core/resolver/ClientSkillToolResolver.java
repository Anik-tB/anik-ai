package com.aizuda.anik.ai.agent.core.resolver;

import com.aizuda.anik.ai.agent.common.rpc.RpcClient;
import com.aizuda.anik.ai.common.dto.agent.ChatDispatchRequest;
import com.aizuda.anik.ai.agent.core.tool.HttpTool;
import com.aizuda.anik.ai.agent.core.tool.ReadSkillTool;
import com.aizuda.anik.ai.agent.core.tool.ShellTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Client-side Skill tool parser
 * <p>
 * Register 3 tools (consistent with the server side):
 * <ul>
 *   <li>{@code read_skill} — Get the complete content + file of the skill through gRPC callback Server</li>
 *   <li>{@code shell} — Execute shell commands in the skill's temporary directory</li>
 *   <li>{@code http_request} — Makes an HTTP request</li>
 * </ul>
 *
 * @author openanik
 */
@Slf4j
public class ClientSkillToolResolver {

    private static final long DEFAULT_SHELL_TIMEOUT_MS = 60000;
    private static final int DEFAULT_SHELL_MAX_OUTPUT_LINES = 500;
    private static final long DEFAULT_HTTP_TIMEOUT_MS = 30000;

    private final RpcClient rpcClient;
    private final String skillTempDir;

    public ClientSkillToolResolver(RpcClient rpcClient, String skillTempDir) {
        this.rpcClient = rpcClient;
        this.skillTempDir = skillTempDir;
    }

    /**
     * Building a tool callback from a list of Skill descriptors
     */
    public List<ToolCallback> resolve(ChatDispatchRequest request) {
        List<ChatDispatchRequest.SkillDescriptor> skills = request.getSkills();
        if (skills == null || skills.isEmpty()) {
            return List.of();
        }

        List<ToolCallback> callbacks = new ArrayList<>();
        try {
            String skillPrompt = SkillPromptConstants.SYSTEM_PROMPT_TEMPLATE
                    .replace("{skills_list}", buildSkillsList(skills));
            request.setSystemPrompt(request.getSystemPrompt() + skillPrompt);

            callbacks.addAll(Arrays.asList(ToolCallbacks.from(
                    new ReadSkillTool(skills, rpcClient, skillTempDir),
                    new ShellTool(skillTempDir, DEFAULT_SHELL_TIMEOUT_MS, DEFAULT_SHELL_MAX_OUTPUT_LINES),
                    new HttpTool(DEFAULT_HTTP_TIMEOUT_MS)
            )));
            log.info("Skill tools resolved: {} skills, ReadSkillTool + ShellTool + HttpTool registered", skills.size());
        } catch (Exception e) {
            log.warn("Failed to resolve skill tools", e);
        }

        return callbacks;
    }

    /**
     * Build a lightweight Skill list (only name + description + filePath), without complete skillContent
     */
    public String buildSkillsList(List<ChatDispatchRequest.SkillDescriptor> skills) {
        StringBuilder sb = new StringBuilder();
        for (ChatDispatchRequest.SkillDescriptor skill : skills) {
            sb.append("- **").append(skill.getName()).append("**");
            if (skill.getDescription() != null) {
                sb.append(": ").append(skill.getDescription());
            }
            sb.append(" → Supporting file directory: `").append(getSkillTempDir(skill)).append("`");
            sb.append("\n");
        }
        return sb.toString();
    }

    public Path getSkillTempDir(ChatDispatchRequest.SkillDescriptor po) {
        return Path.of(skillTempDir + File.separator + po.getId() + File.separator + po.getVersion());
    }
}
