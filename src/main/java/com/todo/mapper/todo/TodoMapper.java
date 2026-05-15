package com.todo.mapper.todo;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.todo.entity.todo.Todo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TodoMapper extends BaseMapper<Todo> {
}
