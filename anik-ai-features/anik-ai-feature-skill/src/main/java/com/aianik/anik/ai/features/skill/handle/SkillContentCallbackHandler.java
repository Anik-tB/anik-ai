package com.aianik.anik.ai.features.skill.handle;

import com.aianik.anik.ai.common.dto.agent.SkillContentRequest;
import com.aianik.anik.ai.common.grpc.auto.GrpcAnikAiResult;
import com.aianik.anik.ai.common.grpc.constant.UriConstants;
import com.aianik.anik.ai.common.grpc.handler.GrpcHandlerRequest;
import com.aianik.anik.ai.common.grpc.handler.GrpcRequestHandler;
import com.aianik.anik.ai.common.util.JsonUtil;
import com.aianik.anik.ai.persistence.skill.mapper.SkillFileMapper;
import com.aianik.anik.ai.persistence.skill.mapper.SkillMapper;
import com.aianik.anik.ai.persistence.skill.po.SkillFilePO;
import com.aianik.anik.ai.persistence.skill.po.SkillPO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Callback: Get Skill complete content + supporting files
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SkillContentCallbackHandler implements GrpcRequestHandler {

    private final SkillMapper skillMapper;
    private final SkillFileMapper skillFileMapper;

    @Override
    public boolean supports(String uri) {
        return UriConstants.CALLBACK_SKILL_CONTENT.equals(uri);
    }

    @Override
    public GrpcAnikAiResult handle(GrpcHandlerRequest request) {
        try {
            SkillContentRequest req = JsonUtil.parseObject(request.getBody(), SkillContentRequest.class);
            if (req == null || req.getSkillId() == null) {
                return buildError("skillId is required");
            }

            SkillPO skill = skillMapper.selectById(req.getSkillId());
            if (skill == null) {
                return buildError("Skill not found: " + req.getSkillId());
            }

            // Build response
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("skillContent", skill.getSkillContent());
            result.put("version", skill.getVersion());

            // Load support files
            List<SkillFilePO> files = skillFileMapper.selectList(
                    new LambdaQueryWrapper<SkillFilePO>()
                            .eq(SkillFilePO::getSkillId, req.getSkillId()));

            List<Map<String, Object>> fileList = files.stream().map(f -> {
                Map<String, Object> fileMap = new LinkedHashMap<>();
                fileMap.put("filePath", f.getFilePath());
                fileMap.put("content", f.getContent());
                fileMap.put("encoding", f.getEncoding());
                return fileMap;
            }).toList();
            result.put("files", fileList);

            return GrpcAnikAiResult.newBuilder()
                    .setStatus(1).setMessage("OK")
                    .setData(JsonUtil.toJsonString(result))
                    .build();
        } catch (Exception e) {
            log.error("Callback skill content failed", e);
            return buildError(e.getMessage());
        }
    }

    private GrpcAnikAiResult buildError(String msg) {
        return GrpcAnikAiResult.newBuilder().setStatus(0).setMessage(msg).build();
    }
}
