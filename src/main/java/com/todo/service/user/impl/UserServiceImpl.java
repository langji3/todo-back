package com.todo.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.todo.common.api.ResponseCode;
import com.todo.common.exception.BusinessException;
import com.todo.common.exception.UnauthorizedException;
import com.todo.common.security.JwtTokenProvider;
import com.todo.common.security.RedisTokenService;
import com.todo.dto.user.UserLoginRequest;
import com.todo.dto.user.UserRegisterRequest;
import com.todo.dto.user.UserUpdateRequest;
import com.todo.entity.user.User;
import com.todo.mapper.user.UserMapper;
import com.todo.service.auth.VerifyCodeService;
import com.todo.service.user.UserService;
import com.todo.vo.auth.AuthVO;
import com.todo.vo.user.UserVO;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTokenService redisTokenService;
    private final ModelMapper modelMapper;
    private final VerifyCodeService verifyCodeService;

    @Override
    @Transactional
    public AuthVO register(UserRegisterRequest request) {
        verifyCodeService.verifyCode(request.getEmail(), request.getCode());

        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getEmail, request.getEmail()));
        if (count > 0) {
            throw new BusinessException(ResponseCode.BAD_REQUEST, "邮箱已注册");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setNickname(request.getName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(0);
        userMapper.insert(user);

        String token = jwtTokenProvider.generateToken(user.getEmail());
        redisTokenService.storeToken(user.getEmail(), token);

        return new AuthVO(toVO(user), token);
    }

    @Override
    public AuthVO login(UserLoginRequest request) {
        User user = userMapper.selectByEmail(request.getEmail());
        if (user == null) {
            throw new UnauthorizedException("邮箱或密码错误");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("邮箱或密码错误");
        }

        String token = jwtTokenProvider.generateToken(user.getEmail());
        redisTokenService.storeToken(user.getEmail(), token);
        return new AuthVO(toVO(user), token);
    }

    @Override
    public void logout(String email) {
        redisTokenService.removeToken(email);
    }

    @Override
    public UserVO getCurrentUser(String email) {
        User user = userMapper.selectByEmail(email);
        if (user == null) {
            throw new BusinessException(ResponseCode.UNAUTHORIZED, "用户不存在");
        }
        return toVO(user);
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
    @Transactional
    public UserVO updateProfile(String email, UserUpdateRequest request) {
        User user = userMapper.selectByEmail(email);
        if (user == null) {
            throw new BusinessException(ResponseCode.NOT_FOUND, "用户不存在");
        }

        if (StringUtils.hasText(request.getName())) {
            user.setNickname(request.getName());
        }
        if (StringUtils.hasText(request.getEmail()) && !request.getEmail().equals(user.getEmail())) {
            Long count = userMapper.selectCount(
                    new LambdaQueryWrapper<User>().eq(User::getEmail, request.getEmail()));
            if (count > 0) {
                throw new BusinessException(ResponseCode.BAD_REQUEST, "邮箱已被使用");
            }
            user.setEmail(request.getEmail());
        }
        userMapper.updateById(user);
        return toVO(user);
    }

    @Override
    @Transactional
    public String uploadAvatar(String email, String avatarUrl) {
        User user = userMapper.selectByEmail(email);
        if (user == null) {
            throw new BusinessException(ResponseCode.NOT_FOUND, "用户不存在");
        }
        user.setAvatar(avatarUrl);
        userMapper.updateById(user);
        return avatarUrl;
    }

    private UserVO toVO(User user) {
        return modelMapper.map(user, UserVO.class);
    }
}
