package com.aizuda.anik.ai.admin.vo.memory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddMemoryRequestVO {


    private Long agentId;
    private Long userId;
    private String conversationId;
    private List<MemoryChatMessageVO> messages;

    /** Default true: use LLM extraction */
    @Builder.Default
    private Boolean autoExtract = Boolean.TRUE;
}
