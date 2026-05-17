package com.todo.service.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.todo.entity.user.User;
import com.todo.mapper.user.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCacheService {

    private final UserMapper userMapper;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String CACHE_PREFIX = "user:cache:";
    private static final long CACHE_TTL_SECONDS = 1800L;

    public User getByEmail(String email) {
        String key = CACHE_PREFIX + email;
        try {
            String cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                return objectMapper.readValue(cached, User.class);
            }
        } catch (Exception e) {
            log.warn("读取用户缓存失败: {}", e.getMessage());
        }

        User user = userMapper.selectByEmail(email);
        if (user != null) {
            try {
                redisTemplate.opsForValue().set(key,
                        objectMapper.writeValueAsString(user),
                        CACHE_TTL_SECONDS, TimeUnit.SECONDS);
            } catch (Exception e) {
                log.warn("写入用户缓存失败: {}", e.getMessage());
            }
        }
        return user;
    }

    public void evict(String email) {
        redisTemplate.delete(CACHE_PREFIX + email);
    }
}
