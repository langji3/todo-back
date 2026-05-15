package com.todo.service.category;

import com.todo.dto.category.CategoryRequest;
import com.todo.vo.category.CategoryVO;

import java.util.List;

public interface CategoryService {

    List<CategoryVO> listCategories(String email);

    CategoryVO createCategory(String email, CategoryRequest request);

    CategoryVO updateCategory(String email, Long id, CategoryRequest request);

    void deleteCategory(String email, Long id);
}
