package com.todo.controller.todo;

import com.github.pagehelper.PageInfo;
import com.todo.common.api.BaseResponse;
import com.todo.dto.todo.TodoRequest;
import com.todo.service.todo.TodoService;
import com.todo.vo.todo.TodoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "待办事项管理")
@RestController
@RequestMapping("/api/todos")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer")
public class TodoController {

    private final TodoService todoService;

    @Operation(summary = "获取待办列表")
    @GetMapping
    public BaseResponse<PageInfo<TodoVO>> listTodos(
            HttpServletRequest request,
            @Parameter(description = "日期筛选，格式：yyyy-MM-dd") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @Parameter(description = "分类ID筛选") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "状态筛选 0=待完成 1=已完成") @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        String email = request.getUserPrincipal().getName();
        return BaseResponse.success(todoService.listTodos(email, date, categoryId, status, page, pageSize));
    }

    @Operation(summary = "创建待办")
    @PostMapping
    public BaseResponse<TodoVO> createTodo(HttpServletRequest request,
                                            @Valid @RequestBody TodoRequest todoRequest) {
        String email = request.getUserPrincipal().getName();
        return BaseResponse.success(todoService.createTodo(email, todoRequest));
    }

    @Operation(summary = "更新待办")
    @PutMapping("/{id}")
    public BaseResponse<TodoVO> updateTodo(HttpServletRequest request,
                                            @PathVariable Long id,
                                            @Valid @RequestBody TodoRequest todoRequest) {
        String email = request.getUserPrincipal().getName();
        return BaseResponse.success(todoService.updateTodo(email, id, todoRequest));
    }

    @Operation(summary = "删除待办")
    @DeleteMapping("/{id}")
    public BaseResponse<Void> deleteTodo(HttpServletRequest request,
                                          @PathVariable Long id) {
        String email = request.getUserPrincipal().getName();
        todoService.deleteTodo(email, id);
        return BaseResponse.success();
    }

    @Operation(summary = "切换完成状态")
    @PatchMapping("/{id}/toggle")
    public BaseResponse<TodoVO> toggleTodo(HttpServletRequest request,
                                             @PathVariable Long id) {
        String email = request.getUserPrincipal().getName();
        return BaseResponse.success(todoService.toggleTodo(email, id));
    }
}
