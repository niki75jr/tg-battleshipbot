package com.n75jr.battleship.service;

import java.util.Locale;

public interface SettingsMenuService {

    boolean changeLanguage(Long telegramUserId, Locale locale);
}
