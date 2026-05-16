package com.todo.service.auth.impl;

import com.todo.common.api.ResponseCode;
import com.todo.common.constant.RedisConstant;
import com.todo.common.exception.BusinessException;
import com.todo.service.RedisService;
import com.todo.service.auth.EmailService;
import com.todo.service.auth.VerifyCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerifyCodeServiceImpl implements VerifyCodeService {

    private final RedisService redisService;
    private final EmailService emailService;

    private final SecureRandom random = new SecureRandom();

    @Override
    public void sendCode(String email, String ip) {
        checkInterval(email);
        checkDailyLimit(RedisConstant.VERIFY_DAILY_PREFIX + email,
                RedisConstant.VERIFY_DAILY_EMAIL_LIMIT, "该邮箱今日发送次数已达上限");
        checkDailyLimit(RedisConstant.VERIFY_DAILY_IP_PREFIX + ip,
                RedisConstant.VERIFY_DAILY_IP_LIMIT, "该IP今日发送次数已达上限");

        String code = generateCode();
        redisService.set(RedisConstant.VERIFY_CODE_PREFIX + email, code,
                RedisConstant.VERIFY_CODE_EXPIRATION, TimeUnit.SECONDS);

        redisService.set(RedisConstant.VERIFY_INTERVAL_PREFIX + email, "1",
                RedisConstant.VERIFY_INTERVAL_EXPIRATION, TimeUnit.SECONDS);

        long ttlSeconds = remainingSecondsToday();
        incrementCounter(RedisConstant.VERIFY_DAILY_PREFIX + email, ttlSeconds);
        incrementCounter(RedisConstant.VERIFY_DAILY_IP_PREFIX + ip, ttlSeconds);

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

    private void checkInterval(String email) {
        if (Boolean.TRUE.equals(redisService.hasKey(RedisConstant.VERIFY_INTERVAL_PREFIX + email))) {
            throw new BusinessException(ResponseCode.BAD_REQUEST, "发送过于频繁，请稍后再试");
        }
    }

    private void checkDailyLimit(String key, int limit, String message) {
        String countStr = redisService.get(key);
        int count = countStr != null ? Integer.parseInt(countStr) : 0;
        if (count >= limit) {
            throw new BusinessException(ResponseCode.BAD_REQUEST, message);
        }
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
