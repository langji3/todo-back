package com.todo.service.todo.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.todo.common.api.ResponseCode;
import com.todo.common.exception.BusinessException;
import com.todo.dto.todo.TodoRequest;
import com.todo.entity.todo.Todo;
import com.todo.entity.user.User;
import com.todo.mapper.todo.TodoMapper;
import com.todo.service.todo.TodoService;
import com.todo.service.user.UserCacheService;
import com.todo.vo.todo.TodoVO;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TodoServiceImpl implements TodoService {

    private final TodoMapper todoMapper;
    private final UserCacheService userCacheService;
    private final ModelMapper modelMapper;

    @Override
    public PageInfo<TodoVO> listTodos(String email, LocalDate date, Long categoryId, Integer status, int pageNum, int pageSize) {
        User user = getUserByEmail(email);

        PageHelper.startPage(pageNum, pageSize);
        LambdaQueryWrapper<Todo> wrapper = new LambdaQueryWrapper<Todo>()
                .eq(Todo::getUserId, user.getId());
        if (date != null) {
            wrapper.eq(Todo::getDate, date);
        }
        if (categoryId != null) {
            wrapper.eq(Todo::getCategoryId, categoryId);
        }
        if (status != null) {
            wrapper.eq(Todo::getStatus, status);
        }
        wrapper.orderByDesc(Todo::getCreateTime);

        List<Todo> todos = todoMapper.selectList(wrapper);
        PageInfo<Todo> todoPageInfo = new PageInfo<>(todos);

        List<TodoVO> voList = todos.stream().map(this::toVO).toList();
        PageInfo<TodoVO> voPageInfo = new PageInfo<>(voList);
        voPageInfo.setTotal(todoPageInfo.getTotal());
        voPageInfo.setPages(todoPageInfo.getPages());
        return voPageInfo;
    }

    @Override
    @Transactional
    public TodoVO createTodo(String email, TodoRequest request) {
        User user = getUserByEmail(email);
        Todo todo = new Todo();
        todo.setTitle(request.getTitle());
        todo.setDescription(request.getDescription());
        todo.setDate(request.getDate() != null ? request.getDate() : LocalDate.now());
        todo.setCategoryId(request.getCategoryId());
        todo.setStatus(request.getStatus() != null ? request.getStatus() : 0);
        todo.setUserId(user.getId());
        todoMapper.insert(todo);
        return toVO(todo);
    }

    @Override
    @Transactional
    public TodoVO updateTodo(String email, Long id, TodoRequest request) {
        User user = getUserByEmail(email);
        Todo todo = getTodoByIdAndUserId(id, user.getId());

        if (request.getTitle() != null) {
            todo.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            todo.setDescription(request.getDescription());
        }
        if (request.getDate() != null) {
            todo.setDate(request.getDate());
        }
        if (request.getCategoryId() != null) {
            todo.setCategoryId(request.getCategoryId());
        }
        if (request.getStatus() != null) {
            todo.setStatus(request.getStatus());
        }

        todoMapper.updateById(todo);
        return toVO(todo);
    }

    @Override
    @Transactional
    public void deleteTodo(String email, Long id) {
        User user = getUserByEmail(email);
        Todo todo = getTodoByIdAndUserId(id, user.getId());
        todoMapper.deleteById(todo.getId());
    }

    @Override
    @Transactional
    public TodoVO toggleTodo(String email, Long id) {
        User user = getUserByEmail(email);
        Todo todo = getTodoByIdAndUserId(id, user.getId());
        todo.setStatus(todo.getStatus() == 1 ? 0 : 1);
        todoMapper.updateById(todo);
        return toVO(todo);
    }

    private User getUserByEmail(String email) {
        User user = userCacheService.getByEmail(email);
        if (user == null) {
            throw new BusinessException(ResponseCode.UNAUTHORIZED, "用户不存在");
        }
        return user;
    }

    private Todo getTodoByIdAndUserId(Long id, Long userId) {
        Todo todo = todoMapper.selectOne(
                new LambdaQueryWrapper<Todo>()
                        .eq(Todo::getId, id)
                        .eq(Todo::getUserId, userId));
        if (todo == null) {
            throw new BusinessException(ResponseCode.NOT_FOUND, "待办不存在");
        }
        return todo;
    }

    private TodoVO toVO(Todo todo) {
        TodoVO vo = new TodoVO();
        vo.setId(todo.getId());
        vo.setTitle(todo.getTitle());
        vo.setDescription(todo.getDescription());
        vo.setDate(todo.getDate());
        vo.setCategoryId(todo.getCategoryId());
        vo.setStatus(todo.getStatus());
        vo.setCompleted(todo.getStatus() != null && todo.getStatus() == 1);
        vo.setCreatedAt(todo.getCreateTime());
        return vo;
    }
}
