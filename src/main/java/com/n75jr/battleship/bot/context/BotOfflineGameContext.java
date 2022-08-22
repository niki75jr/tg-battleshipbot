package com.n75jr.battleship.bot.context;

import com.n75jr.battleship.service.game.OfflineGame;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Component
public class BotOfflineGameContext {

    private final ExecutorService OFFLINE_GAMES_POOL = Executors.newCachedThreadPool();
    private final Map<Long, OfflineGame> OFFLINE_GAMES_MAP = new ConcurrentHashMap<>();
    private final Map<Long, Future<?>> OFFLINE_GAMES_FUTURE_MAP = new ConcurrentHashMap<>();

    @PreDestroy
    void tearDown() {
        OFFLINE_GAMES_POOL.shutdown();
    }

    public boolean isGamesPoolTerminated() {
        return OFFLINE_GAMES_POOL.isTerminated();
    }

    public boolean isGamesPoolShutdown() {
        return OFFLINE_GAMES_POOL.isShutdown();
    }

    public int getGamesMapSize() {
        return OFFLINE_GAMES_MAP.size();
    }

    public int getFutureGamesMapSize() {
        return OFFLINE_GAMES_FUTURE_MAP.size();
    }

    public void addGame(final Long telegramUserId,
                        final OfflineGame offlineGame) {
        Future<?> futureGame = OFFLINE_GAMES_POOL.submit(offlineGame);
        OFFLINE_GAMES_FUTURE_MAP.put(telegramUserId, futureGame);
        OFFLINE_GAMES_MAP.put(telegramUserId, offlineGame);
    }

    public void transferUpdate(final Long telegramUserId, final Update update) {
        OFFLINE_GAMES_MAP.get(telegramUserId)
                .transferUpdatePlayer1(telegramUserId, update);
    }

    public void interruptGame(final Long telegramUserId) {
        OFFLINE_GAMES_FUTURE_MAP.get(telegramUserId).cancel(true);
    }

    public void finishGame(final Long telegramUserId) {
        OFFLINE_GAMES_MAP.remove(telegramUserId);
        OFFLINE_GAMES_FUTURE_MAP.remove(telegramUserId);
    }
}
