package com.todo.service.auth;

public interface VerifyCodeService {

    void sendCode(String email, String ip);

    void verifyCode(String email, String code);
}
