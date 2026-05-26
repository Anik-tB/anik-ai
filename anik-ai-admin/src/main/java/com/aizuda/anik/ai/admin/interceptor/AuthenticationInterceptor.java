package com.aizuda.anik.ai.admin.interceptor;

import cn.hutool.core.util.StrUtil;
import com.aizuda.anik.ai.common.execption.AnikAiAuthenticationException;
import com.aizuda.anik.ai.common.util.JsonUtil;
import com.aizuda.anik.ai.admin.security.annotation.LoginRequired;
import com.aizuda.anik.ai.persistence.admin.mapper.UserMapper;
import com.aizuda.anik.ai.admin.dto.AudienceDTO;
import com.aizuda.anik.ai.admin.enums.RoleEnum;
import com.aizuda.anik.ai.persistence.admin.po.UserPO;
import com.aizuda.anik.ai.persistence.security.UserSessionUtils;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * system login authentication interceptor
 *
 * @author: byteblogs
 * @date:2023-04-26 12:52
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class AuthenticationInterceptor implements HandlerInterceptor {
    public static final String AUTHENTICATION = "Anik-Ai-Auth";
    private final UserMapper userMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // If it is not mapped to a method, pass it directly
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        Method method = handlerMethod.getMethod();
        //Check if there is LoginRequired annotation, if not, skip authentication
        if (!method.isAnnotationPresent(LoginRequired.class)) {
            return true;
        }

        LoginRequired loginRequired = method.getAnnotation(LoginRequired.class);
        if (!loginRequired.required()) {
            return true;
        }

        // Get the token from the request header
        String token = request.getHeader(AUTHENTICATION);
        if (StrUtil.isBlank(token)) {
            throw new AnikAiAuthenticationException("Not logged in or your login has expired");
        }

        //Verify token and get user information
        UserPO userPO = verifyTokenAndGetUser(token);

        // Check role permissions
        RoleEnum requiredRole = loginRequired.role();
        if (requiredRole == RoleEnum.ADMIN && !RoleEnum.isAdmin(userPO.getRole())) {
            throw new AnikAiAuthenticationException("Insufficient permissions");
        }

        //Store User info to ThreadLocal
        UserSessionUtils.setUserSession(userPO);
        
        return true;
    }

    /**
     * Verify token and get user information
     */
    private UserPO verifyTokenAndGetUser(String token) {
        try {
            //Decode tokenGet user information
            DecodedJWT jwt = JWT.decode(token);
            List<String> audience = jwt.getAudience();
            
            if (audience == null || audience.isEmpty()) {
                throw new AnikAiAuthenticationException("Token format error");
            }

            AudienceDTO audienceDTO = JsonUtil.parseObject(audience.get(0), AudienceDTO.class);
            if (audienceDTO == null || StrUtil.isBlank(audienceDTO.getUsername())) {
                throw new AnikAiAuthenticationException("Token format error");
            }

            //Query user
            UserPO userPO = userMapper.selectOne(
                new LambdaQueryWrapper<UserPO>().eq(UserPO::getUsername, audienceDTO.getUsername())
            );

            if (Objects.isNull(userPO)) {
                throw new AnikAiAuthenticationException("User does not exist");
            }

            // Verify token signature (using authorization code)
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(userPO.getPassword())).build();
            verifier.verify(token);

            return userPO;

        } catch (TokenExpiredException e) {
            log.warn("Token has expired: {}", e.getMessage());
            throw new AnikAiAuthenticationException("Login has expired, please log in again");
        } catch (JWTVerificationException e) {
            log.warn("Token verification failed: {}", e.getMessage());
            throw new AnikAiAuthenticationException("Token verification failed");
        } catch (Exception e) {
            log.error("Token parsing exception", e);
            throw new AnikAiAuthenticationException("Authentication failed");
        }
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // You can do some post-processing here
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // Clean up ThreadLocal to prevent memory leaks
        UserSessionUtils.clearUserSession();
    }
}
