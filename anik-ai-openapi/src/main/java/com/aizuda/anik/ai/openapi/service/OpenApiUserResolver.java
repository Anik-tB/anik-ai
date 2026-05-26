package com.aizuda.anik.ai.openapi.service;

import cn.hutool.core.util.StrUtil;
import com.aizuda.anik.ai.common.execption.AnikAiAuthenticationException;
import com.aizuda.anik.ai.persistence.admin.mapper.UserMapper;
import com.aizuda.anik.ai.persistence.admin.po.UserPO;
import com.aizuda.anik.ai.persistence.openapi.mapper.OpenApiUserMapper;
import com.aizuda.anik.ai.persistence.openapi.po.OpenApiUserPO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * OpenAPI user identity resolution (appId + openId -> platform user)
 */
@Component
@RequiredArgsConstructor
public class OpenApiUserResolver {

    private final OpenApiUserMapper openApiUserMapper;
    private final UserMapper userMapper;

    public UserPO resolvePlatformUser(String appId, String openId) {
        if (StrUtil.isBlank(openId)) {
            throw new AnikAiAuthenticationException("openId parameter missing");
        }

        OpenApiUserPO openApiUser = openApiUserMapper.selectOne(
                new LambdaQueryWrapper<OpenApiUserPO>()
                        .eq(OpenApiUserPO::getAppId, appId)
                        .eq(OpenApiUserPO::getOpenId, openId));
        if (openApiUser == null) {
            throw new AnikAiAuthenticationException("Invalid OpenId: {}", openId);
        }

        UserPO userPO = userMapper.selectById(openApiUser.getPlatformUserId());
        if (userPO == null) {
            throw new AnikAiAuthenticationException("The associated user does not exist");
        }
        return userPO;
    }
}
