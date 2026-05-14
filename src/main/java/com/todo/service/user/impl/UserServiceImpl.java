package com.todo.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.todo.common.api.ResponseCode;
import com.todo.common.exception.BusinessException;
import com.todo.common.exception.UnauthorizedException;
import com.todo.common.security.JwtTokenProvider;
import com.todo.common.security.RedisTokenService;
import com.todo.dto.user.*;
import com.todo.entity.user.User;
import com.todo.mapper.user.UserMapper;
import com.todo.service.user.UserService;
import com.todo.vo.user.UserVO;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTokenService redisTokenService;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public UserVO register(UserRegisterRequest request) {
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));
        if (count > 0) {
            throw new BusinessException(ResponseCode.BAD_REQUEST, "用户名已存在");
        }

        User user = modelMapper.map(request, User.class);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");
        userMapper.insert(user);

        return toVO(user);
    }

    @Override
    public String login(UserLoginRequest request) {
        User user = userMapper.selectByUsername(request.getUsername());
        if (user == null) {
            throw new UnauthorizedException("用户名或密码错误");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("用户名或密码错误");
        }

        String token = jwtTokenProvider.generateToken(user.getUsername());
        redisTokenService.storeToken(user.getUsername(), token);
        return token;
    }

    @Override
    public void logout(String username) {
        redisTokenService.removeToken(username);
    }

    @Override
    public UserVO getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResponseCode.NOT_FOUND, "用户不存在");
        }
        return toVO(user);
    }

    @Override
    public UserVO getUserByUsername(String username) {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new BusinessException(ResponseCode.NOT_FOUND, "用户不存在");
        }
        return toVO(user);
    }

    @Override
    public PageInfo<UserVO> listUsers(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<User> users = userMapper.selectList(new LambdaQueryWrapper<>());
        PageInfo<User> userPageInfo = new PageInfo<>(users);

        List<UserVO> voList = users.stream().map(this::toVO).toList();
        PageInfo<UserVO> voPageInfo = new PageInfo<>(voList);
        voPageInfo.setTotal(userPageInfo.getTotal());
        voPageInfo.setPages(userPageInfo.getPages());
        return voPageInfo;
    }

    @Override
    @Transactional
    public UserVO updateUser(Long id, UserUpdateRequest request) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResponseCode.NOT_FOUND, "用户不存在");
        }

        if (StringUtils.isNotBlank(request.getNickname())) {
            user.setNickname(request.getNickname());
        }
        if (StringUtils.isNotBlank(request.getEmail())) {
            user.setEmail(request.getEmail());
        }
        if (StringUtils.isNotBlank(request.getPhone())) {
            user.setPhone(request.getPhone());
        }
        userMapper.updateById(user);

        return toVO(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResponseCode.NOT_FOUND, "用户不存在");
        }
        userMapper.deleteById(id);
        redisTokenService.removeToken(user.getUsername());
    }

    private UserVO toVO(User user) {
        return modelMapper.map(user, UserVO.class);
    }
}
