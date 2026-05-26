package com.aizuda.anik.ai.admin.vo.rag;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * <p>
 *
 * </p>
 *
 * @author openanik
 * @date 2025-07-19
 */
@Data
public class ParagraphResponseVO {

    private Long id;

    private Long documentId;

    private String content;

    private LocalDateTime createDt;

    private LocalDateTime updateDt;
}
