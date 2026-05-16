package com.todo;

import com.todo.service.auth.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class EmailTest {

    @Autowired
    private EmailService emailService;

    @Test
    void sendVerifyCode() {
        emailService.sendVerifyCode("1455052189@qq.com", "1234");
    }
}
