package com.aizuda.anik.ai.persistence.security;

import com.aizuda.anik.ai.common.execption.AnikAiAuthenticationException;
import com.aizuda.anik.ai.persistence.admin.po.UserPO;

/**
 * User session tool class (ThreadLocal, cooperates with authentication interceptor)
 *
 * @author openanik
 * @date 2023-11-22 23:14:53
 * @since 2.4.0
 */
public final class UserSessionUtils {

    private static final ThreadLocal<UserPO> USER_SESSION = new ThreadLocal<>();

    /**
     * Set current user session
     */
    public static void setUserSession(UserPO userPO) {
        USER_SESSION.set(userPO);
    }

    /**
     * Get the current user session
     */
    public static UserPO currentUserSession() {
        UserPO userPO = USER_SESSION.get();
        if (userPO == null) {
            throw new AnikAiAuthenticationException("Not logged in or your login has expired");
        }
        return userPO;
    }

    /**
     * Clear current user session
     */
    public static void clearUserSession() {
        USER_SESSION.remove();
    }

    private UserSessionUtils() {
    }
}
