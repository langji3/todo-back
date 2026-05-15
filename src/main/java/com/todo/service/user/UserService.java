package com.todo.service.user;

import com.todo.dto.user.UserLoginRequest;
import com.todo.dto.user.UserRegisterRequest;
import com.todo.dto.user.UserUpdateRequest;
import com.todo.vo.auth.AuthVO;
import com.todo.vo.user.UserVO;

public interface UserService {

    AuthVO register(UserRegisterRequest request);

    AuthVO login(UserLoginRequest request);

    void logout(String email);

    UserVO getCurrentUser(String email);

    UserVO getUserById(Long id);

    UserVO updateProfile(String email, UserUpdateRequest request);

    String uploadAvatar(String email, String avatarUrl);
}
