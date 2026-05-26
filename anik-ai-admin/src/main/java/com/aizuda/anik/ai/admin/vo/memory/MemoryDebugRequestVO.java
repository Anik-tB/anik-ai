package com.aizuda.anik.ai.admin.vo.memory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MemoryDebugRequestVO {

    /** Agent used for retrieval (must have this memory config bound to the agent) */
    @NotNull
    private Long agentId;

    /** Conversation user ID (memory entity) */
    @NotNull
    private Long userId;

    @NotBlank
    private String query;

    private String conversationId;
}
