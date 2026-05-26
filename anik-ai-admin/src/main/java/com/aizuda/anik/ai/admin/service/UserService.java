package com.aizuda.anik.ai.admin.service;

import cn.hutool.core.util.StrUtil;
import com.aizuda.anik.ai.persistence.admin.mapper.UserMapper;
import com.aizuda.anik.ai.admin.vo.PageResult;
import com.aizuda.anik.ai.admin.dto.AudienceDTO;
import com.aizuda.anik.ai.admin.enums.RoleEnum;
import com.aizuda.anik.ai.persistence.admin.po.UserPO;
import com.aizuda.anik.ai.admin.vo.AuthorizeRequestVO;
import com.aizuda.anik.ai.admin.vo.ChangePasswordRequestVO;
import com.aizuda.anik.ai.admin.vo.LoginRequestVO;
import com.aizuda.anik.ai.admin.vo.LoginResponseVO;
import com.aizuda.anik.ai.admin.vo.UserCreateRequestVO;
import com.aizuda.anik.ai.admin.vo.UserInfoVO;
import com.aizuda.anik.ai.admin.vo.UserQueryVO;
import com.aizuda.anik.ai.admin.vo.UserUpdateRequestVO;
import com.aizuda.anik.ai.persistence.security.UserSessionUtils;
import com.aizuda.anik.ai.common.execption.AnikAiCommonException;
import com.aizuda.anik.ai.common.util.JsonUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * User service
 *
 * @author openanik
 * @date 2025-07-12
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserMapper userMapper;
    
    private static final String PASSWORD_SALT = "anik_ai_2026";

    /**
     * Generate random string (for JWT signing key)
     */
    private String generateCode(int length) {
        String chars = "0123456789qwertyuiopasdfghjklzxcvbnmQWERTYUIOPLKJHGFDSAZXCVBNM";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * Encrypted password (SHA-256)
     */
    private String encryptPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((password + PASSWORD_SALT).getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new AnikAiCommonException("Password encryption failed");
        }
    }

    /**
     * Verify password
     */
    private boolean verifyPassword(String rawPassword, String encryptedPassword) {
        return encryptPassword(rawPassword).equals(encryptedPassword);
    }

    /**
     * Create user (administrator)
     */
    public void createUser(UserCreateRequestVO requestVO) {
        if (StrUtil.isBlank(requestVO.getUsername())) {
            throw new AnikAiCommonException("Username cannot be empty");
        }
        
        if (StrUtil.isBlank(requestVO.getPassword())) {
            throw new AnikAiCommonException("Password cannot be empty");
        }

        if (requestVO.getPassword().length() < 6) {
            throw new AnikAiCommonException("The password length cannot be less than 6 characters");
        }

        if (requestVO.getRole() == null) {
            throw new AnikAiCommonException("Role cannot be empty");
        }

        if (!RoleEnum.getEnumTypeMap().containsKey(requestVO.getRole())) {
            throw new AnikAiCommonException("Invalid role");
        }

        // Check if username already exists
        UserPO existUser = userMapper.selectOne(
            new LambdaQueryWrapper<UserPO>().eq(UserPO::getUsername, requestVO.getUsername())
        );
        
        if (existUser != null) {
            throw new AnikAiCommonException("Username already exists");
        }

        // If an email is provided, check whether the email already exists
        if (StrUtil.isNotBlank(requestVO.getEmail())) {
            UserPO existEmailUser = userMapper.selectOne(
                new LambdaQueryWrapper<UserPO>().eq(UserPO::getEmail, requestVO.getEmail())
            );
            
            if (existEmailUser != null) {
                throw new AnikAiCommonException("Email already exists");
            }
        }

        //Create new user
        UserPO userPO = new UserPO();
        userPO.setRole(requestVO.getRole());
        userPO.setUsername(requestVO.getUsername());
        //If an email address is provided, use the email address, otherwise use the username.
        if (StrUtil.isNotBlank(requestVO.getEmail())) {
            userPO.setEmail(requestVO.getEmail());
        }
        userPO.setPassword(encryptPassword(requestVO.getPassword()));
        userMapper.insert(userPO);
        
        log.info("The administrator successfully created a new user: username={}, role={}", requestVO.getUsername(), requestVO.getRole());
    }

    /**
     * user registration
     */
    public void register(LoginRequestVO requestVO) {
        if (StrUtil.isBlank(requestVO.getUsername())) {
            throw new AnikAiCommonException("Account cannot be empty");
        }
        
        if (StrUtil.isBlank(requestVO.getPassword())) {
            throw new AnikAiCommonException("Password cannot be empty");
        }

        if (requestVO.getPassword().length() < 6) {
            throw new AnikAiCommonException("The password length cannot be less than 6 characters");
        }

        //Check if username already exists (email/login name, any duplicate will not register)
        UserPO existUser = userMapper.selectOne(
            new LambdaQueryWrapper<UserPO>()
                .eq(UserPO::getEmail, requestVO.getUsername())
                .or()
                .eq(UserPO::getUsername, requestVO.getUsername())
        );
        
        if (existUser != null) {
            throw new AnikAiCommonException("Account already exists");
        }

        //Create a new user (the login account also writes username and email, consistent with the login query)
        UserPO userPO = new UserPO();
        userPO.setRole(RoleEnum.USER.getRoleId());
        userPO.setUsername(requestVO.getUsername());
        userPO.setEmail(requestVO.getUsername());
        userPO.setPassword(encryptPassword(requestVO.getPassword()));
        userMapper.insert(userPO);
        
        log.info("New user registration successful: {}", requestVO.getUsername());
    }

    /**
     * user login (account password)
     */
    public LoginResponseVO login(LoginRequestVO requestVO) {
        if (StrUtil.isBlank(requestVO.getUsername())) {
            throw new AnikAiCommonException("Account cannot be empty");
        }
        
        if (StrUtil.isBlank(requestVO.getPassword())) {
            throw new AnikAiCommonException("Password cannot be empty");
        }

        //Query user
        UserPO userPO = userMapper.selectOne(
            new LambdaQueryWrapper<UserPO>().eq(UserPO::getUsername, requestVO.getUsername())
        );
        
        if (Objects.isNull(userPO)) {
            throw new AnikAiCommonException("Incorrect account or password");
        }

        // Verify password
        if (!verifyPassword(requestVO.getPassword(), userPO.getPassword())) {
            throw new AnikAiCommonException("Incorrect account or password");
        }

        // Generate token (use authorization code as JWT signing key)
        AudienceDTO audienceDTO = new AudienceDTO();
        audienceDTO.setUsername(userPO.getUsername());
        
        LoginResponseVO loginResponseVO = new LoginResponseVO();
        loginResponseVO.setUsername(userPO.getUsername());
        loginResponseVO.setToken(getToken(audienceDTO, userPO.getPassword()));


        log.info("User login successful: {}", requestVO.getUsername());
        return loginResponseVO;
    }

    /**
     * Generate Token (valid for 24 hours)
     */
    private String getToken(AudienceDTO audienceDTO, String pwd) {
        return JWT.create()
                .withExpiresAt(new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7)))
                .withAudience(JsonUtil.toJsonString(audienceDTO))
                .sign(Algorithm.HMAC256(pwd));
    }

    /**
     * Get current User info
     */
    public UserInfoVO getUserInfo() {
        UserPO userPO = UserSessionUtils.currentUserSession();
        UserInfoVO userInfoVO = new UserInfoVO();
        userInfoVO.setId(userPO.getId());
        userInfoVO.setUsername(userPO.getUsername());
        userInfoVO.setEmail(userPO.getEmail());
        userInfoVO.setRole(userPO.getRole());
        userInfoVO.setRoleName(resolveRoleName(userPO.getRole()));
        return userInfoVO;
    }

    /**
     * Get user paging list (administrator)
     */
    public PageResult<List<UserInfoVO>> getPageUserList(UserQueryVO queryVO) {
        PageDTO<UserPO> userPageDTO = new PageDTO<>(queryVO.getPage(), queryVO.getSize());
        String keyword = StrUtil.trim(queryVO.getEmail());

        LambdaQueryWrapper<UserPO> wrapper = new LambdaQueryWrapper<UserPO>().orderByDesc(UserPO::getId);
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(UserPO::getEmail, keyword).or().like(UserPO::getUsername, keyword));
        }

        PageDTO<UserPO> pageDTO = userMapper.selectPage(userPageDTO, wrapper);

        PageResult<List<UserInfoVO>> pageResult = new PageResult<>();

        List<UserInfoVO> infoVOS = pageDTO.getRecords().stream().map(userPO -> {
            UserInfoVO userInfoVO = new UserInfoVO();
            userInfoVO.setId(userPO.getId());
            userInfoVO.setUsername(userPO.getUsername());
            userInfoVO.setEmail(userPO.getEmail());
            userInfoVO.setRole(userPO.getRole());
            userInfoVO.setRoleName(resolveRoleName(userPO.getRole()));
            userInfoVO.setCreateDt(userPO.getCreateDt());
            userInfoVO.setUpdateDt(userPO.getUpdateDt());
            return userInfoVO;
        }).toList();

        pageResult.setData(infoVOS);
        pageResult.setTotal(pageDTO.getTotal());
        pageResult.setSize(pageDTO.getSize());
        pageResult.setPage(pageDTO.getCurrent());
        return pageResult;
    }

    /**
     * Authorized user (administrator)
     */
    public void authorizeUser(AuthorizeRequestVO requestVO) {
        String account = StrUtil.trim(requestVO.getEmail());
        UserPO userPO = userMapper.selectOne(
            new LambdaQueryWrapper<UserPO>().eq(UserPO::getEmail, account)
        );
        if (Objects.isNull(userPO)) {
            userPO = userMapper.selectOne(
                new LambdaQueryWrapper<UserPO>().eq(UserPO::getUsername, account)
            );
        }

        if (Objects.isNull(userPO)) {
            throw new AnikAiCommonException("User does not exist");
        }

        userMapper.updateById(userPO);
        
        log.info("Authorized user successfully: {}, quota: {}, number of days: {}", requestVO.getEmail(), requestVO.getTotals(), requestVO.getDays());
    }

    /**
     * renewUser info (administrator) - unified renew interface
     */
    public void updateUser(Long id, UserUpdateRequestVO requestVO) {
        //1. Verify that user exists
        UserPO userPO = userMapper.selectById(id);
        if (Objects.isNull(userPO)) {
            throw new AnikAiCommonException("User does not exist");
        }
        
        // 2. Verify role validity
        if (!RoleEnum.getEnumTypeMap().containsKey(requestVO.getRole())) {
            throw new AnikAiCommonException("Invalid role");
        }
        
        //3. If renewing the mailbox, check whether the mailbox has been used by other users.
        if (StrUtil.isNotBlank(requestVO.getEmail()) 
            && !requestVO.getEmail().equals(userPO.getEmail())) {
            UserPO existUser = userMapper.selectOne(
                new LambdaQueryWrapper<UserPO>()
                    .eq(UserPO::getEmail, requestVO.getEmail())
                    .ne(UserPO::getId, id)
            );
            if (Objects.nonNull(existUser)) {
                throw new AnikAiCommonException("Email is already in use");
            }
            userPO.setEmail(requestVO.getEmail());
        }
        
        //4. renew role
        userPO.setRole(requestVO.getRole());
        
        //5. If a password is provided, renew the password
        if (StrUtil.isNotBlank(requestVO.getPassword())) {
            if (requestVO.getPassword().length() < 6) {
                throw new AnikAiCommonException("The password length cannot be less than 6 characters");
            }
            userPO.setPassword(encryptPassword(requestVO.getPassword()));
        }
        
        //6. saverenew
        userMapper.updateById(userPO);
        
        log.info("Successfully updated user information: id={}, role={}, email={}", id, requestVO.getRole(), requestVO.getEmail());
    }

    /**
     * renewuser role (administrator)
     */
    public void updateUserRole(Long id, Integer role) {
        if (!RoleEnum.getEnumTypeMap().containsKey(role)) {
            throw new AnikAiCommonException("Invalid role");
        }

        UserPO userPO = userMapper.selectById(id);
        if (Objects.isNull(userPO)) {
            throw new AnikAiCommonException("User does not exist");
        }

        userPO.setRole(role);
        userMapper.updateById(userPO);
        
        log.info("Successfully updated user role: id={}, role={}", id, role);
    }

    /**
     * User can change password by himself (need to verify old password)
     */
    public void changePassword(ChangePasswordRequestVO request) {
        UserPO currentUser = UserSessionUtils.currentUserSession();
        if (!verifyPassword(request.getOldPassword(), currentUser.getPassword())) {
            throw new AnikAiCommonException("The old password is incorrect");
        }
        if (request.getOldPassword().equals(request.getNewPassword())) {
            throw new AnikAiCommonException("The new password cannot be the same as the old password");
        }
        UserPO update = new UserPO();
        update.setId(currentUser.getId());
        update.setPassword(encryptPassword(request.getNewPassword()));
        userMapper.updateById(update);
        log.info("User changed password successfully: id={}, username={}", currentUser.getId(), currentUser.getUsername());
    }

    /**
     * Reset user password (administrator)
     */
    public void resetUserPassword(Long id, String newPassword) {
        if (StrUtil.isBlank(newPassword)) {
            throw new AnikAiCommonException("Password cannot be empty");
        }

        if (newPassword.length() < 6) {
            throw new AnikAiCommonException("The password length cannot be less than 6 characters");
        }

        UserPO userPO = userMapper.selectById(id);
        if (Objects.isNull(userPO)) {
            throw new AnikAiCommonException("User does not exist");
        }

        userPO.setPassword(encryptPassword(newPassword));
        userMapper.updateById(userPO);
        
        log.info("User password reset successfully: id={}, username={}", id, userPO.getUsername());
    }

    /**
     * deleteuser（administrator）
     */
    public void deleteUser(Long id) {
        UserPO userPO = userMapper.selectById(id);
        if (Objects.isNull(userPO)) {
            throw new AnikAiCommonException("User does not exist");
        }

        //deleteadministrator is not allowed
        if (RoleEnum.isAdmin(userPO.getRole())) {
            throw new AnikAiCommonException("Cannot delete administrator account");
        }

        userMapper.deleteById(id);
        
        log.info("User deleted successfully: id={}, email={}", id, userPO.getEmail());
    }

    private static String resolveRoleName(Integer roleId) {
        if (roleId == null) {
            return "-";
        }
        RoleEnum e = RoleEnum.getEnumTypeMap().get(roleId);
        if (e == null) {
            return "-";
        }
        return switch (e) {
            case ADMIN -> "administrator";
            case USER -> "Ordinary user";
        };
    }
}
