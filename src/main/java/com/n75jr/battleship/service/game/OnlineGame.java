package com.n75jr.battleship.service.game;

import com.n75jr.battleship.bot.BotCallbackButtons;
import com.n75jr.battleship.bot.BotMessages;
import com.n75jr.battleship.bot.BotState;
import com.n75jr.battleship.bot.context.BotOnlineGameContext;
import com.n75jr.battleship.bot.emoji.MessageEmoji;
import com.n75jr.battleship.domain.Board;
import com.n75jr.battleship.domain.DataUser;
import com.n75jr.battleship.domain.PointShipBoardState;
import com.n75jr.battleship.domain.UserGame;
import com.n75jr.battleship.service.BotMessageService;
import com.n75jr.battleship.service.UserGameService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.TriConsumer;
import org.springframework.context.MessageSource;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class OnlineGame extends Game implements Runnable {

    public final static float GAME_SCORES_WIN_INTERRUPT_SCALE = 2.0F;
    public final static short GAME_SCORES_WIN_INTERRUPT_MIN = 10;
    private final static Update UPDATE_DUMMY = new Update();
    private final BotOnlineGameContext botOnlineGameContext;
    private Long interruptPlayer;

    public OnlineGame(
            MessageSource messageSource,
            UserGameService userGameService,
            BotMessageService botMessageService,
            BotOnlineGameContext botOnlineGameContext,
            DataUser dataUserPlayer1,
            DataUser dataUserPlayer2,
            long placementDelay,
            long moveDelay) {
        super(messageSource,
                botMessageService,
                userGameService,
                dataUserPlayer1,
                dataUserPlayer2,
                placementDelay,
                moveDelay);
        this.botOnlineGameContext = botOnlineGameContext;
    }

    public void setInterruptPlayer(final Long telegramUserId) {
        if (this.interruptPlayer == null) {
            this.interruptPlayer = telegramUserId;
        }
    }

    @Override
    public void run() {
        var telegramUserIdPlayer1 = gameData.player1.dataUser.getUser().getTelegramUserId();
        var telegramUserIdPlayer2 = gameData.player2.dataUser.getUser().getTelegramUserId();
        var chatIdPlayer1 = gameData.player1.dataUser.getUser().getChatId();
        var chatIdPlayer2 = gameData.player2.dataUser.getUser().getChatId();
        var localePlayer1 = gameData.player1.dataUser.getUser().getLocale();
        var localePlayer2 = gameData.player2.dataUser.getUser().getLocale();
        var placements = (Future<?>[]) new Future[]{null, null};
        placements[0] = getPlacementFuture(placements, true);
        placements[1] = getPlacementFuture(placements, false);

        botOnlineGameContext.addShipsPlacementFuture(telegramUserIdPlayer1, placements[0]);
        botOnlineGameContext.addShipsPlacementFuture(telegramUserIdPlayer2, placements[1]);

        try {
            try {
                placements[0].get();
                placements[1].get();
            } catch (CancellationException e) {
                throw new InterruptedException();
            } catch (InterruptedException e) {
                throw e;
            } catch (ExecutionException e) {
                var cause = e.getCause();

                if ((cause = e.getCause()) != null) {
                    var causeClass = cause.getClass();

                    if (causeClass == TimeoutException.class) {
                        if (gameData.player1.dataUser.getBotState() == BotState.PLAY_ONLINE_GAME_SHIPS_PLACEMENT) {
                            gameData.player1.updateQueue.offer(UPDATE_DUMMY);

                            if (gameData.player1.dataUser.getBotState() != BotState.PLAY_ONLINE_GAME_SHIPS_PLACEMENT_CONFIRM) {
                                botMessageService.sendMessage(chatIdPlayer1, localePlayer1, false,
                                        BotMessages.PLAY_MAP_BOARD_SHIPS_TIME_IS_UP, MessageEmoji.TIME_IS_UP);
                            }
                        }

                        if (gameData.player2.dataUser.getBotState() == BotState.PLAY_ONLINE_GAME_SHIPS_PLACEMENT) {
                            gameData.player2.updateQueue.offer(UPDATE_DUMMY);

                            if (gameData.player2.dataUser.getBotState() != BotState.PLAY_ONLINE_GAME_SHIPS_PLACEMENT_CONFIRM) {
                                botMessageService.sendMessage(chatIdPlayer2, localePlayer2, false,
                                        BotMessages.PLAY_MAP_BOARD_SHIPS_TIME_IS_UP, MessageEmoji.TIME_IS_UP);
                            }
                        }
                    }
                } else {
                    throw new InterruptedException();
                }
            } catch (Exception e) {
                throw new InterruptedException();
            } finally {
                botOnlineGameContext.removeShipsPlacement(telegramUserIdPlayer1);
                botOnlineGameContext.removeShipsPlacement(telegramUserIdPlayer2);
            }

            gameData.isPlayer1Move = ThreadLocalRandom.current().nextBoolean();
            botMessageService.sendMessage(chatIdPlayer1, localePlayer1, false, BotMessages.PLAY_STARTING);
            botMessageService.sendMessage(chatIdPlayer2, localePlayer2, false, BotMessages.PLAY_STARTING);

            if (gameData.isPlayer1Move) {
                botMessageService.sendMessage(chatIdPlayer1, localePlayer1,
                        false, BotMessages.PLAY_GAME_MOVE_FIRST_YOUR, MessageEmoji.FIRST_MOVE.getCodeUnits());
                botMessageService.sendMessage(chatIdPlayer2, localePlayer2,
                        false, BotMessages.PLAY_GAME_MOVE_FIRST_ENEMY, MessageEmoji.FIRST_MOVE.getCodeUnits());
            } else {
                botMessageService.sendMessage(chatIdPlayer2, localePlayer2,
                        false, BotMessages.PLAY_GAME_MOVE_FIRST_YOUR, MessageEmoji.FIRST_MOVE.getCodeUnits());
                botMessageService.sendMessage(chatIdPlayer1, localePlayer1,
                        false, BotMessages.PLAY_GAME_MOVE_FIRST_ENEMY, MessageEmoji.FIRST_MOVE.getCodeUnits());
            }

            gameData.player1.dataUser.setBotState(BotState.PLAY_ONLINE_GAME_ENEMY_MOVE);
            gameData.player2.dataUser.setBotState(BotState.PLAY_ONLINE_GAME_ENEMY_MOVE);
            shooting();
            evaluateAndSaveGame();
            sendResultGameMessage(gameData.player1, gameData.player2, gameData.isWinPlayer1);
            sendResultGameMessage(gameData.player2, gameData.player1, !gameData.isWinPlayer1);

        } catch (InterruptedException e) {
            if (telegramUserIdPlayer1.equals(interruptPlayer) && gameData.player2.shots != 0) {
                sendTechnicalResultGameMessage(gameData.player2, gameData.player1);
                evaluateAndSaveTechnicalEndGame(gameData.player2, gameData.player1);
            }
            if (telegramUserIdPlayer2.equals(interruptPlayer) && gameData.player1.shots != 0) {
                sendTechnicalResultGameMessage(gameData.player1, gameData.player2);
                evaluateAndSaveTechnicalEndGame(gameData.player1, gameData.player2);
            }

            if (gameData.player1.dataUser.getBotState() != BotState.LEAVE) {
                botMessageService.sendInterruptedGameMessage(chatIdPlayer1, localePlayer1);
            }
            if (gameData.player2.dataUser.getBotState() != BotState.LEAVE) {
                botMessageService.sendInterruptedGameMessage(chatIdPlayer2, localePlayer2);
            }
        } finally {
            botOnlineGameContext.finishGame(telegramUserIdPlayer1);
            botOnlineGameContext.finishGame(telegramUserIdPlayer2);

            if (gameData.player1.dataUser.getBotState() != BotState.LEAVE) {
                gameData.player1.dataUser.setBotState(BotState.MAIN_MENU);
                botMessageService.sendBotApiMethod(
                        botMessageService.getMainMenuMessage(chatIdPlayer1, localePlayer1)
                );
            }

            if (gameData.player2.dataUser.getBotState() != BotState.LEAVE) {
                gameData.player2.dataUser.setBotState(BotState.MAIN_MENU);
                botMessageService.sendBotApiMethod(
                        botMessageService.getMainMenuMessage(chatIdPlayer2, localePlayer2)
                );
            }
        }
    }

    private final TriConsumer<DataUser, Future<?>[], Integer> placementShipsConsumer = (dataUser, submits, submitIndex) -> {
        try {
            var telegramUserId = dataUser.getUser().getTelegramUserId();
            placeShips(telegramUserId);

            if (!submits[submitIndex].isDone()) {
                botMessageService.sendMessage(dataUser.getUser().getChatId(),
                        dataUser.getUser().getLocale(),
                        BotMessages.PLAY_GAME_ONLINE_WAITING.getKey(), null);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    };

    @RequiredArgsConstructor
    private static class PlacementShipsPlayer2InvocationHandler implements InvocationHandler {

        private final Future<?> playerFuture;
        private final Future<?> opponentFuture;

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            var methodName = method.getName();

            if (methodName.equals("cancel")) {
                if (!opponentFuture.isCancelled()) {
                    opponentFuture.cancel(true);
                }
            } else if (methodName.equals("get") && args == null) {
                return playerFuture.get();
            }

            return method.invoke(playerFuture, args);
        }
    }

    private void placeShips(final Long telegramUserId) throws InterruptedException {
        DataUser dataUser = null;
        Board board = null;
        BlockingQueue<org.telegram.telegrambots.meta.api.objects.Update> updateQueue;

        if (telegramUserId.equals(gameData.player1.dataUser.getUser().getTelegramUserId())) {
            dataUser = gameData.player1.dataUser;
            board = gameData.player1.board;
            updateQueue = gameData.player1.updateQueue;
        } else {
            dataUser = gameData.player2.dataUser;
            board = gameData.player2.board;
            updateQueue = gameData.player2.updateQueue;
        }

        var botState = dataUser.getBotState();
        var isFirstShow = true;
        var editMessageTextBuilder = EditMessageText.builder();

        do {
            if (isFirstShow) {
                botMessageService.sendPlacementShipsYourBoardMessage(dataUser.getUser().getChatId(),
                        dataUser.getUser().getLocale(),
                        board.getPoints(),
                        startTimestampLabel,
                        board.getCountLiveShips(),
                        gameData.player1.shots);
                isFirstShow = false;
            } else {
                editMessageTextBuilder = EditMessageText.builder();
            }
            var update = updateQueue.take();

            if (update.equals(UPDATE_DUMMY)) {
                break;
            }

            var query = update.getCallbackQuery();
            var message = query.getMessage();
            var chatId = message.getChatId();
            var messageId = message.getMessageId();

            editMessageTextBuilder.chatId(chatId).messageId(messageId);


            if (query.getData().equals(BotCallbackButtons.PLAY_SHIPS_PLACEMENT_NEW_RANDOM.getQueryData() + startTimestampLabel)) {
                board.reset();
                board.randomPlaceShips();
                botMessageService.sendPlacementShipsYourBoardEditText(chatId, messageId,
                        dataUser.getUser().getLocale(),
                        board.getPoints(), startTimestampLabel,
                        board.getCountLiveShips(), gameData.player1.shots
                );
            } else if (query.getData().equals(BotCallbackButtons.PLAY_SHIPS_PLACEMENT_CONFIRM.getQueryData() + startTimestampLabel)) {
                botState = BotState.PLAY_ONLINE_GAME_SHIPS_PLACEMENT_CONFIRM;
                dataUser.setBotState(botState);

                var messageAnswerGreatChoice = messageSource.getMessage(BotMessages.PLAY_MAP_BOARD_SHIPS_GREAT_CHOICE.getKey(),
                        null,
                        dataUser.getUser().getLocale());
                var answerCallbackQuery = AnswerCallbackQuery.builder()
                        .callbackQueryId(query.getId())
                        .text(messageAnswerGreatChoice)
                        .showAlert(false)
                        .build();

                botMessageService.sendBotApiMethod(answerCallbackQuery);
                botMessageService.sendPlacementShipsYourBoardEditReplyMarkup(message);
                break;
            }
        } while (botState == BotState.PLAY_ONLINE_GAME_SHIPS_PLACEMENT);
    }

    private void shooting() throws InterruptedException {
        if (gameData.isPlayer1Move) {
            botMessageService.sendEnemyBoard(
                    gameData.player1.dataUser.getUser().getChatId(),
                    gameData.player1.dataUser.getUser().getLocale(),
                    gameData.player2.board.getPoints(),
                    gameData.player2.board.getCountLiveShips(), gameData.player1.shots);
        } else {
            botMessageService.sendEnemyBoard(
                    gameData.player2.dataUser.getUser().getChatId(),
                    gameData.player2.dataUser.getUser().getLocale(),
                    gameData.player1.board.getPoints(),
                    gameData.player1.board.getCountLiveShips(), gameData.player2.shots);
        }

        while ((gameData.player1.ships = gameData.player1.board.getCountLiveShips()) != 0
                && (gameData.player2.ships = gameData.player2.board.getCountLiveShips()) != 0
                && !Thread.currentThread().isInterrupted()) {
            if (gameData.isPlayer1Move) {
                shot(gameData,
                        gameData.player1, gameData.player1.updateQueue,
                        gameData.player2, (short) moveDelay, true);
            } else {
                shot(gameData,
                        gameData.player2, gameData.player2.updateQueue,
                        gameData.player1, (short) moveDelay, false);
            }
        }

        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
    }

    private void shot(final GameData gameData,
                      final GameData.PlayerData playerData,
                      final BlockingQueue<Update> updateQueuePlayer,
                      final GameData.PlayerData opponentData,
                      final short shootingDelay,
                      final boolean isPlayer1) throws InterruptedException {
        var nextMove = true;
        playerData.dataUser.setBotState(BotState.PLAY_ONLINE_GAME_YOUR_MOVE);

        var update = updateQueuePlayer.poll(shootingDelay, TimeUnit.MILLISECONDS);

        if (update == null) {
            gameData.isPlayer1Move = !isPlayer1;
            playerData.rowSkips++;

            if (playerData.rowSkips == 2) {
                setInterruptPlayer(playerData.dataUser.getUser().getTelegramUserId());
                throw new InterruptedException();
            }

            botMessageService.sendMessage(
                    playerData.dataUser.getUser().getChatId(),
                    playerData.dataUser.getUser().getLocale(),
                    BotMessages.PLAY_GAME_MOVE_SKIP.getKey(),
                    null,
                    MessageEmoji.TIME_IS_UP.getCodeUnits());
            gameData.isPlayer1Move = !isPlayer1;
            playerData.dataUser.setBotState(BotState.PLAY_ONLINE_GAME_ENEMY_MOVE);
            opponentData.dataUser.setBotState(BotState.PLAY_ONLINE_GAME_YOUR_MOVE);
            botMessageService.sendEnemyBoard(
                    opponentData.dataUser.getUser().getChatId(),
                    opponentData.dataUser.getUser().getLocale(),
                    playerData.board.getPoints(),
                    playerData.board.getCountLiveShips(), opponentData.shots);

            return;
        }

        if (playerData.rowSkips > 0) {
            playerData.rowSkips = 0;
        }

        var text = update.getMessage().getText();

        if (!isCorrectEnemyCoordinate(text)) {
            botMessageService.sendDeleteMessage(update.getMessage().getChatId(), update.getMessage().getMessageId());

            return;
        }

        var x = Integer.parseInt(text.substring(1));
        var y = (text.charAt(0) - 65);
        x--;
        var shipByPoint = opponentData.board.getShipByPoint(x, y);

        if (shipByPoint == null) {
            gameData.isPlayer1Move = !isPlayer1;
            playerData.dataUser.setBotState(BotState.PLAY_ONLINE_GAME_ENEMY_MOVE);
            opponentData.board.setStatePoint(x, y, PointShipBoardState.MISS);
            botMessageService.sendMissMessage(playerData.dataUser.getUser().getChatId(),
                    playerData.dataUser.getUser().getLocale(),
                    opponentData.board.getPoints());
            nextMove = false;
        } else {
            playerData.hits++;
            shipByPoint.damage(x, y);
            opponentData.board.fillUnavailableNeighbourPoints(shipByPoint, x, y);
            botMessageService.sendEnemyBoard(playerData.dataUser.getUser().getChatId(),
                    playerData.dataUser.getUser().getLocale(),
                    opponentData.board.getPoints(),
                    opponentData.board.getCountLiveShips(), playerData.shots);
        }

        playerData.shots++;
        botMessageService.sendYouBoard(opponentData.dataUser.getUser().getChatId(),
                opponentData.dataUser.getUser().getLocale(),
                opponentData.board.getPoints(),
                opponentData.board.getCountLiveShips(),
                playerData.shots);

        if (!nextMove) {
            botMessageService.sendEnemyBoard(opponentData.dataUser.getUser().getChatId(),
                    opponentData.dataUser.getUser().getLocale(),
                    playerData.board.getPoints(),
                    playerData.board.getCountLiveShips(), opponentData.shots);
        }
    }

    private void evaluateAndSaveTechnicalEndGame(GameData.PlayerData winner, GameData.PlayerData interrupter) {
        var gameScoresWinner = (short) (winner.hits * GAME_SCORES_WIN_INTERRUPT_SCALE);
        var gameScoresInterrupter = interrupter.scores;

        if (gameScoresWinner == 0) {
            gameScoresWinner = GAME_SCORES_WIN_INTERRUPT_MIN;
            gameScoresInterrupter = GAME_SCORES_WIN_INTERRUPT_MIN - 1;
        }

        var userGameWinner = UserGame.builder()
                .userRecordId(winner.dataUser.getUser())
                .isWin(true)
                .opponentId(interrupter.dataUser.getUser().getTelegramUserId())
                .totalShots(winner.shots)
                .scoresGame(gameScoresWinner)
                .build();
        var userGameInterrupter = UserGame.builder()
                .userRecordId(interrupter.dataUser.getUser())
                .isWin(false)
                .opponentId(winner.dataUser.getUser().getTelegramUserId())
                .totalShots(interrupter.shots)
                .scoresGame(gameScoresInterrupter)
                .build();

        userGameService.saveAll(List.of(userGameWinner, userGameInterrupter));
    }

    private Future<?> getPlacementFuture(final Future<?>[] placements, final boolean isPlayer1) {
        if (isPlayer1) {
            return getPlacementShipsFuture(placements);
        } else {
            return getPlacementShipsFutureProxy(placements);
        }
    }

    private Future<?> getPlacementShipsFutureProxy(final Future<?>[] placements) {
        return (Future<?>) Proxy.newProxyInstance(OnlineGame.class.getClassLoader(),
                CompletableFuture.class.getInterfaces(),
                new PlacementShipsPlayer2InvocationHandler(CompletableFuture.runAsync
                        (() -> placementShipsConsumer.accept(gameData.player2.dataUser, placements, 0),
                                botOnlineGameContext.getONLINE_SHIPS_PLACEMENT_POOL()).orTimeout(placementDelay, TimeUnit.MILLISECONDS), placements[0]));
    }

    private Future<?> getPlacementShipsFuture(final Future<?>[] placements) {
        return CompletableFuture.runAsync(() -> placementShipsConsumer.accept(gameData.player1.dataUser, placements, 1),
                botOnlineGameContext.getONLINE_SHIPS_PLACEMENT_POOL()
        ).orTimeout(placementDelay, TimeUnit.MILLISECONDS);
    }

    private void sendResultGameMessage(final GameData.PlayerData winner, final GameData.PlayerData loser, final boolean isWin) {
        botMessageService.sendFinishGameMessage(
                winner.dataUser.getUser().getChatId(),
                winner.dataUser.getUser().getLocale(),
                winner.board.getPoints(),
                loser.board.getPoints(),
                isWin,
                winner.scores
        );
    }

    private void sendTechnicalResultGameMessage(final GameData.PlayerData winner, final GameData.PlayerData loser) {
        botMessageService.sendMessage(
                winner.dataUser.getUser().getTelegramUserId(),
                winner.dataUser.getUser().getLocale(),
                false,
                BotMessages.PLAY_GAME_TECHNICAL_WIN
        );
        botMessageService.sendMessage(
                loser.dataUser.getUser().getTelegramUserId(),
                loser.dataUser.getUser().getLocale(),
                false,
                BotMessages.PLAY_GAME_TECHNICAL_LOSE
        );
    }
}
