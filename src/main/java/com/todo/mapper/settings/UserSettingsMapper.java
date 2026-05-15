package com.todo.mapper.settings;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.todo.entity.settings.UserSettings;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserSettingsMapper extends BaseMapper<UserSettings> {
}
