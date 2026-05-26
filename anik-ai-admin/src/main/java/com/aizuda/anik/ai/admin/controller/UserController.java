package com.aizuda.anik.ai.admin.controller;

import com.aizuda.anik.ai.common.model.Result;
import com.aizuda.anik.ai.admin.security.annotation.LoginRequired;
import com.aizuda.anik.ai.admin.vo.PageResult;
import com.aizuda.anik.ai.admin.enums.RoleEnum;
import com.aizuda.anik.ai.admin.vo.AuthorizeRequestVO;
import com.aizuda.anik.ai.admin.vo.ChangePasswordRequestVO;
import com.aizuda.anik.ai.admin.vo.LoginRequestVO;
import com.aizuda.anik.ai.admin.vo.LoginResponseVO;
import com.aizuda.anik.ai.admin.vo.ResetPasswordRequestVO;
import com.aizuda.anik.ai.admin.vo.UserCreateRequestVO;
import com.aizuda.anik.ai.admin.vo.UserInfoVO;
import com.aizuda.anik.ai.admin.vo.UserQueryVO;
import com.aizuda.anik.ai.admin.vo.UserUpdateRequestVO;
import com.aizuda.anik.ai.admin.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * usercontroller
 *
 * @author openanik
 * @date 2025-07-12
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    /**
     * user login (account password)
     */
    @PostMapping("/login")
    public LoginResponseVO login(@RequestBody @Valid LoginRequestVO requestVO) {
        return userService.login(requestVO);
    }

    /**
     * user registration
     */
    @PostMapping("/register")
    public Result<String> register(@RequestBody @Valid LoginRequestVO requestVO) {
        userService.register(requestVO);
        return Result.ok("Registration successful", "Registration successful");
    }

    /**
     * Get user information
     */
    @GetMapping
    @LoginRequired(role = RoleEnum.USER)
    public UserInfoVO getUserInfo() {
       return userService.getUserInfo();
    }

    /**
     * Create user (administrator)
     */
    @PostMapping
    @LoginRequired(role = RoleEnum.ADMIN)
    public Result<String> createUser(@RequestBody @Valid UserCreateRequestVO requestVO) {
        userService.createUser(requestVO);
        return Result.ok("Created successfully", "Created successfully");
    }

    /**
     * Get user list (administrator)
     */
    @GetMapping("/page/list")
    @LoginRequired(role = RoleEnum.ADMIN)
    public PageResult<List<UserInfoVO>> getPageUserList(UserQueryVO queryVO) {
       return userService.getPageUserList(queryVO);
    }

    /**
     * Authorized user (administrator)
     */
    @PostMapping("/authorize/code")
    @LoginRequired(role = RoleEnum.ADMIN)
    public Result<String> authorizeUser(@RequestBody @Valid AuthorizeRequestVO requestVO) {
        userService.authorizeUser(requestVO);
        return Result.ok("Authorization successful", "Authorization successful");
    }

    /**
     * renewUser info (administrator) - unified renew interface
     */
    @PutMapping("/{id}")
    @LoginRequired(role = RoleEnum.ADMIN)
    public Result<String> updateUser(@PathVariable("id") Long id, @RequestBody @Valid UserUpdateRequestVO requestVO) {
        userService.updateUser(id, requestVO);
        return Result.ok("Update successful", "Update successful");
    }

    /**
     * renewuser role (administrator)
     */
    @PutMapping("/{id}/role")
    @LoginRequired(role = RoleEnum.ADMIN)
    public Result<String> updateUserRole(@PathVariable("id") Long id, @RequestParam Integer role) {
        userService.updateUserRole(id, role);
        return Result.ok("Update successful", "Update successful");
    }

    /**
     * User can change password by himself (need to verify old password)
     */
    @PutMapping("/password")
    @LoginRequired(role = RoleEnum.USER)
    public Result<String> changePassword(@RequestBody @Valid ChangePasswordRequestVO requestVO) {
        userService.changePassword(requestVO);
        return Result.ok("Modification successful", "Password changed successfully, please log in again");
    }

    /**
     * Reset user password (administrator)
     */
    @PutMapping("/{id}/password")
    @LoginRequired(role = RoleEnum.ADMIN)
    public Result<String> resetUserPassword(@PathVariable("id") Long id, @RequestBody ResetPasswordRequestVO requestVO) {
        userService.resetUserPassword(id, requestVO.getPassword());
        return Result.ok("Reset successful", "Reset successful");
    }

    /**
     * deleteuser（administrator）
     */
    @DeleteMapping("/{id}")
    @LoginRequired(role = RoleEnum.ADMIN)
    public Result<String> deleteUser(@PathVariable("id") Long id) {
        userService.deleteUser(id);
        return Result.ok("Delete successfully", "Delete successfully");
    }
}
