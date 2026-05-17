package com.todo.service.category.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.todo.common.api.ResponseCode;
import com.todo.common.exception.BusinessException;
import com.todo.dto.category.CategoryRequest;
import com.todo.entity.category.Category;
import com.todo.entity.todo.Todo;
import com.todo.entity.user.User;
import com.todo.mapper.category.CategoryMapper;
import com.todo.mapper.todo.TodoMapper;
import com.todo.service.category.CategoryService;
import com.todo.service.user.UserCacheService;
import com.todo.vo.category.CategoryVO;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;
    private final UserCacheService userCacheService;
    private final TodoMapper todoMapper;
    private final ModelMapper modelMapper;

    @Override
    public List<CategoryVO> listCategories(String email) {
        User user = getUserByEmail(email);
        List<Category> categories = categoryMapper.selectList(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getUserId, user.getId())
                        .orderByDesc(Category::getCreateTime));
        return categories.stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CategoryVO createCategory(String email, CategoryRequest request) {
        User user = getUserByEmail(email);
        Category category = new Category();
        category.setName(request.getName());
        category.setColor(StringUtils.isNotBlank(request.getColor()) ? request.getColor() : "#6C5CE7");
        category.setUserId(user.getId());
        categoryMapper.insert(category);
        return toVO(category);
    }

    @Override
    @Transactional
    public CategoryVO updateCategory(String email, Long id, CategoryRequest request) {
        User user = getUserByEmail(email);
        Category category = getCategoryByIdAndUserId(id, user.getId());
        if (StringUtils.isNotBlank(request.getName())) {
            category.setName(request.getName());
        }
        if (StringUtils.isNotBlank(request.getColor())) {
            category.setColor(request.getColor());
        }
        categoryMapper.updateById(category);
        return toVO(category);
    }

    @Override
    @Transactional
    public void deleteCategory(String email, Long id) {
        User user = getUserByEmail(email);
        Category category = getCategoryByIdAndUserId(id, user.getId());

        // Check if there are associated non-deleted todos
        Long todoCount = todoMapper.selectCount(
                new LambdaQueryWrapper<Todo>()
                        .eq(Todo::getCategoryId, category.getId())
                        .eq(Todo::getUserId, user.getId()));
        if (todoCount > 0) {
            throw new BusinessException(ResponseCode.BAD_REQUEST, "该分类下存在待办事项，无法删除");
        }

        categoryMapper.deleteById(category.getId());
    }

    private User getUserByEmail(String email) {
        User user = userCacheService.getByEmail(email);
        if (user == null) {
            throw new BusinessException(ResponseCode.UNAUTHORIZED, "用户不存在");
        }
        return user;
    }

    private Category getCategoryByIdAndUserId(Long id, Long userId) {
        Category category = categoryMapper.selectOne(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getId, id)
                        .eq(Category::getUserId, userId));
        if (category == null) {
            throw new BusinessException(ResponseCode.NOT_FOUND, "分类不存在");
        }
        return category;
    }

    private CategoryVO toVO(Category category) {
        return modelMapper.map(category, CategoryVO.class);
    }
}
