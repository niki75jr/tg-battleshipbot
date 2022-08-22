package com.n75jr.battleship.bot.handler.callbackquery;

import com.n75jr.battleship.bot.BotCallbackButtons;
import com.n75jr.battleship.bot.BotMessages;
import com.n75jr.battleship.bot.BotState;
import com.n75jr.battleship.bot.emoji.LanguageEmoji;
import com.n75jr.battleship.bot.handler.CallbackQueryHandler;
import com.n75jr.battleship.bot.handler.DataHolder;
import com.n75jr.battleship.service.BotMessageService;
import com.n75jr.battleship.service.SettingsMenuService;
import com.n75jr.battleship.service.UserDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.Locale;

@Log4j2
@Component
@RequiredArgsConstructor
public class SettingsChangeLanguageShowCallbackQueryHandler implements CallbackQueryHandler {

    private final MessageSource messageSource;
    private final UserDataService userDataService;
    private final SettingsMenuService settingsMenuService;
    private final BotMessageService botMessageService;

    @Override
    public BotState getState() {
        return BotState.SETTINGS_CHANGE_LANGUAGE_SHOW;
    }

    @Override
    public BotApiMethod<?> handleQuery(final CallbackQuery query) {
        var chatId = query.getMessage().getChatId();
        var queryMessage = query.getMessage();
        var dataHolder = DataHolder.builder()
                .query(query)
                .chatId(queryMessage.getChatId())
                .messageId(queryMessage.getMessageId())
                .dataUser(userDataService.findByTelegramUserId(query.getFrom().getId()))
                .build();
        var dataUser = dataHolder.getDataUser();
        var botState = dataUser.getBotState();

        if (botState != BotState.MAIN_MENU) {
            return getNotAvailableAnswerCallbackQuery(query.getId(), dataUser.getUser().getLocale());
        }

        var result = handleChangeLanguageProcess(dataHolder);
        var locale = dataUser.getUser().getLocale();
        var mainMenuMessage = botMessageService.getMainMenuMessage(chatId, locale);

        dataUser.setBotState(BotState.MAIN_MENU);

        botMessageService.sendBotApiMethod(result);
        return mainMenuMessage;
    }

    private BotApiMethod<?> getNotAvailableAnswerCallbackQuery(final String queryId, final Locale locale) {
        return AnswerCallbackQuery.builder()
                .callbackQueryId(queryId)
                .text(messageSource.getMessage(BotMessages.SETTINGS_CHANGE_LANGUAGE_NOT_AVAILABLE.getKey(),
                        null,
                        locale))
                .showAlert(false)
                .build();
    }

    private BotApiMethod<?> handleChangeLanguageProcess(final DataHolder dataHolder) {
        var cacheUser = dataHolder.getDataUser().getUser();
        var locale = cacheUser.getLocale();
        var langCode = dataHolder.getQuery().getData()
                .split(BotCallbackButtons.SETTINGS_CHANGE_LANGUAGE_SELECT.getQueryData())[1];

        for (var le : LanguageEmoji.values()) {
            if (le.name().equals(langCode)) {
                locale = Locale.forLanguageTag(le.name().toLowerCase());
                break;
            }
        }

        var isChanged = settingsMenuService.changeLanguage(cacheUser.getTelegramUserId(), locale);

        if (log.isDebugEnabled()) {
            log.debug("handleChangeLanguageProcess: locale: {}, isChanged: {}", locale, isChanged);
        }

        var builder = EditMessageText.builder()
                .chatId(dataHolder.getChatId())
                .messageId(dataHolder.getMessageId());

        String messageText;
        if (isChanged) {
            messageText = messageSource.getMessage(BotMessages.SETTINGS_CHANGE_LANGUAGE_CHANGED_SUCCESS.getKey(),
                    null,
                    locale);
        } else {
            messageText = messageSource.getMessage(BotMessages.SETTINGS_CHANGE_LANGUAGE_CHANGED_FAILURE.getKey(),
                    null,
                    locale);
        }

        return builder.text(messageText)
                .build();
    }
}
