package com.todo.service.auth.impl;

import com.todo.service.auth.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendVerifyCode(String toEmail, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("TODO APP - 邮箱验证码");
            helper.setText(buildHtml(code), true);
            mailSender.send(message);
            log.info("验证码邮件已发送至 {}", toEmail);
        } catch (MessagingException e) {
            log.error("发送验证码邮件失败: {}", e.getMessage(), e);
            throw new RuntimeException("邮件发送失败", e);
        }
    }

    private String buildHtml(String code) {
        return """
                <!DOCTYPE html>
                <html lang="zh-CN">
                <head>
                  <meta charset="utf-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <title>Taskflow - 验证码</title>
                </head>
                <body style="margin:0;padding:0;">
                <table width="100%" cellpadding="0" cellspacing="0" border="0" style="margin:0;padding:0;background-color:#F7F7F5;">
                  <tr>
                    <td align="center" style="padding:48px 24px;">
                      <table width="480" cellpadding="0" cellspacing="0" border="0" style="max-width:480px;width:100%;background-color:#FFFFFF;border-radius:12px;overflow:hidden;box-shadow:0 1px 4px rgba(0,0,0,0.06);">
                        <tr>
                          <td style="height:4px;background-color:#4A8FD9;font-size:0;line-height:0;">&nbsp;</td>
                        </tr>
                        <tr>
                          <td style="padding:48px 40px 12px 40px;">
                            <h1 style="margin:0 0 40px 0;font-family:'Helvetica Neue',Arial,'PingFang SC','Microsoft YaHei',sans-serif;font-size:22px;font-weight:700;color:#4A8FD9;text-align:center;letter-spacing:-0.3px;">TODO</h1>
                            <p style="margin:0 0 28px 0;font-family:'Helvetica Neue',Arial,'PingFang SC','Microsoft YaHei',sans-serif;font-size:15px;color:#444444;line-height:1.75;">
                              你好，<br><br>欢迎注册 <strong style="color:#4A8FD9;">TODO</strong>。请使用以下验证码完成账号注册：
                            </p>
                            <table width="100%" cellpadding="0" cellspacing="0" border="0">
                              <tr>
                                <td align="center" style="background-color:#f1f6fc;border:1px solid #deebf8;border-radius:10px;padding:28px 20px;">
                                  <p style="margin:0 0 12px 0;font-family:'Helvetica Neue',Arial,sans-serif;font-size:11px;color:#AAAAAA;text-transform:uppercase;letter-spacing:3px;font-weight:600;">验证码 / CODE</p>
                                  <p style="margin:0;font-family:'Courier New','Menlo','Consolas',monospace;font-size:34px;font-weight:700;color:#4A8FD9;letter-spacing:10px;">""" + code + """
                                  </p>
                                </td>
                              </tr>
                            </table>
                            <p style="margin:24px 0 0 0;font-family:'Helvetica Neue',Arial,'PingFang SC','Microsoft YaHei',sans-serif;font-size:13px;color:#999999;line-height:1.7;">
                              验证码有效期为 <strong>10 分钟</strong>。如非本人操作，请忽略此邮件。
                            </p>
                          </td>
                        </tr>
                        <tr>
                          <td style="padding:8px 40px 36px 40px;">
                            <hr style="border:none;border-top:1px solid #F0F0F0;margin:0 0 20px 0;">
                            <p style="margin:0;font-family:'Helvetica Neue',Arial,'PingFang SC','Microsoft YaHei',sans-serif;font-size:11px;color:#CCCCCC;text-align:center;line-height:1.9;">
                              此邮件由 Taskflow 系统自动发送，请勿直接回复<br>
                              &copy; 2026 Taskflow. All rights reserved.
                            </p>
                          </td>
                        </tr>
                      </table>
                    </td>
                  </tr>
                </table>
                </body>
                </html>
                """;
    }
}
