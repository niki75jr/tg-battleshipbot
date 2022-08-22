package com.n75jr.battleship.bot.handler.message;

import com.n75jr.battleship.bot.BotCallbackButtons;
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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class SettingsMessageHandler implements MessageHandler {

    private final MessageSource messageSource;
    private final UserDataService userDataService;

    @Override
    public BotState getState() {
        return BotState.SETTINGS;
    }

    @Override
    public BotApiMethod<?> handleMessage(final Message message) {
        var chatId = message.getChatId();
        var messageId = message.getMessageId();
        var user = message.getFrom();
        var telegramUserId = user.getId();
        var dataUser = userDataService.findByTelegramUserId(telegramUserId);
        var locale = dataUser.getUser().getLocale();
        var messageSettings = messageSource.getMessage(BotMessages.SETTINGS.getKey(), null, locale);

        dataUser.setBotState(BotState.MAIN_MENU);

        return SendMessage.builder()
                .chatId(chatId)
                .text(messageSettings)
                .replyMarkup(getKeyboard(locale))
                .build();
    }

    private InlineKeyboardMarkup getKeyboard(final Locale locale) {
        var inlineKeyboardMarkup = new InlineKeyboardMarkup();
        var rows = new ArrayList<List<InlineKeyboardButton>>();
        inlineKeyboardMarkup.setKeyboard(rows);
        var row1 = new ArrayList<InlineKeyboardButton>();
        rows.add(row1);

        var messageButtonChangeLanguage = messageSource.getMessage(BotMessages.CMD_MENU_SETTINGS_CHANGE_LANGUAGE.getKey(),
                new Object[]{MenuEmoji.SETTINGS_CHANGE_LANGUAGE.getCodeUnits()},
                locale);
        var changeLanguageButton = InlineKeyboardButton.builder()
                .text(messageButtonChangeLanguage)
                .callbackData(BotCallbackButtons.SETTINGS_CHANGE_LANGUAGE.getQueryData())
                .build();

        row1.add(changeLanguageButton);

        return inlineKeyboardMarkup;
    }
}
