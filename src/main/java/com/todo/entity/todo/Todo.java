package com.todo.entity.todo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("t_todo")
public class Todo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String description;

    private LocalDate date;

    private Long categoryId;

    private Integer status;

    private Long userId;

    @TableLogic
    private Integer deleteFlag;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
