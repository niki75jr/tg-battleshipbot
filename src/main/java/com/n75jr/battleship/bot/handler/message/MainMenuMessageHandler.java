package com.n75jr.battleship.bot.handler.message;

import com.n75jr.battleship.bot.BotState;
import com.n75jr.battleship.bot.handler.MessageHandler;
import com.n75jr.battleship.service.BotMessageService;
import com.n75jr.battleship.service.UserDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
@RequiredArgsConstructor
public class MainMenuMessageHandler implements MessageHandler {

    private final UserDataService userDataService;
    private final BotMessageService botMessageService;

    @Override
    public BotState getState() {
        return BotState.MAIN_MENU;
    }

    @Override
    public BotApiMethod<?> handleMessage(final Message message) {
        var messageText = message.getText();
        var chatId = message.getChatId();
        var telegramUserId = message.getFrom().getId();
        var dataUser = userDataService.findByTelegramUserId(telegramUserId);
        var locale = dataUser.getUser().getLocale();

        var botState = BotState.MAIN_MENU;
        dataUser.setBotState(botState);

        return botMessageService.getMainMenuMessage(chatId, locale);
    }
}
