package com.todo.controller.user;

import com.github.pagehelper.PageInfo;
import com.todo.common.api.BaseResponse;
import com.todo.dto.user.*;
import com.todo.service.user.UserService;
import com.todo.vo.user.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "用户管理")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer")
public class UserController {

    private final UserService userService;

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public BaseResponse<UserVO> register(@Valid @RequestBody UserRegisterRequest request) {
        return BaseResponse.success(userService.register(request));
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public BaseResponse<String> login(@Valid @RequestBody UserLoginRequest request) {
        return BaseResponse.success(userService.login(request));
    }

    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public BaseResponse<Void> logout(HttpServletRequest request) {
        String username = request.getUserPrincipal().getName();
        userService.logout(username);
        return BaseResponse.success();
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/me")
    public BaseResponse<UserVO> getCurrentUser(HttpServletRequest request) {
        String username = request.getUserPrincipal().getName();
        return BaseResponse.success(userService.getUserByUsername(username));
    }

    @Operation(summary = "根据ID获取用户")
    @GetMapping("/{id}")
    public BaseResponse<UserVO> getUserById(@PathVariable Long id) {
        return BaseResponse.success(userService.getUserById(id));
    }

    @Operation(summary = "获取用户列表")
    @GetMapping
    public BaseResponse<PageInfo<UserVO>> listUsers(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return BaseResponse.success(userService.listUsers(pageNum, pageSize));
    }

    @Operation(summary = "更新用户信息")
    @PutMapping("/{id}")
    public BaseResponse<UserVO> updateUser(@PathVariable Long id,
                                           @Valid @RequestBody UserUpdateRequest request) {
        return BaseResponse.success(userService.updateUser(id, request));
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public BaseResponse<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return BaseResponse.success();
    }
}
