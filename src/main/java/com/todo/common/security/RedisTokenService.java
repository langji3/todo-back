package com.todo.common.security;

import com.todo.common.constant.RedisConstant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisTokenService {

    private final StringRedisTemplate stringRedisTemplate;

    public void storeToken(String username, String token) {
        String key = RedisConstant.JWT_TOKEN_PREFIX + username;
        stringRedisTemplate.opsForValue().set(key, token, RedisConstant.JWT_TOKEN_EXPIRATION, TimeUnit.SECONDS);
    }

    public String getToken(String username) {
        String key = RedisConstant.JWT_TOKEN_PREFIX + username;
        return stringRedisTemplate.opsForValue().get(key);
    }

    public boolean validateToken(String username, String token) {
        String storedToken = getToken(username);
        return storedToken != null && storedToken.equals(token);
    }

    public void removeToken(String username) {
        String key = RedisConstant.JWT_TOKEN_PREFIX + username;
        stringRedisTemplate.delete(key);
    }

    public boolean hasToken(String username) {
        String key = RedisConstant.JWT_TOKEN_PREFIX + username;
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }
}
