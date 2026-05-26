package com.aizuda.anik.ai.openapi.interceptor;

import cn.hutool.core.util.StrUtil;
import com.aizuda.anik.ai.common.execption.AnikAiAuthenticationException;
import com.aizuda.anik.ai.openapi.security.OpenApiSessionUtils;
import com.aizuda.anik.ai.persistence.app.mapper.AppMapper;
import com.aizuda.anik.ai.persistence.app.po.AppPO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class OpenApiAuthInterceptor implements HandlerInterceptor {

    public static final String HEADER_APP_ID = "Anik-Ai-App-Id";
    public static final String HEADER_TOKEN = "Anik-Ai-Token";

    private static final int APP_STATUS_ACTIVE = 1;

    private final LoadingCache<String, AppPO> appCache;

    public OpenApiAuthInterceptor(AppMapper appMapper) {
        this.appCache = CacheBuilder.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build(new CacheLoader<>() {
                    @Override
                    public AppPO load(String appId) {
                        AppPO app = appMapper.selectOne(
                                new LambdaQueryWrapper<AppPO>().eq(AppPO::getAppId, appId));
                        if (app == null) {
                            throw new AnikAiAuthenticationException("App does not exist: {}", appId);
                        }
                        return app;
                    }
                });
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String appId = request.getHeader(HEADER_APP_ID);
        String token = request.getHeader(HEADER_TOKEN);

        if (StrUtil.isBlank(appId) || StrUtil.isBlank(token)) {
            throw new AnikAiAuthenticationException("Authentication information is missing, please provide Anik-Ai-App-Id and Anik-Ai-Token");
        }

        AppPO app;
        try {
            app = appCache.getUnchecked(appId);
        } catch (Exception e) {
            throw new AnikAiAuthenticationException("Application authentication failed: {}", appId);
        }

        if (!Objects.equals(app.getStatus(), APP_STATUS_ACTIVE)) {
            throw new AnikAiAuthenticationException("App disabled: {}", appId);
        }

        if (!token.equals(app.getToken())) {
            throw new AnikAiAuthenticationException("Token verification failed");
        }

        OpenApiSessionUtils.set(OpenApiSessionUtils.OpenApiSession.builder()
                .appId(appId)
                .appDbId(app.getId())
                .build());

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        OpenApiSessionUtils.clear();
    }
}
