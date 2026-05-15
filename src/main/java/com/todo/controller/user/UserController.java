package com.todo.controller.user;

import com.todo.common.api.BaseResponse;
import com.todo.common.util.OssUtil;
import com.todo.dto.user.SettingsRequest;
import com.todo.dto.user.UserUpdateRequest;
import com.todo.service.settings.UserSettingsService;
import com.todo.service.user.UserService;
import com.todo.vo.user.SettingsVO;
import com.todo.vo.user.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Tag(name = "用户管理")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer")
public class UserController {

    private final UserService userService;
    private final UserSettingsService userSettingsService;
    private final OssUtil ossUtil;

    @Operation(summary = "更新用户资料")
    @PutMapping("/profile")
    public BaseResponse<UserVO> updateProfile(HttpServletRequest request,
                                               @Valid @RequestBody UserUpdateRequest updateRequest) {
        String email = request.getUserPrincipal().getName();
        return BaseResponse.success(userService.updateProfile(email, updateRequest));
    }

    @Operation(summary = "上传头像")
    @PostMapping("/avatar")
    public BaseResponse<Map<String, String>> uploadAvatar(HttpServletRequest request,
                                                           @RequestParam("file") MultipartFile file) {
        String email = request.getUserPrincipal().getName();
        String avatarUrl = ossUtil.upload(file, email);
        userService.uploadAvatar(email, avatarUrl);
        return BaseResponse.success(Map.of("avatar", avatarUrl));
    }

    @Operation(summary = "获取用户设置")
    @GetMapping("/settings")
    public BaseResponse<SettingsVO> getSettings(HttpServletRequest request) {
        String email = request.getUserPrincipal().getName();
        return BaseResponse.success(userSettingsService.getSettings(email));
    }

    @Operation(summary = "更新用户设置")
    @PatchMapping("/settings")
    public BaseResponse<SettingsVO> updateSettings(HttpServletRequest request,
                                                    @RequestBody SettingsRequest settingsRequest) {
        String email = request.getUserPrincipal().getName();
        return BaseResponse.success(userSettingsService.updateSettings(email, settingsRequest));
    }
}
