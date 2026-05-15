package com.todo.service.settings;

import com.todo.dto.user.SettingsRequest;
import com.todo.vo.user.SettingsVO;

public interface UserSettingsService {

    SettingsVO getSettings(String email);

    SettingsVO updateSettings(String email, SettingsRequest request);
}
