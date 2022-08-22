package com.n75jr.battleship.bot.handler.message;

import com.n75jr.battleship.bot.BotMessages;
import com.n75jr.battleship.bot.BotState;
import com.n75jr.battleship.bot.emoji.MenuEmoji;
import com.n75jr.battleship.bot.handler.MessageHandler;
import com.n75jr.battleship.service.UserDataService;
import com.n75jr.battleship.service.UserGameService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Locale;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ProfileInfoMessageHandler implements MessageHandler {

    private final MessageSource messageSource;
    private final UserGameService userGameService;
    private final UserDataService userDataService;

    @Override
    public BotState getState() {
        return BotState.PROFILE_INFO;
    }

    @Override
    public BotApiMethod<?> handleMessage(final Message message) {
        var chatId = message.getChatId();
        var messageUser = message.getFrom();
        var telegramUserId = messageUser.getId();
        var dataUser = userDataService.findByTelegramUserId(telegramUserId);
        var locale = dataUser.getUser().getLocale();
        var profileInfo = getProfileInfo(messageUser, locale);
        var gamesStat = getGameStat(
                userGameService.getProfileInfoByTelegramUserId(telegramUserId), locale
        );

        dataUser.setBotState(BotState.MAIN_MENU);

        return SendMessage.builder()
                .chatId(chatId)
                .text(profileInfo + System.lineSeparator() + gamesStat)
                .build();
    }

    private String getProfileInfo(final User messageUser, final Locale locale) {
        var notAvailable = messageSource.getMessage(BotMessages.PROFILE_YOUR_NAME_NOT_AVAILABLE.getKey(), null, locale);

        return MenuEmoji.USER_PROFILE +
                " ━━━━━━━━━━━━━" +
                System.lineSeparator() +
                "└" + messageSource.getMessage(BotMessages.PROFILE_YOUR_ID_TELEGRAM.getKey(), null, locale) + ": " + messageUser.getId() +
                System.lineSeparator() +
                "└" + messageSource.getMessage(BotMessages.PROFILE_YOUR_NAME_FIRST.getKey(), null, notAvailable, locale) + ": " + messageUser.getFirstName() +
                System.lineSeparator() +
                "└" + messageSource.getMessage(BotMessages.PROFILE_YOUR_NAME_LAST.getKey(), null, notAvailable, locale) + ": " + messageUser.getLastName();
    }

    private String getGameStat(final Map<String, ?> stat, final Locale locale) {
        return MenuEmoji.ANCHOR +
                " ━━━━━━━━━━━━━" +
                System.lineSeparator() +
                "└" + messageSource.getMessage(BotMessages.PROFILE_YOUR_GAMES_TOTAL_GAMES.getKey(), null, locale) + ": " + stat.get("total_games") +
                System.lineSeparator() +
                "└" + messageSource.getMessage(BotMessages.PROFILE_YOUR_GAMES_TOTAL_SCORES.getKey(), null, locale) + ": " + stat.get("total_scores") +
                System.lineSeparator() +
                "└" + messageSource.getMessage(BotMessages.PROFILE_YOUR_GAMES_TOTAL_WINS.getKey(), null, locale) + ": " + stat.get("total_wins") +
                System.lineSeparator() +
                "└" + messageSource.getMessage(BotMessages.PROFILE_YOUR_GAMES_TOTAL_LOSES.getKey(), null, locale) + ": " + stat.get("total_loses") +
                System.lineSeparator() +
                "└" + messageSource.getMessage(BotMessages.PROFILE_YOUR_GAMES_TOTAL_SHOTS.getKey(), null, locale) + ": " + stat.get("total_shots");
    }
}
