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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class AboutMessageHandler implements MessageHandler {

    private final MessageSource messageSource;
    private final UserDataService userDataService;

    @Override
    public BotState getState() {
        return BotState.ABOUT;
    }

    @Override
    public BotApiMethod<?> handleMessage(final Message message) {
        var chatId = message.getChatId();
        var telegramUserId = message.getFrom().getId();
        var locale = userDataService.findByTelegramUserId(telegramUserId).getUser().getLocale();
        var sendMessageText = messageSource.getMessage(BotMessages.CMD_MENU_ABOUT.getKey(),
                new Object[]{MenuEmoji.ABOUT},
                locale);

        return SendMessage.builder()
                .chatId(chatId)
                .text(sendMessageText)
                .replyMarkup(getInlineKeyboard(locale))
                .build();
    }

    private InlineKeyboardMarkup getInlineKeyboard(final Locale locale) {
        var inlineKeyboardMarkup = new InlineKeyboardMarkup();
        var rows = new ArrayList<List<InlineKeyboardButton>>();
        var row1 = new ArrayList<InlineKeyboardButton>();
        var row2 = new ArrayList<InlineKeyboardButton>();
        rows.add(row1);
        rows.add(row2);
        inlineKeyboardMarkup.setKeyboard(rows);

        var messageButtonToWriteAuthor =
                messageSource.getMessage(BotMessages.ABOUT_WRITE_TO_AUTHOR.getKey(),
                        null,
                        locale);
        var messageButtonGithubRepo =
                messageSource.getMessage(BotMessages.ABOUT_GITHUB_REPO.getKey(),
                        null,
                        locale);
        var buttonWriteToAuthor = InlineKeyboardButton.builder()
                .text(messageButtonToWriteAuthor)
                .url("tg://user?id=463493238")
                .build();
        var buttonGithubRepo = InlineKeyboardButton.builder()
                .text(messageButtonGithubRepo)
                .url("https://github.com/niki75jr/tg-battleshipbot")
                .build();

        row1.add(buttonWriteToAuthor);
        row2.add(buttonGithubRepo);

        return inlineKeyboardMarkup;
    }
}
