package com.aianik.anik.ai.admin.service.rag;

import cn.hutool.core.util.IdUtil;
import com.aianik.anik.ai.admin.service.rag.preview.UploadPreviewState;
import com.aianik.anik.ai.admin.service.rag.preview.UploadPreviewState.UploadPreviewItemState;
import com.aianik.anik.ai.admin.vo.rag.RagDocumentResponseVO;
import com.aianik.anik.ai.admin.vo.rag.RagDocumentUploadRequestVO;
import com.aianik.anik.ai.admin.vo.rag.UploadCommitItemVO;
import com.aianik.anik.ai.admin.vo.rag.UploadCommitRequestVO;
import com.aianik.anik.ai.admin.vo.rag.UploadCommitResultVO;
import com.aianik.anik.ai.admin.vo.rag.UploadPreviewItemVO;
import com.aianik.anik.ai.admin.vo.rag.UploadPreviewResultVO;
import com.aianik.anik.ai.common.execption.AnikAiCommonException;
import com.aianik.anik.ai.common.util.JsonUtil;
import com.aianik.anik.ai.features.rag.dedup.DedupResult;
import com.aianik.anik.ai.features.rag.dedup.DocumentDedupChecker;
import com.aianik.anik.ai.features.rag.dedup.UploadDecision;
import com.aianik.anik.ai.features.rag.dedup.UploadDecisionType;
import com.aianik.anik.ai.features.rag.enums.DedupAction;
import com.aianik.anik.ai.features.rag.enums.DedupStrategy;
import com.aianik.anik.ai.features.rag.enums.DocumentSourceTypeEnum;
import com.aianik.anik.ai.features.rag.util.ContentHashUtil;
import com.aianik.anik.ai.features.resource.ResourceService;
import com.aianik.anik.ai.features.resource.enums.ResourceBizTypeEnum;
import com.aianik.anik.ai.persistence.rag.po.RagPO;
import com.aianik.anik.ai.persistence.rag.po.RagDocumentPO;
import com.aianik.anik.ai.persistence.resource.po.ResourcePO;
import com.aianik.anik.ai.persistence.security.UserSessionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Knowledge base upload preview-submit two-stage process
 * <p>
 * - preview: drop temporary resources, generate each file according to policy decision making, and write token into memory
 * - Commit: Recheck the decision making (TOCTOU) in the knowledge base lock, and execute the storage according to the user's final selection.
 * - cancel: delete temporary resources and expired tokens
 *
 * @author openanik
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentUploadPreviewService {

    private static final long PREVIEW_TTL_MILLIS = 30L * 60L * 1000L; //30 minutes

    // Memory Token cache: token -> {state, expiresAt}
    private final Map<String, TokenEntry> tokenCache = new ConcurrentHashMap<>();

    private final RagDocumentService ragDocumentService;
    private final ResourceService resourceService;
    
    private static class TokenEntry {
        UploadPreviewState state;
        long expiresAt;

        TokenEntry(UploadPreviewState state) {
            this.state = state;
            this.expiresAt = System.currentTimeMillis() + PREVIEW_TTL_MILLIS;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiresAt;
        }
    }

    // ──────────────────────────────────── preview ────────────────────────────────────

    public UploadPreviewResultVO preview(MultipartFile[] files, RagDocumentUploadRequestVO request) {
        if (files == null || files.length == 0) {
            throw new AnikAiCommonException("Please select the file to upload");
        }
        RagPO knowledge = ragDocumentService.requireKnowledgeOrThrow(request.getRagId());
        DedupStrategy strategy = ragDocumentService.resolveStrategy(request, knowledge);
        DedupAction action = ragDocumentService.resolveAction(request, knowledge);
        DocumentDedupChecker checker = ragDocumentService.getDedupChecker();
        Long userId = currentUserIdOrNull();

        List<UploadPreviewItemState> stateItems = new ArrayList<>(files.length);
        List<UploadPreviewItemVO> voItems = new ArrayList<>(files.length);

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            String fileName = file.getOriginalFilename();
            String fileType = extractFileType(fileName);
            byte[] bytes;
            try {
                bytes = file.getBytes();
            } catch (IOException e) {
                throw new AnikAiCommonException("Failed to read uploaded file:" + fileName);
            }
            String contentHash = ContentHashUtil.sha256Hex(bytes);

            ResourcePO resource = resourceService.upload(
                    new ByteArrayInputStream(bytes),
                    fileName,
                    bytes.length,
                    ResourceBizTypeEnum.DOCUMENT_PREVIEW.getValue(),
                    knowledge.getId(),
                    userId
            );

            DedupResult dedup = checker.check(knowledge.getId(), fileName, contentHash, strategy);
            UploadDecision decision = checker.decide(dedup, action);

            stateItems.add(UploadPreviewItemState.builder()
                    .tempResourceId(resource.getId())
                    .fileName(fileName)
                    .fileType(fileType)
                    .sourceType(DocumentSourceTypeEnum.UPLOAD.getValue())
                    .fileSize((long) bytes.length)
                    .contentHash(contentHash)
                    .decision(decision.getType().name())
                    .matchType(decision.getMatchType().name())
                    .conflictDocumentId(decision.getConflict() != null ? decision.getConflict().getId() : null)
                    .build());

            voItems.add(UploadPreviewItemVO.builder()
                    .tempResourceId(resource.getId())
                    .fileName(fileName)
                    .fileType(fileType)
                    .fileSize((long) bytes.length)
                    .contentHash(contentHash)
                    .decision(decision.getType().name())
                    .matchType(decision.getMatchType().name())
                    .conflictDocumentId(decision.getConflict() != null ? decision.getConflict().getId() : null)
                    .conflictDocumentName(decision.getConflict() != null ? decision.getConflict().getName() : null)
                    .rejectReason(decision.getType() == UploadDecisionType.REJECT
                            ? buildConflictReason(decision) : null)
                    .build());
        }

        if (stateItems.isEmpty()) {
            throw new AnikAiCommonException("No files to process");
        }

        String token = IdUtil.fastSimpleUUID();
        UploadPreviewState state = UploadPreviewState.builder()
                .ragId(knowledge.getId())
                .userId(userId)
                .dedupStrategy(strategy.getCode())
                .dedupAction(action.getCode())
                .items(stateItems)
                .build();
        tokenCache.put(token, new TokenEntry(state));

        return UploadPreviewResultVO.builder()
                .previewToken(token)
                .ragId(knowledge.getId())
                .items(voItems)
                .build();
    }

    // ──────────────────────────────────── commit ────────────────────────────────────

    public UploadCommitResultVO commit(UploadCommitRequestVO request) {
        UploadPreviewState state = loadState(request.getPreviewToken());
        RagPO knowledge = ragDocumentService.requireKnowledgeOrThrow(state.getRagId());

        // Use local locks instead of distributed locks (sufficient in single-machine scenarios)
        synchronized (ragDocumentService.uploadLockKey(knowledge.getId())) {
            UploadCommitResultVO result = doCommit(knowledge, state, request);
            
            //Delete the token only after the entire batch of commits is completed to avoid users being unable to retry when partial failures occur.
            if (Boolean.FALSE.equals(result.getConflictChanged())) {
                tokenCache.remove(request.getPreviewToken());
            }
            return result;
        }
    }

    private UploadCommitResultVO doCommit(RagPO knowledge, UploadPreviewState state,
                                          UploadCommitRequestVO request) {
        Map<Long, UploadPreviewItemState> stateByResource = new HashMap<>();
        for (UploadPreviewItemState item : state.getItems()) {
            stateByResource.put(item.getTempResourceId(), item);
        }
        DocumentDedupChecker checker = ragDocumentService.getDedupChecker();
        DedupStrategy strategy = DedupStrategy.fromCode(state.getDedupStrategy());

        boolean conflictChanged = false;
        List<RagDocumentResponseVO> committed = new ArrayList<>();

        for (UploadCommitItemVO ui : request.getItems()) {
            UploadPreviewItemState item = stateByResource.get(ui.getTempResourceId());
            if (item == null) {
                throw new AnikAiCommonException("The preview data has expired, please re-upload it.");
            }
            UploadDecisionType userChoice = parseDecision(ui.getDecision());

            // TOCTOU re-determination
            DedupResult fresh = checker.check(knowledge.getId(), item.getFileName(),
                    item.getContentHash(), strategy);
            if (isConflictChanged(item, fresh, userChoice)) {
                conflictChanged = true;
                committed.add(buildConflictChangedResponse(item, fresh));
                continue;
            }

            committed.add(executeOne(knowledge, item, fresh, userChoice));
        }

        return UploadCommitResultVO.builder()
                .conflictChanged(conflictChanged)
                .items(committed)
                .build();
    }

    /**
     * Determine whether the "latest conflict status" has changed from the status that the user based on when previewing.
     * If either situation is met, the conflict will be deemed to have changed and the user will need to reconfirm.
     */
    private boolean isConflictChanged(UploadPreviewItemState item, DedupResult fresh,
                                      UploadDecisionType userChoice) {
        boolean previewHadConflict = item.getConflictDocumentId() != null;
        boolean freshHasConflict = fresh.isHit();

        // Is the conflict reversed?
        if (previewHadConflict != freshHasConflict) {
            return true;
        }
        //user selected NEW but there is still a conflict
        if (userChoice == UploadDecisionType.NEW && freshHasConflict) {
            return true;
        }
        //The user selected SKIP/OVERWRITE but there is currently no conflict.
        if (userChoice != UploadDecisionType.NEW && !freshHasConflict) {
            return true;
        }
        // There is still a conflict but the target document has changed
        if (freshHasConflict && fresh.getConflict() != null
                && !fresh.getConflict().getId().equals(item.getConflictDocumentId())) {
            return true;
        }
        return false;
    }

    private RagDocumentResponseVO executeOne(RagPO knowledge, UploadPreviewItemState item,
                                             DedupResult fresh, UploadDecisionType userChoice) {
        RagDocumentPO conflict = fresh.getConflict();
        UploadDecision decision = new UploadDecision(userChoice, fresh.getMatchType(), conflict);

        return switch (userChoice) {
            case NEW -> {
                resourceService.updateBizType(item.getTempResourceId(),
                        ResourceBizTypeEnum.DOCUMENT.getValue());
                yield ragDocumentService.persistDocumentRow(knowledge, item.getFileName(),
                        item.getFileType(), item.getSourceType(), item.getContentHash(),
                        item.getTempResourceId(), decision);
            }
            case SKIP -> {
                //Skip: The temporary resource is no longer needed, delete it directly
                resourceService.delete(item.getTempResourceId());
                yield buildSkipResponse(item, conflict);
            }
            case OVERWRITE -> {
                if (conflict != null) {
                    ragDocumentService.cleanupDocument(conflict.getId());
                }
                resourceService.updateBizType(item.getTempResourceId(),
                        ResourceBizTypeEnum.DOCUMENT.getValue());
                RagDocumentResponseVO vo = ragDocumentService.persistDocumentRow(knowledge,
                        item.getFileName(), item.getFileType(), item.getSourceType(),
                        item.getContentHash(), item.getTempResourceId(), decision);
                vo.setConflictDocumentId(conflict != null ? conflict.getId() : null);
                yield vo;
            }
            case REJECT -> throw new AnikAiCommonException("REJECT decisions cannot be submitted at commit time");
        };
    }

    // ──────────────────────────────────── cancel ────────────────────────────────────

    public void cancel(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        TokenEntry entry = tokenCache.remove(token);
        if (entry != null && entry.state != null && entry.state.getItems() != null) {
            for (UploadPreviewItemState item : entry.state.getItems()) {
                try {
                    resourceService.delete(item.getTempResourceId());
                } catch (Exception e) {
                    log.warn("Failed to delete preview resource: {}", item.getTempResourceId(), e);
                }
            }
        }
    }

    // ─────────────────────────────────── Regularly clean up expired tokens ───────────────────────────────────

    @Scheduled(fixedDelay = 60_000) //Check every 1 minute
    public void cleanupExpiredTokens() {
        Iterator<Map.Entry<String, TokenEntry>> iter = tokenCache.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, TokenEntry> entry = iter.next();
            if (entry.getValue().isExpired()) {
                iter.remove();
                log.debug("Cleaned up expired preview token: {}", entry.getKey());
            }
        }
    }

    // ──────────────────────────────────── helpers ────────────────────────────────────

    private UploadPreviewState loadState(String token) {
        TokenEntry entry = tokenCache.get(token);
        if (entry == null || entry.isExpired()) {
            throw new AnikAiCommonException("The preview data has expired or does not exist, please upload again");
        }
        UploadPreviewState state = entry.state;
        if (state == null || state.getItems() == null || state.getItems().isEmpty()) {
            throw new AnikAiCommonException("The preview data is invalid, please upload again");
        }
        return state;
    }

    private RagDocumentResponseVO buildSkipResponse(UploadPreviewItemState item, RagDocumentPO conflict) {
        return RagDocumentResponseVO.builder()
                .ragId(conflict != null ? conflict.getRagId() : null)
                .name(item.getFileName())
                .fileType(item.getFileType())
                .sourceType(item.getSourceType())
                .fileSize(item.getFileSize())
                .decision(UploadDecisionType.SKIP.name())
                .matchType(item.getMatchType())
                .conflictDocumentId(conflict != null ? conflict.getId() : null)
                .build();
    }

    private RagDocumentResponseVO buildConflictChangedResponse(UploadPreviewItemState item, DedupResult fresh) {
        return RagDocumentResponseVO.builder()
                .name(item.getFileName())
                .fileType(item.getFileType())
                .sourceType(item.getSourceType())
                .fileSize(item.getFileSize())
                .decision("CONFLICT_CHANGED")
                .matchType(fresh.getMatchType().name())
                .conflictDocumentId(fresh.getConflict() != null ? fresh.getConflict().getId() : null)
                .errorMsg("The conflict status has changed, please preview again before submitting.")
                .build();
    }

    private String buildConflictReason(UploadDecision decision) {
        RagDocumentPO conflict = decision.getConflict();
        String name = conflict == null ? "" : "：" + conflict.getName();
        return switch (decision.getMatchType()) {
            case BY_NAME -> "A document with the same name already exists" + name;
            case BY_CONTENT -> "A document with the same content already exists" + name;
            case BOTH -> "A document with the same name and content already exists" + name;
            case NONE -> "Upload rejected" + name;
        };
    }

    private UploadDecisionType parseDecision(String value) {
        try {
            return UploadDecisionType.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new AnikAiCommonException("Illegal decision types:" + value);
        }
    }

    private Long currentUserIdOrNull() {
        try {
            return UserSessionUtils.currentUserSession().getId();
        } catch (Exception ignored) {
            return null;
        }
    }

    private String extractFileType(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "txt";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }
}
