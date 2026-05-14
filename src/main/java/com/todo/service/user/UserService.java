package com.todo.service.user;

import com.github.pagehelper.PageInfo;
import com.todo.dto.user.*;
import com.todo.vo.user.UserVO;

public interface UserService {

    UserVO register(UserRegisterRequest request);

    String login(UserLoginRequest request);

    void logout(String username);

    UserVO getUserById(Long id);

    UserVO getUserByUsername(String username);

    PageInfo<UserVO> listUsers(int pageNum, int pageSize);

    UserVO updateUser(Long id, UserUpdateRequest request);

    void deleteUser(Long id);
}
