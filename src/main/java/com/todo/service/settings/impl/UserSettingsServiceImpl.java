package com.todo.service.settings.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.todo.common.api.ResponseCode;
import com.todo.common.exception.BusinessException;
import com.todo.dto.user.SettingsRequest;
import com.todo.entity.settings.UserSettings;
import com.todo.entity.user.User;
import com.todo.mapper.settings.UserSettingsMapper;
import com.todo.mapper.user.UserMapper;
import com.todo.service.settings.UserSettingsService;
import com.todo.vo.user.SettingsVO;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserSettingsServiceImpl implements UserSettingsService {

    private final UserSettingsMapper userSettingsMapper;
    private final UserMapper userMapper;
    private final ModelMapper modelMapper;

    @Override
    public SettingsVO getSettings(String email) {
        User user = userMapper.selectByEmail(email);
        if (user == null) {
            throw new BusinessException(ResponseCode.NOT_FOUND, "用户不存在");
        }

        UserSettings settings = userSettingsMapper.selectOne(
                new LambdaQueryWrapper<UserSettings>().eq(UserSettings::getUserId, user.getId()));
        if (settings == null) {
            settings = createDefaultSettings(user.getId());
        }

        return toVO(settings);
    }

    @Override
    @Transactional
    public SettingsVO updateSettings(String email, SettingsRequest request) {
        User user = userMapper.selectByEmail(email);
        if (user == null) {
            throw new BusinessException(ResponseCode.NOT_FOUND, "用户不存在");
        }

        UserSettings settings = userSettingsMapper.selectOne(
                new LambdaQueryWrapper<UserSettings>().eq(UserSettings::getUserId, user.getId()));
        if (settings == null) {
            settings = createDefaultSettings(user.getId());
        }

        if (request.getDarkMode() != null) {
            settings.setDarkMode(request.getDarkMode());
        }
        if (request.getNotifications() != null) {
            settings.setNotifications(request.getNotifications());
        }

        userSettingsMapper.updateById(settings);
        return toVO(settings);
    }

    private UserSettings createDefaultSettings(Long userId) {
        UserSettings settings = new UserSettings();
        settings.setUserId(userId);
        settings.setDarkMode(false);
        settings.setNotifications(true);
        userSettingsMapper.insert(settings);
        return settings;
    }

    private SettingsVO toVO(UserSettings settings) {
        return modelMapper.map(settings, SettingsVO.class);
    }
}
