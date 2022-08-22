package com.n75jr.battleship.service;

import com.n75jr.battleship.bot.BotMessages;
import com.n75jr.battleship.bot.BotState;
import com.n75jr.battleship.bot.context.BotOnlineGameContext;
import com.n75jr.battleship.domain.DataUser;
import com.n75jr.battleship.service.game.Game;
import com.n75jr.battleship.service.game.OnlineGame;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Log4j2
@Service
public class PlayOnlineServiceImpl implements PlayOnlineService {

    private final BotMessageService botMessageService;
    private final MessageSource messageSource;
    private final UserDataService userDataService;
    private final UserGameService userGameService;
    private final BotOnlineGameContext botOnlineGameContext;
    private final Lock lock;
    private final Condition enoughCondition;
    private final GameSearcher gameSearcher;

    public PlayOnlineServiceImpl(BotMessageService botMessageService,
                                 MessageSource messageSource,
                                 UserDataService userDataService,
                                 UserGameService userGameService,
                                 BotOnlineGameContext botOnlineGameContext) {
        this.botMessageService = botMessageService;
        this.messageSource = messageSource;
        this.userDataService = userDataService;
        this.userGameService = userGameService;
        this.botOnlineGameContext = botOnlineGameContext;

        lock = new ReentrantLock();
        enoughCondition = lock.newCondition();
        gameSearcher = GameSearcher.of(this,
                botOnlineGameContext.getUSERS_QUEUE(),
                lock,
                enoughCondition);

        botOnlineGameContext.submitGameSearcher(gameSearcher);
    }

    public void searchGame(final DataUser dataUser) {
        lock.lock();
        try {
            botOnlineGameContext.addUserToQueue(dataUser);
            enoughCondition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public void createGame(final DataUser dataUser1, final DataUser dataUser2) {
        var cacheUserPlayer1 = dataUser1.getUser();
        var telegramUserIdPlayer1 = cacheUserPlayer1.getTelegramUserId();
        var localePlayer1 = cacheUserPlayer1.getLocale();
        var chatIdPlayer1 = cacheUserPlayer1.getChatId();
        var cacheUserPlayer2 = dataUser2.getUser();
        var telegramUserIdPlayer2 = cacheUserPlayer2.getTelegramUserId();
        var localePlayer2 = cacheUserPlayer2.getLocale();
        var chatIdPlayer2 = cacheUserPlayer2.getChatId();

        botMessageService.sendMessage(chatIdPlayer1, localePlayer1, true, BotMessages.PLAY_LOADING);
        botMessageService.sendMessage(chatIdPlayer2, localePlayer2, true, BotMessages.PLAY_LOADING);
        botMessageService.sendRulesMessage(chatIdPlayer1, localePlayer1,
                Game.DEFAULT_PLACEMENT_DELAY / 1000, Game.DEFAULT_MOVE_DELAY / 1000);
        botMessageService.sendRulesMessage(chatIdPlayer2, localePlayer2,
                Game.DEFAULT_PLACEMENT_DELAY / 1000, Game.DEFAULT_MOVE_DELAY / 1000);

        var onlineGame = new OnlineGame(
                messageSource,
                userGameService,
                botMessageService,
                botOnlineGameContext,
                dataUser1,
                dataUser2,
                30_000L,
                30_000L);

        dataUser1.setBotState(BotState.PLAY_ONLINE_GAME_SHIPS_PLACEMENT);
        dataUser2.setBotState(BotState.PLAY_ONLINE_GAME_SHIPS_PLACEMENT);
        botOnlineGameContext.addGame(telegramUserIdPlayer1, telegramUserIdPlayer2, onlineGame);
    }

    public void removeUserFromQueue(final DataUser user) {
        botOnlineGameContext.removeUserFromQueue(user);
    }

    public void interruptGame(final Long telegramUserId, BotState oldBotState) {
        var dataUser = userDataService.findByTelegramUserId(telegramUserId);
        var userBotState = dataUser.getBotState();
        var chatId = dataUser.getUser().getChatId();
        var locale = dataUser.getUser().getLocale();

        switch (oldBotState) {
            case PLAY_ONLINE_GAME_SEARCH:
                removeUserFromQueue(dataUser);
                botMessageService.sendInterruptedGameMessage(chatId, locale);
                if (userBotState != BotState.LEAVE) {
                    dataUser.setBotState(BotState.MAIN_MENU);
                    botMessageService.sendMainMenuMessage(chatId, locale);
                }
                break;
            case PLAY_ONLINE_GAME_SHIPS_PLACEMENT:
                botOnlineGameContext.interruptShipsPlacementGame(telegramUserId);
                break;
            case PLAY_ONLINE_GAME_SHIPS_PLACEMENT_CONFIRM:
                botOnlineGameContext.interruptShipsPlacementConfirmGame(telegramUserId);
                break;
            case PLAY_ONLINE_GAME_ENEMY_MOVE:
            case PLAY_ONLINE_GAME_YOUR_MOVE:
                botOnlineGameContext.interruptMoveGame(telegramUserId);
                break;
        }
    }

    public void finishGame(final Long telegramUserId) {
        botOnlineGameContext.finishGame(telegramUserId);
    }

    public void transferUpdate(final Long telegramUserId, final Update update) {
        if (log.isDebugEnabled()) {
            log.debug("transferUpdate: [{}]: update: {}",
                    telegramUserId, update);
        }

        botOnlineGameContext.transferUpdate(telegramUserId, update);
    }

    @Override
    public int getSearchSize() {
        return botOnlineGameContext.getCountSearchPlayers();
    }

    @Log4j2
    @RequiredArgsConstructor(staticName = "of")
    public static class GameSearcher implements Runnable {

        private final PlayOnlineServiceImpl playOnlineService;
        private final BlockingDeque<DataUser> usersQueue;
        private final Lock lock;
        private final Condition enoughCondition;

        @Override
        public void run() {
            lock.lock();

            try {
                while (!Thread.interrupted()) {
                    while (usersQueue.size() < 2) {
                        enoughCondition.await();
                    }

                    var player1 = usersQueue.poll();
                    var isWaitingPlayer1 = player1.getBotState() != BotState.PLAY_ONLINE_GAME_SEARCH;

                    if (isWaitingPlayer1) {
                        continue;
                    }

                    var player2 = usersQueue.poll();
                    var isWaitingPlayer2 = player2.getBotState() != BotState.PLAY_ONLINE_GAME_SEARCH;

                    if (isWaitingPlayer2) {
                        usersQueue.putFirst(player1);
                        continue;
                    }

                    if (log.isDebugEnabled()) {
                        log.debug("run: creating game for p1: {}, p2: {}",
                                player1.getUser().getTelegramUserId(),
                                player2.getUser().getTelegramUserId());
                    }

                    playOnlineService.createGame(player1, player2);

                    if (log.isDebugEnabled()) {
                        log.debug("run: created game for p1: {}, p2: {}",
                                player1.getUser().getTelegramUserId(),
                                player2.getUser().getTelegramUserId());
                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                lock.unlock();
            }
        }
    }
}
