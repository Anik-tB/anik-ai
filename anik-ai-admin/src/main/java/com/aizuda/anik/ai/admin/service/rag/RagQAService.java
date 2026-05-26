package com.aizuda.anik.ai.admin.service.rag;

import cn.hutool.core.util.StrUtil;
import com.aizuda.anik.ai.common.execption.AnikAiException;
import com.aizuda.anik.ai.common.util.JsonUtil;
import com.aizuda.anik.ai.model.model.ModelFactory;
import com.aizuda.anik.ai.model.model.chat.ChatModel;
import com.aizuda.anik.ai.persistence.rag.mapper.RagMapper;
import com.aizuda.anik.ai.persistence.rag.dataobject.RagConfigDO;
import com.aizuda.anik.ai.persistence.rag.po.RagPO;
import com.aizuda.anik.ai.common.dto.rag.SearchResult;
import com.aizuda.anik.ai.admin.vo.rag.RagQARequestVO;
import com.aizuda.anik.ai.features.rag.dto.RagSearchRequestDTO;
import com.aizuda.anik.ai.features.rag.service.RagSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagQAService {

    private final RagSearchService ragSearchService;
    private final ModelFactory modelFactory;
    private final RagMapper knowledgeMapper;

    /**
     * Streaming Q&A: Read knowledge base configuration from DB → Retrieve → Assemble prompt → Call LLM streaming output
     */
    public void qaStream(RagQARequestVO request, ResponseBodyEmitter emitter) {
        try {
            RagPO knowledge = knowledgeMapper.selectById(request.getRagId());
            if (knowledge == null) {
                throw new AnikAiException("Knowledge not found: " + request.getRagId());
            }

            // Read knowledge base DB configuration
            RagConfigDO configDO = StrUtil.isNotBlank(knowledge.getConfig())
                    ? JsonUtil.parseObject(knowledge.getConfig(), RagConfigDO.class)
                    : new RagConfigDO();
            if (configDO == null) {
                configDO = new RagConfigDO();
            }
            RagConfigDO.SearchParams searchParams = configDO.getSearchParams() != null
                    ? configDO.getSearchParams() : new RagConfigDO.SearchParams();
            RagConfigDO.ModelParams mp = configDO.getModelParams() != null
                    ? configDO.getModelParams() : new RagConfigDO.ModelParams();

            if (mp.getModelId() == null) {
                throw new IllegalStateException("The question and answer model is not configured in the knowledge base. Please set it on the configuration page first.");
            }

            // 1. Search (use SearchParams directly, no conversion required)
            RagConfigDO searchConfigDO = RagConfigDO.builder()
                    .searchParams(searchParams)
                    .modelParams(mp)
                    .build();
            RagSearchRequestDTO searchRequest = new RagSearchRequestDTO();
            searchRequest.setRagId(request.getRagId());
            searchRequest.setQuery(request.getQuery());
            List<SearchResult> searchResults = new ArrayList<>(
                    ragSearchService.search(searchRequest, searchConfigDO).getResults());


            // 4. Assembly prompt
            String documentsText = buildDocumentsText(searchResults);
            String systemPrompt = buildSystemPrompt(mp.getPrompt(), documentsText);

            // 5. Streaming call LLM
            ChatModel chatModel = (ChatModel) modelFactory.getModel(mp.getModelId());
            chatModel.chatStreamModel(new ChatModel.ChatStreamModelDTO(
                    mp.getModelId(),
                    request.getQuery(),
                    systemPrompt,
                    chunk -> {
                        try {
                            emitter.send(chunk, MediaType.TEXT_PLAIN);
                        } catch (IOException e) {
                            log.error("Writing to stream failed", e);
                        }
                    },
                    emitter::complete,
                    emitter::completeWithError
            ));

        } catch (Exception e) {
            log.error("RAG QA stream error: query='{}', ragId={}", request.getQuery(), request.getRagId(), e);
            try {
                emitter.send("Error: " + e.getMessage(), MediaType.TEXT_PLAIN);
            } catch (IOException ex) {
                log.error("Failed to write error message", ex);
            }
            emitter.completeWithError(e);
        }
    }

    private String buildDocumentsText(List<SearchResult> results) {
        if (results.isEmpty()) {
            return "(No relevant reference found)";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < results.size(); i++) {
            sb.append(String.format("[%d] %s\n\n", i + 1, results.get(i).getContent()));
        }
        return sb.toString().trim();
    }

    private String buildSystemPrompt(String promptTemplate, String documentsText) {
        if (!StringUtils.hasText(promptTemplate)) {
            return "Please answer user questions based on the following references:\n\n" + documentsText;
        }
        return promptTemplate.replace("<Documents>", documentsText);
    }
}
