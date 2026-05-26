package com.aizuda.anik.ai.admin.vo.rag;

import lombok.Data;

/**
 * author: openanik date: 2025-07-18
 */
@Data
public class ImportRequestVO {

    private Long docId;
    private String appendPrefix;
    private String appendPost;
    private String resourcePath;
}
