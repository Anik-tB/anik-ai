package com.aizuda.anik.ai.admin.vo.memory;

import com.aizuda.anik.ai.memory.dto.ConversationMemoryDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Preview of history and memory for the next conversation request (rough token estimate, no access log written)
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConversationContextPreviewVO {

    private int historyMessageCount;
    private List<String> historyPreviewLines;
    private List<ConversationMemoryDTO> memories;
    private int estimatedPromptTokens;
    private boolean compressionApplied;
}
