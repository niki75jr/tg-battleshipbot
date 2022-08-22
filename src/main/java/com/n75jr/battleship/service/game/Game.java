package com.n75jr.battleship.service.game;

import com.n75jr.battleship.domain.Board;
import com.n75jr.battleship.domain.DataUser;
import com.n75jr.battleship.domain.UserGame;
import com.n75jr.battleship.service.BotMessageService;
import com.n75jr.battleship.service.UserGameService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.MessageSource;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Pattern;

@Log4j2
public abstract class Game {

    public enum BotDirectionShot {
        LEFT(0, -1),
        UP(-1, 0),
        RIGHT(0, 1),
        DOWN(1, 0);

        private final int[] deltaArr;
        private boolean isPrevSuccess;

        BotDirectionShot(int... delta) {
            this.deltaArr = delta;
            isPrevSuccess = false;
        }

        public void setPrevToSeccess() {
            isPrevSuccess = true;
        }

        public boolean isPrevSuccess() {
            return isPrevSuccess;
        }

        public int[] getDeltaArr() {
            return deltaArr;
        }

        public int getY() {
            return deltaArr[0];
        }

        public int getX() {
            return deltaArr[1];
        }
    }

    public final static long DEFAULT_PLACEMENT_DELAY = 30_000L;
    public final static long DEFAULT_MOVE_DELAY = 30_000L;
    public final static short GAME_SCORES_WIN = 20;
    protected final MessageSource messageSource;
    protected final BotMessageService botMessageService;
    protected final UserGameService userGameService;
    private final DataUser dataUserPlayer1;
    private final DataUser dataUserPlayer2;
    protected final GameData gameData;
    protected final long placementDelay;
    protected final long moveDelay;
    protected final String startTimestampLabel;

    public Game(MessageSource messageSource,
                BotMessageService botMessageService,
                UserGameService userGameService,
                DataUser dataUserPlayer1,
                DataUser dataUserPlayer2,
                long placementDelay,
                long moveDelay) {
        this.messageSource = messageSource;
        this.botMessageService = botMessageService;
        this.userGameService = userGameService;
        this.dataUserPlayer1 = dataUserPlayer1;
        this.dataUserPlayer2 = dataUserPlayer2;
        this.placementDelay = placementDelay;
        this.moveDelay = moveDelay;
        this.startTimestampLabel = Long.toString(Instant.now().getEpochSecond(), 32);

        if (log.isDebugEnabled()) {
            log.debug("OfflineGame<init>: creating board1");
        }

        var boardPlayer1 = new Board();
        boardPlayer1.randomPlaceShips();

        if (log.isDebugEnabled()) {
            log.debug("OfflineGame<init>: creating board2");
        }

        var boardPlayer2 = new Board();
        boardPlayer2.randomPlaceShips();

        if (log.isDebugEnabled()) {
            log.debug("OfflineGame<init>: boards are created");
        }

        this.gameData = new GameData(
                dataUserPlayer1, boardPlayer1,
                dataUserPlayer2, boardPlayer2
        );
    }

    public void transferUpdatePlayer1(final Long telegramUserId, final Update update) {
        gameData.player1.updateQueue.offer(update);
    }
    public void transferUpdatePlayer2(final Long telegramUserId, final Update update) {
        gameData.player2.updateQueue.offer(update);
    }

    public DataUser getDataUserPlayer1() {
        return gameData.player1.dataUser;
    }

    public DataUser getDataUserPlayer2() {
        return gameData.player2.dataUser;
    }

    protected boolean isCorrectEnemyCoordinate(final String coordinate) {
        return Pattern.compile("[A-J]([1-9]|10)").matcher(coordinate).matches();
    }

    protected void evaluateAndSaveGame() {
        gameData.isWinPlayer1 = gameData.player1.ships != 0;
        gameData.player1.scores = gameData.player1.hits;
        gameData.player2.scores = gameData.player2.hits;

        if (gameData.isWinPlayer1) {
            gameData.player1.scores += GAME_SCORES_WIN;
        } else {
            gameData.player2.scores += GAME_SCORES_WIN;
        }

        var userGames = new ArrayList<UserGame>(2);
        var opponentTelegramUserId = gameData.player2.dataUser == null
                ? 0
                : gameData.player2.dataUser.getUser().getTelegramUserId();
        var userGamePlayer1 = UserGame.builder()
                .userRecordId(gameData.player1.dataUser.getUser())
                .isWin(gameData.isWinPlayer1)
                .opponentId(opponentTelegramUserId)
                .totalShots(gameData.player1.shots)
                .scoresGame(gameData.player1.scores)
                .build();

        if (gameData.player2.dataUser != null) {
            var userGamePlayer2 = UserGame.builder()
                    .opponentId(gameData.player2.dataUser.getUser().getTelegramUserId())
                    .isWin(!gameData.isWinPlayer1)
                    .totalShots(gameData.player2.shots)
                    .scoresGame(gameData.player2.scores)
                    .build();
            userGames.add(userGamePlayer2);
        }

        userGames.add(userGamePlayer1);
        userGameService.saveAll(userGames);
    }

    protected static class GameData {

        final PlayerData player1;
        final PlayerData player2;
        boolean isWinPlayer1;
        boolean isPlayer1Move;

        public GameData(DataUser dataUserPlayer1, Board boardPlayer1,
                        DataUser dataUserPlayer2, Board boardPlayer2) {
            player1 = PlayerData.of(dataUserPlayer1, new ArrayBlockingQueue<>(1), boardPlayer1);
            player2 = PlayerData.of(dataUserPlayer2, new ArrayBlockingQueue<>(1), boardPlayer2);
        }

        @RequiredArgsConstructor(staticName = "of")
        protected static class PlayerData {
            final DataUser dataUser;
            final BlockingQueue<Update> updateQueue;
            final Board board;
            short ships;
            short shots;
            short hits;
            short scores;
            short rowSkips;
        }
    }
}
