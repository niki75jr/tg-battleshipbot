package com.n75jr.battleship.util;

import com.n75jr.battleship.bot.BotState;
import com.n75jr.battleship.domain.DataUser;
import lombok.experimental.UtilityClass;

import java.util.Locale;

@UtilityClass
public class UserUtils {

    public static DataUser toUser(final org.telegram.telegrambots.meta.api.objects.User user, final long chatId) {
        var telegramUserId = user.getId();
        var firstName = user.getFirstName();
        var lastName = user.getLastName();
        var languageCode = user.getLanguageCode();

        var buildUser = com.n75jr.battleship.domain.User.builder()
                .telegramUserId(telegramUserId)
                .chatId(chatId)
                .firstName(firstName)
                .lastName(lastName)
                .locale(Locale.forLanguageTag(languageCode))
                .build();

        return DataUser.builder()
                .user(buildUser)
                .botState(BotState.UNDEFINED)
                .build();
    }

    public static DataUser toDataUser(final com.n75jr.battleship.domain.User user) {
        return DataUser.builder()
                .user(user)
                .botState(BotState.UNDEFINED)
                .build();
    }
}
