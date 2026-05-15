package com.todo.controller.category;

import com.todo.common.api.BaseResponse;
import com.todo.dto.category.CategoryRequest;
import com.todo.service.category.CategoryService;
import com.todo.vo.category.CategoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "分类管理")
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "获取分类列表")
    @GetMapping
    public BaseResponse<List<CategoryVO>> listCategories(HttpServletRequest request) {
        String email = request.getUserPrincipal().getName();
        return BaseResponse.success(categoryService.listCategories(email));
    }

    @Operation(summary = "创建分类")
    @PostMapping
    public BaseResponse<CategoryVO> createCategory(HttpServletRequest request,
                                                   @Valid @RequestBody CategoryRequest categoryRequest) {
        String email = request.getUserPrincipal().getName();
        return BaseResponse.success(categoryService.createCategory(email, categoryRequest));
    }

    @Operation(summary = "更新分类")
    @PutMapping("/{id}")
    public BaseResponse<CategoryVO> updateCategory(HttpServletRequest request,
                                                   @PathVariable Long id,
                                                   @Valid @RequestBody CategoryRequest categoryRequest) {
        String email = request.getUserPrincipal().getName();
        return BaseResponse.success(categoryService.updateCategory(email, id, categoryRequest));
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    public BaseResponse<Void> deleteCategory(HttpServletRequest request,
                                              @PathVariable Long id) {
        String email = request.getUserPrincipal().getName();
        categoryService.deleteCategory(email, id);
        return BaseResponse.success();
    }
}
