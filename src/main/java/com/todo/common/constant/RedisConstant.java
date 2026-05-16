package com.todo.common.constant;

public class RedisConstant {

    public static final String JWT_TOKEN_PREFIX = "jwt:";
    public static final long JWT_TOKEN_EXPIRATION = 86400L;

    public static final String VERIFY_CODE_PREFIX = "verify:code:";
    public static final String VERIFY_INTERVAL_PREFIX = "verify:interval:";
    public static final String VERIFY_DAILY_PREFIX = "verify:daily:";
    public static final String VERIFY_DAILY_IP_PREFIX = "verify:daily:ip:";
    public static final String VERIFY_FAIL_PREFIX = "verify:fail:";

    public static final long VERIFY_CODE_EXPIRATION = 600L;
    public static final long VERIFY_INTERVAL_EXPIRATION = 60L;
    public static final long VERIFY_FAIL_EXPIRATION = 600L;
    public static final int VERIFY_DAILY_EMAIL_LIMIT = 5;
    public static final int VERIFY_DAILY_IP_LIMIT = 10;
    public static final int VERIFY_MAX_FAIL_COUNT = 5;

    private RedisConstant() {
    }
}
