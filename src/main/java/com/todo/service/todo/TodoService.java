package com.todo.service.todo;

import com.github.pagehelper.PageInfo;
import com.todo.dto.todo.TodoRequest;
import com.todo.vo.todo.TodoVO;

import java.time.LocalDate;

public interface TodoService {

    PageInfo<TodoVO> listTodos(String email, LocalDate date, Long categoryId, Integer status, int pageNum, int pageSize);

    TodoVO createTodo(String email, TodoRequest request);

    TodoVO updateTodo(String email, Long id, TodoRequest request);

    void deleteTodo(String email, Long id);

    TodoVO toggleTodo(String email, Long id);
}
