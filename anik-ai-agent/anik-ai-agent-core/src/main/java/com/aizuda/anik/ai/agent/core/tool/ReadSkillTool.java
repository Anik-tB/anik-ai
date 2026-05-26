package com.aizuda.anik.ai.agent.core.tool;

import com.aizuda.anik.ai.agent.common.rpc.RpcClient;
import com.aizuda.anik.ai.common.dto.agent.SkillContentResponse;
import com.aizuda.anik.ai.common.dto.agent.SkillContentRequest;
import com.aizuda.anik.ai.common.dto.agent.ChatDispatchRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-side version of read_skill tool (aligned with Server-side ReadSkillTool)
 * <p>
 * design:
 * <ol>
 *   <li>The skillContent is obtained synchronously (through gRPC callback to Server) when called for the first time and cached</li>
 *   <li>Support files (scripts, references, etc.) are written asynchronously to the local temporary directory without blocking returns</li>
 *   <li>Subsequent repeated calls to the same skill will directly return to the cache and no longer request the Server</li>
 * </ol>
 */
@Slf4j
public class ReadSkillTool {

    private final Map<String, ChatDispatchRequest.SkillDescriptor> skillRegistry;
    private final RpcClient rpcClient;
    private final String tempDir;

    /** Cache: skillId → skillContent (to avoid repeated callbacks to Server in the same conversation)*/
    private final ConcurrentHashMap<Long, String> contentCache = new ConcurrentHashMap<>();

    public ReadSkillTool(List<ChatDispatchRequest.SkillDescriptor> skills,
                         RpcClient rpcClient,
                         String tempDir) {
        this.rpcClient = rpcClient;
        this.tempDir = tempDir;
        this.skillRegistry = new HashMap<>();
        if (skills != null) {
            for (ChatDispatchRequest.SkillDescriptor s : skills) {
                skillRegistry.put(s.getName(), s);
            }
        }
    }

    @Tool(name = "read_skill",
          description = "Read the full instruction content (SKILL.md) of a specified skill. "
                  + "Call this tool first to get detailed execution steps and parameter descriptions before using a skill. "
                  + "The parameter must exactly match a name from the available skills list.")
    public String readSkill(@ToolParam(description = "Skill name, must exactly match a name from the available skills list") String skillName) {
        if (skillName == null || skillName.trim().isEmpty()) {
            return "Error: please provide a skill name";
        }

        ChatDispatchRequest.SkillDescriptor descriptor = skillRegistry.get(skillName.trim());
        if (descriptor == null) {
            return "Error: skill \"" + skillName + "\" not found, available skills: " + String.join(", ", skillRegistry.keySet());
        }

        // Hit cache and return directly
        String cached = contentCache.get(descriptor.getId());
        if (cached != null) {
            log.debug("read_skill cache hit: skill={}", skillName);
            return cached;
        }

        log.info("read_skill: fetching content for skill={}, id={}", skillName, descriptor.getId());

        // Get skillContent synchronously (this is the instruction content that LLM needs to read immediately)
        SkillContentRequest request = SkillContentRequest.builder()
                .skillId(descriptor.getId())
                .agentId(null)
                .build();
        
        SkillContentResponse response = rpcClient.fetchSkillContent(request);
        if (response == null || response.getSkillContent() == null) {
            return "Error: failed to retrieve skill content, please try again later";
        }

        //Caching skillContent
        contentCache.put(descriptor.getId(), response.getSkillContent());

        // Asynchronously write supporting files to the local temporary directory (without blocking read_skill return)
        String version = descriptor.getVersion() != null ? descriptor.getVersion() : "1";
        Path skillDir = Path.of(tempDir, String.valueOf(descriptor.getId()), version);
        CompletableFuture.runAsync(() -> {
            try {
                materializeFiles(skillDir, response);
                log.info("Skill files materialized to: {}", skillDir);
            } catch (Exception e) {
                log.warn("Failed to materialize skill files to temp dir: {}", skillDir, e);
            }
        });

        // Return skillContent immediately (consistent with Server-side ReadSkillTool behavior)
        return response.getSkillContent();
    }

    /**
     * Get skill temporary directory path (for use by ShellTool)
     */
    public String getSkillDir(Long skillId, String version) {
        return Path.of(tempDir, String.valueOf(skillId), version != null ? version : "1").toString();
    }

    private void materializeFiles(Path skillDir, SkillContentResponse response) throws Exception {
        Files.createDirectories(skillDir);

        // Write to SKILL.md
        Files.writeString(skillDir.resolve("SKILL.md"), response.getSkillContent(), StandardCharsets.UTF_8);

        // Write supporting files
        if (response.getFiles() != null) {
            for (SkillContentResponse.SkillFile file : response.getFiles()) {
                Path filePath = skillDir.resolve(file.getFilePath());
                Files.createDirectories(filePath.getParent());

                if ("base64".equals(file.getEncoding())) {
                    byte[] decoded = Base64.getDecoder().decode(file.getContent());
                    Files.write(filePath, decoded);
                } else {
                    Files.writeString(filePath, file.getContent(), StandardCharsets.UTF_8);
                }
            }
        }
    }
}
