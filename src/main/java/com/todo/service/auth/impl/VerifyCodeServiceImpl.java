package com.todo.service.auth.impl;

import com.todo.common.api.ResponseCode;
import com.todo.common.constant.RedisConstant;
import com.todo.common.exception.BusinessException;
import com.todo.service.RedisService;
import com.todo.service.auth.EmailService;
import com.todo.service.auth.VerifyCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerifyCodeServiceImpl implements VerifyCodeService {

    private final RedisService redisService;
    private final EmailService emailService;
    private final StringRedisTemplate redisTemplate;

    private final SecureRandom random = new SecureRandom();

    @Override
    public void sendCode(String email, String ip) {
        String intervalKey = RedisConstant.VERIFY_INTERVAL_PREFIX + email;
        String dailyEmailKey = RedisConstant.VERIFY_DAILY_PREFIX + email;
        String dailyIpKey = RedisConstant.VERIFY_DAILY_IP_PREFIX + ip;

        // Pipeline 读取：间隔检查 + 每日限制
        List<Object> reads = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            connection.keyCommands().exists(intervalKey.getBytes());
            connection.stringCommands().get(dailyEmailKey.getBytes());
            connection.stringCommands().get(dailyIpKey.getBytes());
            return null;
        });

        Boolean intervalExists = (Boolean) reads.get(0);
        if (Boolean.TRUE.equals(intervalExists)) {
            throw new BusinessException(ResponseCode.BAD_REQUEST, "发送过于频繁，请稍后再试");
        }

        int emailCount = reads.get(1) != null ? Integer.parseInt((String) reads.get(1)) : 0;
        if (emailCount >= RedisConstant.VERIFY_DAILY_EMAIL_LIMIT) {
            throw new BusinessException(ResponseCode.BAD_REQUEST, "该邮箱今日发送次数已达上限");
        }

        int ipCount = reads.get(2) != null ? Integer.parseInt((String) reads.get(2)) : 0;
        if (ipCount >= RedisConstant.VERIFY_DAILY_IP_LIMIT) {
            throw new BusinessException(ResponseCode.BAD_REQUEST, "该IP今日发送次数已达上限");
        }

        // Pipeline 写入：验证码 + 间隔 + 计数器
        String code = generateCode();
        long ttlSeconds = remainingSecondsToday();

        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            connection.stringCommands().set(
                    (RedisConstant.VERIFY_CODE_PREFIX + email).getBytes(),
                    code.getBytes());
            connection.keyCommands().expire(
                    (RedisConstant.VERIFY_CODE_PREFIX + email).getBytes(),
                    RedisConstant.VERIFY_CODE_EXPIRATION);
            connection.stringCommands().set(
                    intervalKey.getBytes(), "1".getBytes());
            connection.keyCommands().expire(
                    intervalKey.getBytes(), RedisConstant.VERIFY_INTERVAL_EXPIRATION);
            connection.stringCommands().incr(dailyEmailKey.getBytes());
            connection.stringCommands().incr(dailyIpKey.getBytes());
            return null;
        });

        // 首次计数的 key 需要设置过期时间
        if (emailCount == 0) {
            redisService.expire(dailyEmailKey, ttlSeconds, TimeUnit.SECONDS);
        }
        if (ipCount == 0) {
            redisService.expire(dailyIpKey, ttlSeconds, TimeUnit.SECONDS);
        }

        emailService.sendVerifyCode(email, code);
    }

    @Override
    public void verifyCode(String email, String code) {
        String failKey = RedisConstant.VERIFY_FAIL_PREFIX + email;
        String failCountStr = redisService.get(failKey);
        int failCount = failCountStr != null ? Integer.parseInt(failCountStr) : 0;

        if (failCount >= RedisConstant.VERIFY_MAX_FAIL_COUNT) {
            redisService.delete(RedisConstant.VERIFY_CODE_PREFIX + email);
            redisService.delete(failKey);
            throw new BusinessException(ResponseCode.BAD_REQUEST, "验证码已失效，请重新获取");
        }

        String codeKey = RedisConstant.VERIFY_CODE_PREFIX + email;
        String storedCode = redisService.get(codeKey);

        if (storedCode == null) {
            throw new BusinessException(ResponseCode.BAD_REQUEST, "验证码已过期，请重新获取");
        }

        if (!storedCode.equals(code)) {
            incrementCounter(failKey, RedisConstant.VERIFY_FAIL_EXPIRATION);
            throw new BusinessException(ResponseCode.BAD_REQUEST, "验证码错误");
        }

        redisService.delete(codeKey);
        redisService.delete(failKey);
    }

    private String generateCode() {
        int code = 1000 + random.nextInt(9000);
        return String.valueOf(code);
    }

    private void incrementCounter(String key, long ttlSeconds) {
        Long count = redisService.increment(key);
        if (count != null && count == 1L) {
            redisService.expire(key, ttlSeconds, TimeUnit.SECONDS);
        }
    }

    private long remainingSecondsToday() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endOfDay = now.toLocalDate().atTime(LocalTime.MAX);
        return Duration.between(now, endOfDay).getSeconds();
    }
}
