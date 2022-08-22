package com.n75jr.battleship.bot.handler.message;

import com.n75jr.battleship.bot.BotMessages;
import com.n75jr.battleship.bot.BotState;
import com.n75jr.battleship.bot.emoji.MenuEmoji;
import com.n75jr.battleship.bot.handler.MessageHandler;
import com.n75jr.battleship.service.UserDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class PlayMenuMessageHandler implements MessageHandler {

    private final MessageSource messageSource;
    private final UserDataService userDataService;

    @Override
    public BotState getState() {
        return BotState.PLAY_MENU;
    }

    @Override
    public BotApiMethod<?> handleMessage(final Message message) {
        var chatId = message.getChatId();
        var telegramUserId = message.getFrom().getId();
        var dataUser = userDataService.findByTelegramUserId(telegramUserId);
        var cacheUser = dataUser.getUser();
        var locale = cacheUser.getLocale();
        var sendMessageText = messageSource.getMessage(BotMessages.PLAY_SELECT_MODE.getKey(),
                null,
                locale);
        var keyboardMarkup = getPlayMenuKeyboard(locale);

        dataUser.setBotState(BotState.PLAY_MENU_MODE);

        var sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(sendMessageText + ":")
                .replyMarkup(keyboardMarkup)
                .build();

        return sendMessage;
    }

    private ReplyKeyboardMarkup getPlayMenuKeyboard(final Locale locale) {
        var keyboardMarkup = new ReplyKeyboardMarkup();
        var row1 = new KeyboardRow(2);
        var row2 = new KeyboardRow(1);
        keyboardMarkup.setKeyboard(List.of(row1, row2));

        var messageButtonOffline = messageSource.getMessage(BotMessages.CMD_MENU_PLAY_MODE_OFFLINE.getKey(),
                new Object[]{MenuEmoji.OFFLINE}, locale);
        var messageButtonOnline = messageSource.getMessage(BotMessages.CMD_MENU_PLAY_MODE_ONLINE.getKey(),
                new Object[]{MenuEmoji.ONLINE}, locale);
        var messageButtonReturnToMainMenu = messageSource.getMessage(BotMessages.CMD_MENU_PLAY_MODE_RETURN_TO_MAIN_MENU.getKey(),
                new Object[]{MenuEmoji.RETURN_TO_MAIN_MENU}, locale);

        row1.add(messageButtonOffline);
        row1.add(messageButtonOnline);
        row2.add(messageButtonReturnToMainMenu);

        return keyboardMarkup;
    }
}
