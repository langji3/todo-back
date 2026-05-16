package com.todo.service.auth;

public interface EmailService {

    void sendVerifyCode(String toEmail, String code);
}
