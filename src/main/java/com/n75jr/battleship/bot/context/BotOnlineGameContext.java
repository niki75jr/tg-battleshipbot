package com.n75jr.battleship.bot.context;

import com.n75jr.battleship.domain.DataUser;
import com.n75jr.battleship.service.PlayOnlineServiceImpl;
import com.n75jr.battleship.service.game.OnlineGame;
import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.LongAdder;

@Component
public class BotOnlineGameContext {

    private final ExecutorService ONLINE_GAME_SEARCHER_POOL = Executors.newSingleThreadExecutor();
    @Getter
    private final ExecutorService ONLINE_SHIPS_PLACEMENT_POOL = Executors.newCachedThreadPool();
    private final ExecutorService ONLINE_GAMES_POOL = Executors.newCachedThreadPool();
    private final LongAdder ONLINE_PLAYERS_QUEUE_COUNTER = new LongAdder();
    @Getter
    private final BlockingDeque<DataUser> USERS_QUEUE = new LinkedBlockingDeque<>();
    private final Map<Long, OnlineGame> ONLINE_GAMES_MAP = new ConcurrentHashMap<>();
    private final Map<Long, Future<?>> ONLINE_GAMES_FUTURE_MAP = new ConcurrentHashMap<>();
    private final Map<Long, Future<?>> ONLINE_SHIPS_PLACEMENT_MAP = new ConcurrentHashMap<>();

    @PreDestroy
    void tearDown() {
        ONLINE_GAMES_POOL.shutdown();
        ONLINE_GAME_SEARCHER_POOL.shutdown();
        ONLINE_GAME_SEARCHER_POOL.shutdown();
        ONLINE_SHIPS_PLACEMENT_POOL.shutdown();
    }

    public boolean isGameSearcherPoolTerminated() {
        return ONLINE_GAME_SEARCHER_POOL.isTerminated();
    }

    public boolean isGameSearcherPoolShutdown() {
        return ONLINE_GAME_SEARCHER_POOL.isShutdown();
    }

    public boolean isShipsPlacementPoolTerminated() {
        return ONLINE_SHIPS_PLACEMENT_POOL.isTerminated();
    }

    public boolean isShipsPlacementPoolShutdown() {
        return ONLINE_SHIPS_PLACEMENT_POOL.isShutdown();
    }

    public boolean isGamesPoolTerminated() {
        return ONLINE_GAMES_POOL.isTerminated();
    }

    public boolean isGamesPoolShutdown() {
        return ONLINE_GAMES_POOL.isShutdown();
    }

    public int getCountSearchPlayers() {
        return USERS_QUEUE.size();
    }

    public int getGamesMapSize() {
        return ONLINE_GAMES_MAP.size();
    }

    public int getGamesFutureMapSize() {
        return ONLINE_GAMES_FUTURE_MAP.size();
    }

    public int getShipsPlacementMapSize() {
        return ONLINE_SHIPS_PLACEMENT_MAP.size();
    }

    @SneakyThrows
    public void addUserToQueue(final DataUser user) {
        USERS_QUEUE.put(user);
        ONLINE_PLAYERS_QUEUE_COUNTER.increment();
    }

    @SneakyThrows
    public void addFirstUserToQueue(final DataUser user) {
        USERS_QUEUE.putFirst(user);
    }

    public void addGame(final Long telegramUserIdPlayer1,
                        final Long telegramUserIdPlayer2,
                        final OnlineGame onlineGame) {
        Future<?> futureGame = ONLINE_GAMES_POOL.submit(onlineGame);
        ONLINE_GAMES_FUTURE_MAP.put(telegramUserIdPlayer1, futureGame);
        ONLINE_GAMES_FUTURE_MAP.put(telegramUserIdPlayer2, futureGame);
        ONLINE_GAMES_MAP.put(telegramUserIdPlayer1, onlineGame);
        ONLINE_GAMES_MAP.put(telegramUserIdPlayer2, onlineGame);
    }

    public void addShipsPlacementFuture(final Long telegramUserId, final Future<?> shipsPlacementFuture) {
        ONLINE_SHIPS_PLACEMENT_MAP.put(telegramUserId, shipsPlacementFuture);
    }

    public void removeShipsPlacement(final Long telegramUserId) {
        ONLINE_SHIPS_PLACEMENT_MAP.remove(telegramUserId);
    }

    public boolean removeUserFromQueue(DataUser user) {
        var isRemoved = USERS_QUEUE.remove(user);

        if (isRemoved) {
            ONLINE_PLAYERS_QUEUE_COUNTER.decrement();
        }

        return isRemoved;
    }

    public void interruptShipsPlacementGame(final Long telegramUserId) {
        ONLINE_SHIPS_PLACEMENT_MAP.get(telegramUserId).cancel(true);
    }

    public void interruptShipsPlacementConfirmGame(final Long telegramUserId) {
        ONLINE_GAMES_FUTURE_MAP.get(telegramUserId).cancel(true);
    }

    public void interruptMoveGame(final Long telegramUserId) {
        ONLINE_GAMES_MAP.get(telegramUserId).setInterruptPlayer(telegramUserId);
        ONLINE_GAMES_FUTURE_MAP.get(telegramUserId).cancel(true);
    }

    public void submitGameSearcher(final PlayOnlineServiceImpl.GameSearcher gameSearcher) {
        ONLINE_GAME_SEARCHER_POOL.submit(gameSearcher);
    }

    public void transferUpdate(final Long telegramUserId, final Update update) {
        var onlineGame = ONLINE_GAMES_MAP.get(telegramUserId);
        var data = update.getMessage() == null ? update.getCallbackQuery() : update.getMessage();

        if (onlineGame.getDataUserPlayer1().getUser().getTelegramUserId().equals(telegramUserId)) {
            onlineGame.transferUpdatePlayer1(telegramUserId, update);
        } else {
            onlineGame.transferUpdatePlayer2(telegramUserId, update);
        }
    }

    public void finishGame(final Long telegramUserId) {
        ONLINE_GAMES_MAP.remove(telegramUserId);
        ONLINE_GAMES_FUTURE_MAP.remove(telegramUserId);
        ONLINE_PLAYERS_QUEUE_COUNTER.decrement();
    }
}
