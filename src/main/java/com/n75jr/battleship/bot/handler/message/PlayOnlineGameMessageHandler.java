package com.n75jr.battleship.bot.handler.message;

import com.n75jr.battleship.bot.BotState;
import com.n75jr.battleship.bot.context.BotOnlineGameContext;
import com.n75jr.battleship.bot.handler.MessageHandler;
import com.n75jr.battleship.service.BotMessageService;
import com.n75jr.battleship.service.PlayOnlineServiceImpl;
import com.n75jr.battleship.service.UserDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
@RequiredArgsConstructor
public class PlayOnlineGameMessageHandler implements MessageHandler {

    private final MessageSource messageSource;
    private final UserDataService userDataService;
    private final BotMessageService botMessageService;
    private final PlayOnlineServiceImpl playOnlineService;
    private final BotOnlineGameContext botOnlineGameContext;

    @Override
    public BotState getState() {
        return BotState.PLAY_ONLINE_GAME_SEARCH;
    }

    @Override
    public BotApiMethod<?> handleMessage(final Message message) {
        var telegramUserId = message.getFrom().getId();
        var dataUser = userDataService.findByTelegramUserId(telegramUserId);
        var chatId = dataUser.getUser().getChatId();
        var locale = dataUser.getUser().getLocale();
        var searchPlayers = botOnlineGameContext.getCountSearchPlayers();

        botMessageService.sendOnlineSearchGameMessage(chatId, locale, searchPlayers);
        playOnlineService.searchGame(dataUser);

        return null;
    }
}
