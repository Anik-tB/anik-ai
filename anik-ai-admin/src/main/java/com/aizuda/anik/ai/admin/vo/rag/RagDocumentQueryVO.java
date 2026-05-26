package com.aizuda.anik.ai.admin.vo.rag;

import com.aizuda.anik.ai.common.vo.BaseQueryVO;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RagDocumentQueryVO extends BaseQueryVO {

    @JsonAlias("knowledgeId")
    private Long ragId;

    private String name;

    private Integer status;
}
