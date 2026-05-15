package com.todo.dto.user;

import lombok.Data;

@Data
public class SettingsRequest {

    private Boolean darkMode;

    private Boolean notifications;
}
