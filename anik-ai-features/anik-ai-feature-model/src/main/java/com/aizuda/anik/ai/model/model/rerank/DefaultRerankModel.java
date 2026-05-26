package com.aizuda.anik.ai.model.model.rerank;

import com.aizuda.anik.ai.common.log.AnikAiLog;
import com.aizuda.anik.ai.common.model.ModelCallException;
import com.aizuda.anik.ai.common.model.RerankApiClient;
import com.aizuda.anik.ai.model.builder.RerankClientBuilder;
import com.aizuda.anik.ai.model.handle.ModelConfigHandler;
import com.aizuda.anik.ai.model.dto.ModelConfigInfoDTO;
import com.aizuda.anik.ai.model.model.AbstractModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Default rearrange model implementation
 * Refer to DefaultAnikEmbeddingModel design
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultRerankModel extends AbstractModel implements RerankModel {

    private final RerankClientBuilder rerankClientBuilder;
    private final ModelConfigHandler modelConfigHandler;

    @Override
    public boolean supports(String modelKey) {
        return true;
    }

    @Override
    public RerankResponse rerank(RerankDTO dto) throws ModelCallException {
        long startTime = System.currentTimeMillis();

        try {
            String decryptedApiKey = decryptApiKey(modelConfigInfo);

            RerankApiClient client = rerankClientBuilder.buildRerankClient(decryptedApiKey, modelConfigInfo);

            AnikAiLog.LOCAL.info("Rerank with model: {}, config: {}, docCount: {}",
                    modelConfigInfo.getModelName(), modelConfigInfo.getId(), dto.documents().size());

            List<RerankApiClient.RerankResultItem> apiResults = client.rerank(
                    dto.query(), dto.documents(), dto.topN() != null ? dto.topN() : dto.documents().size());

            long duration = System.currentTimeMillis() - startTime;

            // conversion response
            RerankResponse response = new RerankResponse();
            response.setCostTimeMs(duration);
            List<RerankResponse.RerankResult> results = new ArrayList<>(apiResults.size());
            for (RerankApiClient.RerankResultItem item : apiResults) {
                RerankResponse.RerankResult result = new RerankResponse.RerankResult();
                result.setIndex(item.index());
                result.setScore(item.score());
                results.add(result);
            }
            response.setResults(results);

            AnikAiLog.LOCAL.info("Rerank completed: {}, duration: {}ms, resultCount: {}",
                    modelConfigInfo.getId(), duration, results.size());

            return response;

        } catch (ModelCallException e) {
            throw e;
        } catch (Exception e) {
            AnikAiLog.LOCAL.error("Rerank failed for config: {}, error: {}",
                    modelConfigInfo.getId(), e.getMessage(), e);
            throw new ModelCallException(
                    ModelCallException.ErrorCode.MODEL_CALL_FAILED,
                    "Reorder call failed: " + e.getMessage(), e);
        }
    }

    private String decryptApiKey(ModelConfigInfoDTO config) throws ModelCallException {
        try {
            String decryptedKey = modelConfigHandler.decryptApiKey(config.getEncryptedApiKey());
            if (!StringUtils.hasText(decryptedKey)) {
                throw new ModelCallException(ModelCallException.ErrorCode.API_KEY_DECRYPT_FAILED,
                        "API Key decryption failed or does not exist");
            }
            return decryptedKey;
        } catch (ModelCallException e) {
            throw e;
        } catch (Exception e) {
            throw new ModelCallException(ModelCallException.ErrorCode.API_KEY_DECRYPT_FAILED,
                    "API Key decryption failed: " + e.getMessage(), e);
        }
    }
}
