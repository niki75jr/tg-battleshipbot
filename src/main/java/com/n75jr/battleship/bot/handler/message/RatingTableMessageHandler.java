package com.n75jr.battleship.bot.handler.message;

import com.n75jr.battleship.bot.BotMessages;
import com.n75jr.battleship.bot.BotState;
import com.n75jr.battleship.bot.emoji.MenuEmoji;
import com.n75jr.battleship.bot.handler.MessageHandler;
import com.n75jr.battleship.service.UserDataService;
import com.n75jr.battleship.service.UserGameService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
public class RatingTableMessageHandler implements MessageHandler {

    private final MessageSource messageSource;
    private final UserGameService userGameService;
    private final UserDataService userDataService;

    @Override
    public BotState getState() {
        return BotState.RATING_TABLE;
    }

    @Override
    public BotApiMethod<?> handleMessage(final Message message) {
        var chatId = message.getChatId();
        var telegramUserId = message.getFrom().getId();
        var userData = userDataService.findByTelegramUserId(telegramUserId);
        var cacheUser = userData.getUser();
        var locale = cacheUser.getLocale();
        var textMessageBuilder = new StringBuilder();

        prepareGreetingMessage(textMessageBuilder, locale);

        var tableScores = userGameService.getTableScores(PageRequest.of(0, 10))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toList());
        var placeEmojis = EnumSet.range(MenuEmoji.FIRST, MenuEmoji.THIRD).toArray();
        var topPlayers = tableScores.stream().limit(3).collect(Collectors.toList());
        var losers = tableScores.stream()
                .skip(3)
                .collect(Collectors.toList());

        preparePlacePlayersMessage(topPlayers, textMessageBuilder, locale, placeEmojis);

        if (losers.size() > 0) {
            preparePlacePlayersMessage(losers, textMessageBuilder, locale,
                    IntStream.range(3, losers.size() + 3).boxed().toArray());
        }

        userData.setBotState(BotState.MAIN_MENU);

        return SendMessage.builder()
                .chatId(chatId)
                .text(textMessageBuilder.toString())
                .build();
    }

    private void prepareGreetingMessage(final StringBuilder textMessageBuilder,
                                        final Locale locale) {
        textMessageBuilder
                .append(MenuEmoji.TROPHY)
                .append(" ━━━━━━━━━━━━━ ")
                .append(System.lineSeparator())
                .append("├").append(messageSource.getMessage(BotMessages.RATING_TABLE.getKey(), null, locale))
                .append(System.lineSeparator())
                .append("├").append(messageSource.getMessage(BotMessages.RATING_TABLE_TOP.getKey(), new Object[]{10}, locale))
                .append(System.lineSeparator());
    }

    private void preparePlacePlayersMessage(final List<Map.Entry<Long, Long>> players,
                                            final StringBuilder messageBuilder,
                                            final Locale locale,
                                            final Object... places) {
        for (int i = 0; i < players.size(); i++) {
            messageBuilder.append(getFormatPlaceString(players.get(i), places[i], locale));
        }
    }

    private String getFormatPlaceString(final Map.Entry<Long, Long> entry,
                                        final Object place,
                                        final Locale locale) {
        return String.format("%2s", place) +
                " user#" +
                entry.getKey() +
                "   " +
                entry.getValue() +
                " " +
                messageSource.getMessage(BotMessages.RATING_TABLE_SCORES.getKey(), null, locale) +
                System.lineSeparator();
    }
}
