package com.todo.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateRequest {

    @Size(max = 64, message = "昵称长度不能超过64个字符")
    private String name;

    @Email(message = "邮箱格式不正确")
    private String email;
}
