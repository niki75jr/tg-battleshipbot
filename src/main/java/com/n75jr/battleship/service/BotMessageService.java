package com.n75jr.battleship.service;

import com.n75jr.battleship.bot.BotMessages;
import com.n75jr.battleship.domain.Board;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

import java.util.Locale;

public interface BotMessageService {

    BotApiMethod<?> getMainMenuMessage(Long chatId, Locale locale);
    void sendBotApiMethod(BotApiMethod<?> botApiMethod);
    void sendMessage(Long chatId, Locale locale, String messageKey, ReplyKeyboard replyKeyboard, Object... placeHolders);
    void sendMessage(Long chatId, Locale locale,
                     boolean removeKeyboard, BotMessages botMessageKey, Object... placeHolders);
    void sendDeleteMessage(Long chatId, Integer messageId);
    void sendDeleteInlineKeyBoard(Long chatId, Integer messageId);
    void sendMainMenuMessage(Long chatId, Locale locale);
    void sendUnexpectedErrorMessage(Long chatId, Locale locale);
    void sendRulesMessage(Long chatId, Locale locale, long placementDelay, long moveDelay);
    void sendMissMessage(Long chatId, Locale locale, Board.Point[][] points);
    void sendStartingGameMessage(Long chatId, Locale locale);
    void sendOnlineSearchGameMessage(Long chatId, Locale locale, int nPlayersQueue);
    void sendPlacementShipsYourBoardMessage(Long chatId, Locale locale,
                                            Board.Point[][] points, String label,
                                            short nShips, short nShots);
    void sendPlacementShipsYourBoardEditText(Long chatId, Integer messageId, Locale locale,
                                             Board.Point[][] points, String label,
                                             short nShips, short nShots);
    void sendPlacementShipsYourBoardEditReplyMarkup(Message message);
    void sendYouBoard(Long chatId, Locale locale, Board.Point[][] points, short nShips, short nShots);
    void sendEnemyBoard(Long chatId, Locale locale, Board.Point[][] points, short nShips, short nShots);
    void sendInterruptedGameMessage(Long chatId, Locale locale);
    void sendFinishGameMessage(Long chatId, Locale locale,
                               Board.Point[][] board1, Board.Point[][] board2,
                               boolean isWin, short scores);
}
