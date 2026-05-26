package com.aizuda.anik.ai.features.rag.strategy.rerank;

import com.aizuda.anik.ai.common.model.ModelCallException;
import com.aizuda.anik.ai.model.model.Model;
import com.aizuda.anik.ai.model.model.ModelFactory;
import com.aizuda.anik.ai.model.model.rerank.RerankModel;
import com.aizuda.anik.ai.model.model.rerank.RerankResponse;
import com.aizuda.anik.ai.common.dto.rag.SearchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Default reordering service implementation
 * Get RerankModel through ModelFactory for real cross-encoder rearrangement
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultRerankService implements RerankService {

    private final ModelFactory modelFactory;

    @Override
    public List<SearchResult> rerank(String query, List<SearchResult> candidates,
                                     int rerankOutputTopN, Long rerankModelId) {
        if (candidates.isEmpty()) {
            return candidates;
        }

        if (rerankModelId == null) {
            log.warn("rerankModelId is null, falling back to score-based sorting");
            return sortByScore(candidates, rerankOutputTopN);
        }

        try {
            Model model = modelFactory.getModel(rerankModelId);
            if (!(model instanceof RerankModel rerankModel)) {
                log.error("Model {} is not a RerankModel", rerankModelId);
                return sortByScore(candidates, rerankOutputTopN);
            }

            // Extract the content of candidate documents
            List<String> documents = candidates.stream()
                    .map(SearchResult::getContent)
                    .map(c -> c != null ? c : "")
                    .toList();

            RerankResponse response = rerankModel.rerank(
                    new RerankModel.RerankDTO(query, documents, rerankOutputTopN));

            if (response == null || response.getResults() == null || response.getResults().isEmpty()) {
                log.warn("Rerank API returned no results, falling back to original ordering by score");
                return sortByScore(candidates, rerankOutputTopN);
            }

            // Map back to SearchResultDTO based on the returned index + score (in the order returned by the API)
            List<SearchResult> reranked = new ArrayList<>(response.getResults().size());
            for (RerankResponse.RerankResult result : response.getResults()) {
                if (result.getIndex() >= 0 && result.getIndex() < candidates.size()) {
                    SearchResult dto = candidates.get(result.getIndex());
                    dto.setScore(result.getScore());
                    reranked.add(dto);
                }
            }
            if (reranked.isEmpty()) {
                return sortByScore(candidates, rerankOutputTopN);
            }
            return reranked;

        } catch (ModelCallException e) {
            log.error("Rerank failed: {}", e.getMessage());
            return sortByScore(candidates, rerankOutputTopN);

        } catch (Exception e) {
            log.error("Rerank failed, falling back to score-based sorting", e);
            return sortByScore(candidates, rerankOutputTopN);
        }
    }

    private List<SearchResult> sortByScore(List<SearchResult> candidates, int topN) {
        return candidates.stream()
                .sorted(Comparator.comparing(SearchResult::getScore).reversed())
                .limit(topN)
                .toList();
    }
}
