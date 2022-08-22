package com.n75jr.battleship.bot.handler.message;

import com.n75jr.battleship.bot.BotState;
import com.n75jr.battleship.bot.emoji.MenuEmoji;
import com.n75jr.battleship.bot.handler.MessageHandler;
import com.n75jr.battleship.service.BotMessageService;
import com.n75jr.battleship.service.PlayOfflineServiceImpl;
import com.n75jr.battleship.service.UserDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class PlayMenuModeMessageHandler implements MessageHandler {

    private final UserDataService userDataService;
    private final BotMessageService botMessageService;
    private final PlayOfflineServiceImpl playOfflineServiceImpl;

    @Override
    public BotState getState() {
        return BotState.PLAY_MENU_MODE;
    }

    @Override
    public BotApiMethod<?> handleMessage(final Message message) {
        var chatId = message.getChatId();
        var messageId = message.getMessageId();
        var receivedMessageText = message.getText();
        var telegramUserId = message.getFrom().getId();
        var userData = userDataService.findByTelegramUserId(telegramUserId);
        var locale = userData.getUser().getLocale();
        var botState = userData.getBotState();
        var key = receivedMessageText.split("\\s")[0];
        var emoji = Arrays.stream(MenuEmoji.values())
                .filter(menuEmoji -> menuEmoji.getCodeUnits().equals(key))
                .findAny()
                .orElse(MenuEmoji.DUMMY);

        switch (emoji) {
            case OFFLINE:
                System.out.println("OFFLINE");
                botState = BotState.PLAY_OFFLINE_GAME_SHIP_PLACEMENT;
                playOfflineServiceImpl.createGame(userData);
                break;
            case ONLINE:
                System.out.println("ONLINE");
                break;
            case RETURN_TO_MAIN_MENU:
                botState = BotState.MAIN_MENU;
                userData.setBotState(botState);
                return botMessageService.getMainMenuMessage(chatId, locale);
            default:
                return DeleteMessage.builder()
                        .chatId(chatId)
                        .messageId(messageId)
                        .build();
        }

        userData.setBotState(botState);

        return null;
    }
}
