package com.todo.controller.auth;

import com.todo.common.api.BaseResponse;
import com.todo.dto.user.UserLoginRequest;
import com.todo.dto.user.UserRegisterRequest;
import com.todo.service.user.UserService;
import com.todo.vo.auth.AuthVO;
import com.todo.vo.user.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "认证管理")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public BaseResponse<AuthVO> register(@Valid @RequestBody UserRegisterRequest request) {
        return BaseResponse.success(userService.register(request));
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public BaseResponse<AuthVO> login(@Valid @RequestBody UserLoginRequest request) {
        return BaseResponse.success(userService.login(request));
    }

    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public BaseResponse<Void> logout(HttpServletRequest request) {
        String email = request.getUserPrincipal().getName();
        userService.logout(email);
        return BaseResponse.success();
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/me")
    public BaseResponse<UserVO> getCurrentUser(HttpServletRequest request) {
        String email = request.getUserPrincipal().getName();
        return BaseResponse.success(userService.getCurrentUser(email));
    }
}
