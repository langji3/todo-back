package com.todo.entity.settings;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_user_settings")
public class UserSettings {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Boolean darkMode;

    private Boolean notifications;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
