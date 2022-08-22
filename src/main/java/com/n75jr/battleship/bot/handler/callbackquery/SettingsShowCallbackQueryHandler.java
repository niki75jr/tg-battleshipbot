package com.n75jr.battleship.bot.handler.callbackquery;

import com.n75jr.battleship.bot.BotCallbackButtons;
import com.n75jr.battleship.bot.BotMessages;
import com.n75jr.battleship.bot.BotState;
import com.n75jr.battleship.bot.emoji.LanguageEmoji;
import com.n75jr.battleship.bot.handler.CallbackQueryHandler;
import com.n75jr.battleship.service.UserDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SettingsShowCallbackQueryHandler implements CallbackQueryHandler {

    private final MessageSource messageSource;
    private final UserDataService userDataService;

    @Override
    public BotState getState() {
        return BotState.SETTINGS_OPTIONS;
    }

    @Override
    public BotApiMethod<?> handleQuery(final CallbackQuery query) {
        var queryMessage = query.getMessage();
        var chatId = queryMessage.getChatId();
        var messageId = queryMessage.getMessageId();
        var locale = userDataService.findByTelegramUserId(query.getFrom().getId()).getUser().getLocale();
        var text = messageSource.getMessage(BotMessages.SETTINGS_CHANGE_LANGUAGE_SHOW.getKey(),
                null,
                locale);

        return EditMessageText.builder()
                .chatId(chatId)
                .messageId(messageId)
                .text(text)
                .replyMarkup(getKeyboardForHandleChangeLanguageShow())
                .build();
    }

    private InlineKeyboardMarkup getKeyboardForHandleChangeLanguageShow() {
        var rows = new ArrayList<List<InlineKeyboardButton>>();
        var inlineKeyboardMarkup = new InlineKeyboardMarkup();
        var languageEmojis = LanguageEmoji.values();
        ArrayList<InlineKeyboardButton> row = null;

        inlineKeyboardMarkup.setKeyboard(rows);

        for (int i = 0; i < languageEmojis.length; i++) {
            if (i % 2 == 0) {
                row = new ArrayList<>();
                rows.add(row);
            }

            var langEmoji = languageEmojis[i];
            var buttonData = BotCallbackButtons.SETTINGS_CHANGE_LANGUAGE_SELECT.getQueryData() +
                    langEmoji.name();
            var button = InlineKeyboardButton.builder()
                    .callbackData(buttonData)
                    .text(langEmoji.getCodeUnits())
                    .build();

            row.add(button);
        }

        return inlineKeyboardMarkup;
    }
}
