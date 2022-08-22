package com.n75jr.battleship.bot.handler.message;

import com.n75jr.battleship.bot.BotState;
import com.n75jr.battleship.bot.handler.MessageHandler;
import com.n75jr.battleship.service.PlayOfflineServiceImpl;
import com.n75jr.battleship.service.UserDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
@RequiredArgsConstructor
public class PlayOfflineGameMessageHandler implements MessageHandler {

    private final MessageSource messageSource;
    private final UserDataService userDataService;
    private final PlayOfflineServiceImpl playOfflineServiceImpl;

    @Override
    public BotState getState() {
        return BotState.PLAY_OFFLINE_GAME_SHIP_PLACEMENT;
    }

    @Override
    public BotApiMethod<?> handleMessage(final Message message) {
        var telegramUserId = message.getFrom().getId();
        var dataUser = userDataService.findByTelegramUserId(telegramUserId);

        playOfflineServiceImpl.createGame(dataUser);

        return null;
    }
}
