package com.todo.mapper.category;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.todo.entity.category.Category;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}
