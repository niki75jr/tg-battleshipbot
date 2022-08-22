package com.n75jr.battleship.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Log4j2
@Service
@RequiredArgsConstructor
public class SettingsMenuServiceImpl implements SettingsMenuService {

    private final UserDataService userDataService;

    @Override
    public boolean changeLanguage(final Long telegramUserId, final Locale locale) {
        var user = userDataService.findByTelegramUserId(telegramUserId).getUser();
        if (user.getLocale() != locale) {
            user.setLocale(locale);
            userDataService.updateRepo(user.getTelegramUserId());

            if (log.isDebugEnabled()) {
                log.debug("changeLanguage: user: {} {} -> {} (answer: {})",
                        telegramUserId, user.getLocale(), locale, true);
            }

            return true;
        }

        return false;
    }
}
