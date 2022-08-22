package com.n75jr.battleship.bot;

import com.n75jr.battleship.bot.context.BotStateContext;
import com.n75jr.battleship.bot.emoji.MenuEmoji;
import com.n75jr.battleship.domain.DataUser;
import com.n75jr.battleship.exception.NotFountHandlerForBotStateException;
import com.n75jr.battleship.exception.UserNotFoundException;
import com.n75jr.battleship.service.BotMessageService;
import com.n75jr.battleship.service.PlayOfflineService;
import com.n75jr.battleship.service.PlayOnlineServiceImpl;
import com.n75jr.battleship.service.UserDataService;
import com.n75jr.battleship.util.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.ChatMemberUpdated;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Arrays;

@Log4j2
@Component
@RequiredArgsConstructor
public class TelegramFacade {

    private final UserDataService userDataService;
    private final BotStateContext botStateContext;
    private final BotMessageService botMessageService;
    private final PlayOfflineService playOfflineService;
    private final PlayOnlineServiceImpl playOnlineService;

    public BotApiMethod<?> handleUpdate(final Update update) {
        BotApiMethod<?> answer = null;
        var updateMessage = update.getMessage();
        var updateCallbackQuery = update.getCallbackQuery();
        var updateMyChatMember = update.getMyChatMember();
        var chatId = 0L;
        var telegramUserId = 0L;

        try {
            if (updateMessage != null && updateMessage.hasText()) {
                chatId = updateMessage.getChatId();
                telegramUserId = update.getMessage().getFrom().getId();

                if (log.isDebugEnabled()) {
                    log.debug("handleUpdate: [{}][{}] updateMessage: {}",
                            chatId, telegramUserId, updateMessage);
                }

                answer = handleInputMessage(update);
            } else if (updateCallbackQuery != null && update.hasCallbackQuery()) {
                chatId = update.getCallbackQuery().getMessage().getChatId();
                telegramUserId = update.getCallbackQuery().getFrom().getId();

                if (log.isDebugEnabled()) {
                    log.debug("handleUpdate: [{}][{}] updateCallbackQuery: {}",
                            chatId, telegramUserId, updateCallbackQuery);
                }

                answer = handleCallbackQuery(update);
            } else if (updateMyChatMember != null
                    && updateMyChatMember.getNewChatMember().getStatus().equals(BotChatMemberStatus.LEAVED.getStatus())) {
                chatId = update.getMyChatMember().getChat().getId();
                telegramUserId = update.getMyChatMember().getFrom().getId();

                if (log.isDebugEnabled()) {
                    log.debug("handleUpdate: updateMyChatMember: {}", updateMyChatMember);
                }

                handleMyChatMember(updateMyChatMember);
            }
        } catch (Exception e) {
            log.error("handleUpdate: [{}][{}] caught exception: {}", chatId, telegramUserId, e);

            try {
                var dataUser = userDataService.findByTelegramUserId(telegramUserId);
                dataUser.setBotState(BotState.MAIN_MENU);
                var locale = dataUser.getUser().getLocale();
                botMessageService.sendUnexpectedErrorMessage(chatId, locale);
                botMessageService.sendMainMenuMessage(chatId, locale);
            } catch (Exception ex) {
                log.error("\tnot found chat or user: [{}][{}]", telegramUserId, chatId);
            }
        }

        return answer;
    }

    private BotApiMethod<?> handleInputMessage(final Update update) {
        var message = update.getMessage();
        var chatId = message.getChatId();
        var receivedMessageText = message.getText();
        var messageUser = message.getFrom();
        var telegramUserId = messageUser.getId();

        DataUser dataUser;
        try {
            dataUser = userDataService.findByTelegramUserId(telegramUserId);
        } catch (UserNotFoundException e) {
            dataUser = userDataService.save(UserUtils.toUser(messageUser, chatId));
        }

        var locale = dataUser.getUser().getLocale();
        var botState = dataUser.getBotState();

        if (log.isDebugEnabled()) {
            log.debug("handleInputMessage: (state: {}), \"{}\" (langCode: {}) from {} (telegramUserId: {})",
                    botState, receivedMessageText, locale,
                    String.format("%s %s", messageUser.getLastName(), messageUser.getFirstName()),
                    messageUser.getId());
        }

        if (receivedMessageText.equals("/start")) {
            if (isGame(botState)) {
                if (isOfflineGame(botState)) {
                    playOfflineService.interruptGame(telegramUserId);
                } else {
                    playOnlineService.interruptGame(telegramUserId, botState);
                }

                return null;
            }

            botState = BotState.MAIN_MENU;
        } else if (isGame(botState)) {
            switch (botState) {
                case PLAY_OFFLINE_GAME_YOUR_MOVE:
                    playOfflineService.transferUpdate(telegramUserId, update);
                    return null;
                case PLAY_ONLINE_GAME_YOUR_MOVE:
                    playOnlineService.transferUpdate(telegramUserId, update);
                    return null;
                default:
                    if (receivedMessageText.startsWith(MenuEmoji.RETURN_TO_MAIN_MENU.getCodeUnits())) {
                        playOnlineService.interruptGame(telegramUserId, botState);
                        return null;
                    }

                    botState = BotState.DEFAULT;
                    break;
            }
        } else {
            var key = receivedMessageText.split("\\s")[0];
            var emoji = Arrays.stream(MenuEmoji.values())
                    .filter(menuEmoji -> menuEmoji.getCodeUnits().equals(key))
                    .findAny()
                    .orElse(MenuEmoji.DUMMY);

            switch (emoji) {
                case RETURN_TO_MAIN_MENU:
                    botState = BotState.MAIN_MENU;
                    break;
                case PLAY:
                    botState = BotState.PLAY_MENU;
                    break;
                case OFFLINE:
                    botState = BotState.PLAY_OFFLINE_GAME_SHIP_PLACEMENT;
                    break;
                case ONLINE:
                    botState = BotState.PLAY_ONLINE_GAME_SEARCH;
                    break;
                case USER_PROFILE:
                    botState = BotState.PROFILE_INFO;
                    break;
                case RATING_TABLE:
                    botState = BotState.RATING_TABLE;
                    break;
                case SETTINGS:
                    botState = BotState.SETTINGS;
                    break;
                case ABOUT:
                    botState = BotState.ABOUT;
                    break;
                default:
                    botState = BotState.DEFAULT;
            }
        }

        if (botState != BotState.DEFAULT) {
            dataUser.setBotState(botState);
        }

        var handler = botStateContext.getMessageHandler(botState);
        var answer = handler.handleMessage(message);

        if (log.isDebugEnabled()) {
            log.debug("\n locale: {}, botState: {}, \n handler: {}, \n userBotState: {}",
                    locale,
                    botState,
                    handler.name(),
                    dataUser.getBotState());
        }

        return answer;
    }

    private BotApiMethod<?> handleCallbackQuery(final Update update) {
        var query = update.getCallbackQuery();
        var telegramUserId = query.getFrom().getId();
        var userData = userDataService.findByTelegramUserId(telegramUserId);
        var botState = userData.getBotState();

        if (log.isDebugEnabled()) {
            log.debug("handleCallbackQuery: botState: {}, userData: {}",
                    botState, userData);
        }

        if (isGame(botState)) {
            if (isOfflineGame(botState)) {
                playOfflineService.transferUpdate(telegramUserId, update);
            } else {
                playOnlineService.transferUpdate(telegramUserId, update);
            }

            return null;
        } else {
            botState = detectBotStateForCallbackHandler(query);
        }

        var handler = botStateContext.getCallbackQueryHandler(botState);

        return handler.handleQuery(query);
    }

    private void handleMyChatMember(final ChatMemberUpdated updateChatMember) {
        if (updateChatMember.getNewChatMember().getStatus().equals(BotChatMemberStatus.LEAVED.getStatus())) {
            var handler = botStateContext.getChatMemberHandler(BotState.LEAVE);
            handler.handleMyChatMemberUpdate(updateChatMember);
        }
    }

    private BotState detectBotStateForCallbackHandler(final CallbackQuery query) {
        var queryData = query.getData();

        if (queryData.equals(BotCallbackButtons.SETTINGS_CHANGE_LANGUAGE.getQueryData())) {
            return BotState.SETTINGS_OPTIONS;
        } else if (queryData.startsWith(BotCallbackButtons.SETTINGS_CHANGE_LANGUAGE_SELECT.getQueryData())) {
            return BotState.SETTINGS_CHANGE_LANGUAGE_SHOW;
        } else {
            throw new NotFountHandlerForBotStateException("Not found for \"" + queryData + "\"");
        }
    }

    private boolean isGame(final BotState botState) {
        return isOfflineGame(botState) || isOnlineGame(botState);
    }

    private boolean isOfflineGame(final BotState botState) {
        switch (botState) {
            case PLAY_OFFLINE_GAME_SHIP_PLACEMENT:
            case PLAY_OFFLINE_GAME_YOUR_MOVE:
            case PLAY_OFFLINE_GAME_ENEMY_MOVE:
            case PLAY_OFFLINE_GAME:
                return true;
            default:
                return false;
        }
    }

    private boolean isOnlineGame(final BotState botState) {
        switch (botState) {
            case PLAY_ONLINE_GAME_SEARCH:
            case PLAY_ONLINE_GAME_SHIPS_PLACEMENT:
            case PLAY_ONLINE_GAME_SHIPS_PLACEMENT_CONFIRM:
            case PLAY_ONLINE_GAME_YOUR_MOVE:
            case PLAY_ONLINE_GAME_ENEMY_MOVE:
                return true;
            default:
                return false;
        }
    }
}
