package com.n75jr.battleship.bot.handler.message;

import com.n75jr.battleship.bot.BotState;
import com.n75jr.battleship.bot.handler.MessageHandler;
import com.n75jr.battleship.service.BotMessageService;
import com.n75jr.battleship.service.UserDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
@RequiredArgsConstructor
public class DefaultMessageHandler implements MessageHandler {

    private final BotMessageService botMessageService;
    private final UserDataService userDataService;

    @Override
    public BotState getState() {
        return BotState.DEFAULT;
    }

    @Override
    public BotApiMethod<?> handleMessage(final Message message) {
        var chatId = message.getChatId();
        var messageId = message.getMessageId();
        var dataUser = userDataService.findByTelegramUserId(message.getFrom().getId());

        if (dataUser.getBotState() == BotState.UNDEFINED) {
            dataUser.setBotState(BotState.MAIN_MENU);
            botMessageService.sendMainMenuMessage(chatId, dataUser.getUser().getLocale());
        }

        return DeleteMessage.builder()
                .chatId(chatId)
                .messageId(messageId)
                .build();
    }
}
