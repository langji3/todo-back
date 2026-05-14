package com.todo.common.constant;

public class SecurityConstant {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";

    public static final String[] PERMIT_ALL_PATHS = {
            "/api/users/register",
            "/api/users/login",
            "/swagger-docs/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/error"
    };

    private SecurityConstant() {
    }
}
