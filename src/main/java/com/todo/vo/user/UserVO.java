package com.todo.vo.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UserVO {

    private Long id;

    @JsonProperty("name")
    private String nickname;

    private String email;

    private String avatar;
}
