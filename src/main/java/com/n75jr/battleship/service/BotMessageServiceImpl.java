package com.n75jr.battleship.service;

import com.n75jr.battleship.BattleshipWebhookBot;
import com.n75jr.battleship.bot.BotCallbackButtons;
import com.n75jr.battleship.bot.BotMessages;
import com.n75jr.battleship.bot.emoji.MenuEmoji;
import com.n75jr.battleship.bot.emoji.MessageEmoji;
import com.n75jr.battleship.domain.Board;
import com.n75jr.battleship.util.BoardUtils;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Log4j2
@Service
public class BotMessageServiceImpl implements BotMessageService {

    private final MessageSource messageSource;
    private final BattleshipWebhookBot bot;

    public BotMessageServiceImpl(MessageSource messageSource,
                                 @Lazy BattleshipWebhookBot bot) {
        this.messageSource = messageSource;
        this.bot = bot;
    }

    @SneakyThrows
    @Override
    public void sendMainMenuMessage(final Long chatId, final Locale locale) {
        bot.execute(
                getMainMenuMessage(chatId, locale)
        );
    }

    @Override
    public SendMessage getMainMenuMessage(final Long chatId, final Locale locale) {
        var sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(messageSource.getMessage(BotMessages.MENU_MAIN.getKey(), null, locale))
                .replyMarkup(getMainMenuKeyboard(locale))
                .build();

        if (log.isDebugEnabled()) {
            log.debug("getMainMenuMessage: (chatId: {}, locale: {})", chatId, locale);
        }

        return sendMessage;
    }

    @SneakyThrows
    @Override
    public void sendUnexpectedErrorMessage(final Long chatId, final Locale locale) {
        var sendMessageText = messageSource.getMessage(BotMessages.UNEXPECTED_ERROR.getKey(),
                new Object[]{MenuEmoji.UNEXPECTED_ERROR.getCodeUnits()}, locale);
        bot.execute(
                SendMessage.builder()
                        .chatId(chatId)
                        .text(sendMessageText)
                        .build()
        );
    }

    @SneakyThrows
    @Override
    public void sendRulesMessage(final Long chatId, final Locale locale,
                                 final long placementDelay, final long moveDelay) {
        var messageText = messageSource.getMessage(BotMessages.PLAY_RULES.getKey(),
                new Object[]{MessageEmoji.RULES_SIGN}, locale) +
                System.lineSeparator() +
                messageSource.getMessage(BotMessages.PLAY_RULES_1.getKey(),
                        new Object[]{MessageEmoji.RULES_ITEM, placementDelay}, locale) +
                System.lineSeparator() +
                messageSource.getMessage(BotMessages.PLAY_RULES_2.getKey(),
                        new Object[]{MessageEmoji.RULES_ITEM, moveDelay}, locale) +
                System.lineSeparator() +
                messageSource.getMessage(BotMessages.PLAY_RULES_3.getKey(),
                        new Object[]{MessageEmoji.RULES_ITEM}, locale) +
                System.lineSeparator() +
                messageSource.getMessage(BotMessages.PLAY_RULES_4.getKey(),
                        new Object[]{MessageEmoji.RULES_ITEM}, locale) +
                System.lineSeparator() +
                messageSource.getMessage(BotMessages.PLAY_RULES_5.getKey(),
                        new Object[]{MessageEmoji.RULES_ITEM}, locale);
        bot.execute(
                SendMessage.builder()
                        .chatId(chatId)
                        .text(messageText)
                        .build()
        );
    }

    @SneakyThrows
    @Override
    public void sendMissMessage(final Long chatId, final Locale locale, final Board.Point[][] points) {
        var messageMiss = messageSource.getMessage(BotMessages.PLAY_GAME_MISS.getKey(),
                new Object[]{MessageEmoji.MISS.getCodeUnits()}, locale);
        var messageEnemyMove = messageSource.getMessage(BotMessages.PLAY_GAME_MOVE_ENEMY.getKey(),
                null, locale);
        bot.execute(
                SendMessage.builder()
                        .chatId(chatId)
                        .text(messageMiss + System.lineSeparator() + messageEnemyMove)
                        .replyMarkup(getEnemyKeyboard(points))
                        .build()
        );
    }

    @SneakyThrows
    @Override
    public void sendStartingGameMessage(final Long chatId, final Locale locale) {
        bot.execute(
                SendMessage.builder()
                        .chatId(chatId)
                        .text(messageSource.getMessage(BotMessages.PLAY_STARTING.getKey(),
                                null,
                                locale))
                        .replyMarkup(ReplyKeyboardRemove.builder().removeKeyboard(true).build())
                        .build()
        );
    }

    @SneakyThrows
    @Override
    public void sendBotApiMethod(final BotApiMethod<?> botApiMethod) {
        bot.execute(botApiMethod);
    }

    @SneakyThrows
    @Override
    public void sendMessage(final Long chatId, final Locale locale,
                            final String messageKey, final ReplyKeyboard replyKeyboard, final Object... placeHolders) {
        var messageText = messageSource.getMessage(messageKey, placeHolders, locale);
        bot.execute(
                SendMessage.builder()
                        .chatId(chatId)
                        .text(messageText)
                        .replyMarkup(replyKeyboard)
                        .build()
        );
    }

    @SneakyThrows
    @Override
    public void sendMessage(final Long chatId, final Locale locale,
                            final boolean removeKeyboard, final BotMessages botMessageKey, final Object... placeHolders) {
        var messageText = messageSource.getMessage(botMessageKey.getKey(), placeHolders, locale);
        bot.execute(
                SendMessage.builder()
                        .chatId(chatId)
                        .text(messageText)
                        .replyMarkup(ReplyKeyboardRemove.builder().removeKeyboard(removeKeyboard).build())
                        .build()
        );
    }

    @SneakyThrows
    @Override
    public void sendDeleteMessage(final Long chatId, final Integer messageId) {
        var deleteMessage = DeleteMessage.builder()
                .chatId(chatId)
                .messageId(messageId)
                .build();
        bot.execute(deleteMessage);
    }

    @SneakyThrows
    @Override
    public void sendDeleteInlineKeyBoard(final Long chatId, final Integer messageId) {
        var editMessageReplyMarkup = EditMessageReplyMarkup.builder()
                .chatId(chatId)
                .messageId(messageId)
                .replyMarkup(null)
                .build();
        bot.execute(editMessageReplyMarkup);
    }

    @SneakyThrows
    @Override
    public void sendOnlineSearchGameMessage(final Long chatId, final Locale locale, final int nPlayersQueue) {
        var messageSearchGame =
                messageSource.getMessage(BotMessages.PLAY_GAME_ONLINE_SEARCH.getKey(),
                        null, locale);
        var messagePlayersQueue =
                messageSource.getMessage(BotMessages.PLAY_GAME_ONLINE_SEARCH_PLAYERS_QUEUE.getKey(),
                        new Object[]{nPlayersQueue}, locale);
        bot.execute(
                SendMessage.builder()
                        .chatId(chatId)
                        .text(messageSearchGame + System.lineSeparator() + messagePlayersQueue)
                        .build()
        );
    }

    @SneakyThrows
    @Override
    public void sendPlacementShipsYourBoardMessage(final Long chatId, final Locale locale,
                                                   final Board.Point[][] points, final String label,
                                                   final short nShips, final short nShots) {
        var sendMessageText =
                messageSource.getMessage(BotMessages.PLAY_MAP_BOARD_YOUR.getKey(), null, locale) +
                        System.lineSeparator() +
                        BoardUtils.toYourBoardString(points);
        var sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(sendMessageText)
                .replyMarkup(getPlacementShipsKeyboard(label))
                .build();
        bot.execute(sendMessage);
    }

    @SneakyThrows
    @Override
    public void sendPlacementShipsYourBoardEditText(final Long chatId, final Integer messageId, final Locale locale,
                                                    final Board.Point[][] points, final String label,
                                                    final short nShips, final short nShots) {
        var sendMessageText =
                messageSource.getMessage(BotMessages.PLAY_MAP_BOARD_YOUR.getKey(), null, locale) +
                        System.lineSeparator() +
                        BoardUtils.toYourBoardString(points);
        var editMessageText = EditMessageText.builder()
                .chatId(chatId)
                .messageId(messageId)
                .text(sendMessageText)
                .replyMarkup(getPlacementShipsKeyboard(label))
                .build();
        bot.execute(editMessageText);
    }

    @SneakyThrows
    @Override
    public void sendPlacementShipsYourBoardEditReplyMarkup(final Message message) {
        var editMessageReplyMarkup = EditMessageReplyMarkup.builder()
                .chatId(message.getChatId())
                .messageId(message.getMessageId())
                .replyMarkup(null)
                .build();
        bot.execute(editMessageReplyMarkup);
    }

    @SneakyThrows
    @Override
    public void sendYouBoard(final Long chatId, final Locale locale,
                             final Board.Point[][] points, final short nShips, final short nShots) {
        var sendMessageText = getTextForYourBoard(locale, points, nShips, nShots);
        var sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(sendMessageText)
                .build();
        bot.execute(sendMessage);
    }

    @SneakyThrows
    @Override
    public void sendEnemyBoard(final Long chatId, final Locale locale,
                               final Board.Point[][] points, final short nShips, final short nShots) {
        var messageYourMove = getTextForEnemyBoard(locale, nShips, nShots);
        var sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(messageYourMove)
                .replyMarkup(getEnemyKeyboard(points))
                .build();
        bot.execute(sendMessage);
    }

    @SneakyThrows
    @Override
    public void sendInterruptedGameMessage(final Long chatId, final Locale locale) {
        var sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(messageSource.getMessage(BotMessages.PLAY_GAME_INTERRUPTED.getKey(), null, locale))
                .build();
        bot.execute(sendMessage);
    }

    @SneakyThrows
    @Override
    public void sendFinishGameMessage(final Long chatId, final Locale locale,
                                      final Board.Point[][] board1, final Board.Point[][] board2,
                                      final boolean isWin, final short scores) {
        var resultGameBotMessage = isWin ? BotMessages.PLAY_GAME_WIN : BotMessages.PLAY_GAME_LOSE;
        var resultGameEmoji = isWin ? MessageEmoji.VICTORY : MessageEmoji.LOSE;
        var text = messageSource.getMessage(BotMessages.PLAY_MAP_BOARD_YOUR.getKey(), null, locale) + ": " +
                System.lineSeparator() +
                "━━━━━━━━━━━━━━━" +
                System.lineSeparator() +
                BoardUtils.toYourBoardString(board1) +
                System.lineSeparator() + System.lineSeparator() +
                messageSource.getMessage(BotMessages.PLAY_MAP_BOARD_ENEMY.getKey(), null, locale) + ": " +
                System.lineSeparator() +
                "━━━━━━━━━━━━━━━" +
                System.lineSeparator() +
                BoardUtils.toYourBoardString(board2) +
                System.lineSeparator() +
                "┬───────────────────" +
                System.lineSeparator() +
                "├ " + messageSource.getMessage(resultGameBotMessage.getKey(),
                new Object[]{resultGameEmoji}, locale) +
                System.lineSeparator() +
                "├ " + messageSource.getMessage(BotMessages.PLAY_GAME_SCORES.getKey(),
                new Object[]{MessageEmoji.SCORES}, locale) + ": " + scores;

        bot.execute(
                SendMessage.builder()
                        .chatId(chatId)
                        .text(text)
                        .build()
        );
    }
    // -----------------------------------------------------------------------------------------------------------------

    private ReplyKeyboard getEnemyKeyboard(final Board.Point[][] points) {
        var keyboardMarkup = new ReplyKeyboardMarkup();
        var rows = new ArrayList<KeyboardRow>();

        for (int y = 0; y < 10; y++) {
            var buttons = new KeyboardRow();
            for (int x = 0; x < 10; x++) {
                var button = KeyboardButton.builder()
                        .text(BoardUtils.toEnemyBoardCeilString(points[y][x]))
                        .build();
                buttons.add(button);
            }
            rows.add(buttons);
        }
        keyboardMarkup.setKeyboard(rows);

        return keyboardMarkup;
    }

    private String getTextForYourBoard(final Locale locale,
                                       final Board.Point[][] points, final short nShips, final short nShots) {
        return
                messageSource.getMessage(BotMessages.PLAY_MAP_BOARD_YOUR.getKey(), null, locale) +
                        System.lineSeparator() +
                        messageSource.getMessage(BotMessages.PLAY_MAP_BOARD_YOUR_LIVE_SHIPS.getKey(),
                                new Object[]{nShips}, locale) +
                        System.lineSeparator() +
                        messageSource.getMessage(BotMessages.PLAY_BOARD_ENEMY_TOTAL_SHOTS.getKey(),
                                new Object[]{nShots}, locale) +
                        System.lineSeparator() +
                        BoardUtils.toYourBoardString(points);
    }

    private String getTextForEnemyBoard(final Locale locale, final short nShips, final short nShots) {
        return
                messageSource.getMessage(BotMessages.PLAY_GAME_MOVE_YOUR.getKey(),
                        new Object[]{MessageEmoji.YOUR_MOVE.getCodeUnits()}, locale) +
                        System.lineSeparator() +
                        messageSource.getMessage(BotMessages.PLAY_BOARD_ENEMY_TOTAL_LIVE_SHIPS.getKey(),
                                new Object[]{nShips}, locale) +
                        System.lineSeparator() +
                        messageSource.getMessage(BotMessages.PLAY_MAP_BOARD_YOUR_TOTAL_SHOTS.getKey(),
                                new Object[]{nShots}, locale);
    }

    @SneakyThrows
    public void sendPlacementYourBoard(final Long chatId, final Locale locale,
                                       final Board.Point[][] points, final String label) {
        var messageYourMap = messageSource.getMessage(BotMessages.PLAY_MAP_BOARD_YOUR.getKey(),
                null, locale);
        var textSendMessage = messageYourMap +
                System.lineSeparator() +
                BoardUtils.toYourBoardString(points);
        var sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(textSendMessage)
                .replyMarkup(getPlacementShipsKeyboard(label))
                .build();
        bot.execute(sendMessage);
    }

    private ReplyKeyboard getMainMenuKeyboard(final Locale locale) {
        var replyKeyboardMarkup = new ReplyKeyboardMarkup();
        var rows = new ArrayList<KeyboardRow>();
        replyKeyboardMarkup.setKeyboard(rows);
        var row1 = new KeyboardRow(1);
        var row2 = new KeyboardRow(2);
        var row3 = new KeyboardRow(1);
        var row4 = new KeyboardRow(1);
        rows.addAll(List.of(row1, row2, row3, row4));

        var messageButtonPlay = messageSource.getMessage(BotMessages.CMD_MENU_PLAY.getKey(),
                new Object[]{MenuEmoji.PLAY.getCodeUnits()},
                locale);
        var messageButtonProfile = messageSource.getMessage(BotMessages.CMD_MENU_MENU_PROFILE.getKey(),
                new Object[]{MenuEmoji.USER_PROFILE.getCodeUnits()},
                locale);
        var messageButtonTable = messageSource.getMessage(BotMessages.CMD_MENU_RATING_TABLE.getKey(),
                new Object[]{MenuEmoji.RATING_TABLE.getCodeUnits()},
                locale);
        var messageButtonSettings = messageSource.getMessage(BotMessages.CMD_MENU_SETTINGS.getKey(),
                new Object[]{MenuEmoji.SETTINGS.getCodeUnits()},
                locale);
        var messageButtonAbout = messageSource.getMessage(BotMessages.CMD_MENU_ABOUT.getKey(),
                new Object[]{MenuEmoji.ABOUT.getCodeUnits()},
                locale);

        var buttonPlay = KeyboardButton.builder().text(messageButtonPlay).build();
        var buttonProfile = KeyboardButton.builder().text(messageButtonProfile).build();
        var buttonTable = KeyboardButton.builder().text(messageButtonTable).build();
        var buttonSettings = KeyboardButton.builder().text(messageButtonSettings).build();
        var buttonAbout = KeyboardButton.builder().text(messageButtonAbout).build();

        row1.add(buttonPlay);
        row2.add(buttonProfile);
        row2.add(buttonTable);
        row3.add(buttonSettings);
        row4.add(buttonAbout);

        return replyKeyboardMarkup;
    }

    private InlineKeyboardMarkup getPlacementShipsKeyboard(final String label) {
        var inlineKeyboardMarkup = new InlineKeyboardMarkup();
        var rows = new ArrayList<List<InlineKeyboardButton>>();
        var row1 = new ArrayList<InlineKeyboardButton>();

        inlineKeyboardMarkup.setKeyboard(rows);
        rows.add(row1);

        var buttonNewRandom = InlineKeyboardButton.builder()
                .callbackData(BotCallbackButtons.PLAY_SHIPS_PLACEMENT_NEW_RANDOM.getQueryData() + label)
                .text(MenuEmoji.NEW_RANDOM.getCodeUnits())
                .build();
        var buttonConfirm = InlineKeyboardButton.builder()
                .callbackData(BotCallbackButtons.PLAY_SHIPS_PLACEMENT_CONFIRM.getQueryData() + label)
                .text(MenuEmoji.CONFIRM.getCodeUnits())
                .build();

        row1.add(buttonNewRandom);
        row1.add(buttonConfirm);

        return inlineKeyboardMarkup;
    }
}
