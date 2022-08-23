package com.n75jr.battleship.service.game;

import com.n75jr.battleship.bot.BotCallbackButtons;
import com.n75jr.battleship.bot.BotMessages;
import com.n75jr.battleship.bot.BotState;
import com.n75jr.battleship.bot.emoji.MessageEmoji;
import com.n75jr.battleship.domain.Board;
import com.n75jr.battleship.domain.DataUser;
import com.n75jr.battleship.domain.PointShipBoardState;
import com.n75jr.battleship.service.BotMessageService;
import com.n75jr.battleship.service.PlayOfflineService;
import com.n75jr.battleship.service.UserGameService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.MessageSource;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;


public class OfflineGame extends Game implements Runnable {

    private final PlayOfflineService playOfflineService;

    public OfflineGame(PlayOfflineService playOfflineService,
                       MessageSource messageSource,
                       BotMessageService botMessageService,
                       UserGameService userGameService,
                       DataUser dataUserPlayer1,
                       long placementDelay,
                       long moveDelay) {
        super(messageSource,
                botMessageService,
                userGameService,
                dataUserPlayer1,
                null,
                placementDelay,
                moveDelay);
        this.playOfflineService = playOfflineService;
    }

    @Override
    public DataUser getDataUserPlayer2() {
        throw new UnsupportedOperationException("It's a bot");
    }

    @Override
    public void transferUpdatePlayer2(final Long telegramUserId, final Update update) {
        throw new UnsupportedOperationException("It's a bot");
    }

    @Override
    public void run() {
        gameData.isPlayer1Move = ThreadLocalRandom.current().nextBoolean();

        try {
            placeShips();
            botMessageService.sendStartingGameMessage(gameData.player1.dataUser.getUser().getTelegramUserId(),
                    gameData.player1.dataUser.getUser().getLocale());

            if (gameData.isPlayer1Move) {
                botMessageService.sendMessage(gameData.player1.dataUser.getUser().getChatId(),
                        gameData.player1.dataUser.getUser().getLocale(),
                        false, BotMessages.PLAY_GAME_MOVE_FIRST_YOUR, MessageEmoji.FIRST_MOVE.getCodeUnits());
            } else {
                botMessageService.sendMessage(gameData.player1.dataUser.getUser().getChatId(),
                        gameData.player1.dataUser.getUser().getLocale(),
                        false, BotMessages.PLAY_GAME_MOVE_FIRST_ENEMY, MessageEmoji.FIRST_MOVE.getCodeUnits());
            }

            shooting();
            evaluateAndSaveGame();
            botMessageService.sendFinishGameMessage(
                    gameData.player1.dataUser.getUser().getChatId(),
                    gameData.player1.dataUser.getUser().getLocale(),
                    gameData.player1.board.getPoints(), gameData.player2.board.getPoints(),
                    gameData.isWinPlayer1, gameData.player1.scores
            );
            gameData.player1.dataUser.setBotState(BotState.PLAY_OFFLINE_GAME);
        } catch (InterruptedException e) {
            playOfflineService.finishGame(gameData.player1.dataUser.getUser().getTelegramUserId());

            if (gameData.player1.dataUser.getBotState() != BotState.LEAVE) {
                botMessageService.sendInterruptedGameMessage(gameData.player1.dataUser.getUser().getChatId(),
                        gameData.player1.dataUser.getUser().getLocale());
            }
        } finally {
            playOfflineService.finishGame(gameData.player1.dataUser.getUser().getTelegramUserId());

            if (gameData.player1.dataUser.getBotState() != BotState.LEAVE) {
                gameData.player1.dataUser.setBotState(BotState.MAIN_MENU);
                botMessageService.sendBotApiMethod(
                        botMessageService.getMainMenuMessage(gameData.player1.dataUser.getUser().getChatId(), gameData.player1.dataUser.getUser().getLocale())
                );
            }
        }
    }

    private void placeShips() throws InterruptedException {
        var botState = gameData.player1.dataUser.getBotState();
        var isFirstShow = true;
        var editMessageTextBuilder = EditMessageText.builder();

        do {
            if (isFirstShow) {
                botMessageService.sendPlacementShipsYourBoardMessage(gameData.player1.dataUser.getUser().getChatId(),
                        gameData.player1.dataUser.getUser().getLocale(),
                        gameData.player1.board.getPoints(),
                        startTimestampLabel,
                        gameData.player1.board.getCountLiveShips(),
                        gameData.player1.shots);
                isFirstShow = false;
            } else {
                editMessageTextBuilder = EditMessageText.builder();
            }

            var update = gameData.player1.updateQueue.poll(placementDelay, TimeUnit.MILLISECONDS);
            if (update == null) {
                throw new InterruptedException();
            }

            var query = update.getCallbackQuery();
            var message = query.getMessage();
            var chatId = message.getChatId();
            var messageId = message.getMessageId();

            editMessageTextBuilder.chatId(chatId).messageId(messageId);

            if (query.getData().equals(BotCallbackButtons.PLAY_SHIPS_PLACEMENT_NEW_RANDOM.getQueryData() + startTimestampLabel)) {
                gameData.player1.board.reset();
                gameData.player1.board.randomPlaceShips();
                botMessageService.sendPlacementShipsYourBoardEditText(chatId, messageId,
                        gameData.player1.dataUser.getUser().getLocale(),
                        gameData.player1.board.getPoints(), startTimestampLabel,
                        gameData.player1.board.getCountLiveShips(), gameData.player1.shots
                );
            } else if (query.getData().equals(BotCallbackButtons.PLAY_SHIPS_PLACEMENT_CONFIRM.getQueryData() + startTimestampLabel)) {
                botState = BotState.PLAY_OFFLINE_GAME;
                gameData.player1.dataUser.setBotState(botState);
                var messageAnswerGreatChoice = messageSource.getMessage(BotMessages.PLAY_MAP_BOARD_SHIPS_GREAT_CHOICE.getKey(),
                        null,
                        gameData.player1.dataUser.getUser().getLocale());
                var answerCallbackQuery = AnswerCallbackQuery.builder()
                        .callbackQueryId(query.getId())
                        .text(messageAnswerGreatChoice)
                        .showAlert(false)
                        .build();
                botMessageService.sendBotApiMethod(answerCallbackQuery);
                botMessageService.sendPlacementShipsYourBoardEditReplyMarkup(message);
            }
        } while (botState == BotState.PLAY_OFFLINE_GAME_SHIP_PLACEMENT);
    }

    private void shotByPlayer() throws InterruptedException {
        gameData.player1.dataUser.setBotState(BotState.PLAY_OFFLINE_GAME_YOUR_MOVE);
        botMessageService.sendEnemyBoard(gameData.player1.dataUser.getUser().getChatId(),
                gameData.player1.dataUser.getUser().getLocale(),
                gameData.player2.board.getPoints(),
                gameData.player2.board.getCountLiveShips(), gameData.player1.shots);

        var update = gameData.player1.updateQueue.poll(moveDelay, TimeUnit.MILLISECONDS);

        if (update == null) {
            gameData.isPlayer1Move = false;
            gameData.player1.rowSkips++;

            if (gameData.player1.rowSkips == 2) {
                throw new InterruptedException();
            }
            return;
        }

        if (gameData.player1.rowSkips > 0) {
            gameData.player1.rowSkips = 1;
        }

        var text = update.getMessage().getText();

        if (!isCorrectEnemyCoordinate(text)) {
            botMessageService.sendDeleteMessage(update.getMessage().getChatId(), update.getMessage().getMessageId());

            return;
        }

        var x = Integer.parseInt(text.substring(1));
        var y = (text.charAt(0) - 65);
        x--;

        var shipByPoint = gameData.player2.board.getShipByPoint(x, y);

        if (shipByPoint == null) {
            gameData.player2.board.setStatePoint(x, y, PointShipBoardState.MISS);
            gameData.isPlayer1Move = false;
        } else {
            gameData.player1.hits++;
            shipByPoint.damage(x, y);
            gameData.player2.board.fillUnavailableNeighbourPoints(shipByPoint, x, y);
        }

        gameData.player1.shots++;
    }

    private static class BotMove {

        @Getter @Setter
        Board.Point pointBotHit;
        ShotDirection LEFT = new ShotDirection();
        ShotDirection UP = new ShotDirection();
        ShotDirection RIGHT = new ShotDirection();
        ShotDirection DOWN = new ShotDirection();
        List<ShotDirection> SHOT_DIRECTIONS = new ArrayList<>(List.of(LEFT, UP, RIGHT, DOWN));

        void reset() {
            SHOT_DIRECTIONS = new ArrayList<>(List.of(LEFT, UP, RIGHT, DOWN));
            LEFT.x = -1;
            UP.y = -1;
            RIGHT.x = 1;
            DOWN.y = 1;
        }

        ShotDirection getRandom() {
            return SHOT_DIRECTIONS.get(ThreadLocalRandom.current().nextInt(SHOT_DIRECTIONS.size()));
        }

        static class ShotDirection {
            short x;
            short y;
        }
    }

    private void shotByBot(final BotMove botMove) {
        gameData.player1.dataUser.setBotState(BotState.PLAY_OFFLINE_GAME_ENEMY_MOVE);
        int x;
        int y;

        if (botMove.pointBotHit == null) {
            var pointsArr = Arrays.stream(gameData.player1.board.getPoints())
                    .flatMap(Arrays::stream)
                    .filter(p -> p.getPointShipBoardState() == PointShipBoardState.INIT
                            || p.getPointShipBoardState() == PointShipBoardState.HEALTHFUL)
                    .toArray();
            var point = (Board.Point) pointsArr[ThreadLocalRandom.current().nextInt(pointsArr.length)];
            y = point.getY();
            x = point.getX();
            var shipByPoint = gameData.player1.board.getShipByPoint(x, y);

            if (shipByPoint == null) {
                gameData.player1.board.setStatePoint(x, y, PointShipBoardState.MISS);
                gameData.isPlayer1Move = true;
            } else {
                shipByPoint.damage(x, y);
                var pointShipBoardState = shipByPoint.updateState();

                if (pointShipBoardState == PointShipBoardState.WOUNDED) {
                    botMove.pointBotHit = point;
                    botMove.reset();
                }
                gameData.player1.board.fillUnavailableNeighbourPoints(shipByPoint, x, y);
            }
        } else {
            x = botMove.pointBotHit.getX();
            y = botMove.pointBotHit.getY();
            var randomDirectionShot = botMove.getRandom();
            x += randomDirectionShot.x;
            y += randomDirectionShot.y;

            if (x < 0 || x >= Board.DEFAULT_X_SIZE
                    || y < 0 || y >= Board.DEFAULT_Y_SIZE
                    || (gameData.player1.board.getPoint(y, x).getPointShipBoardState() != PointShipBoardState.INIT
                    && gameData.player1.board.getPoint(y, x).getPointShipBoardState() != PointShipBoardState.HEALTHFUL)) {
                botMove.SHOT_DIRECTIONS.remove(randomDirectionShot);
                return;
            }

            var shipByPoint = gameData.player1.board.getShipByPoint(x, y);

            if (shipByPoint == null) {
                gameData.isPlayer1Move = true;
                botMove.SHOT_DIRECTIONS.remove(randomDirectionShot);

                if (gameData.player1.board.getStateOfPoint(x, y) == PointShipBoardState.INIT) {
                    gameData.player1.board.setStatePoint(x, y, PointShipBoardState.MISS);
                }
            } else {
                shipByPoint.damage(x, y);
                var shipBoardState = shipByPoint.updateState();
                gameData.player1.board.fillUnavailableNeighbourPoints(shipByPoint, x, y);

                if (shipBoardState == PointShipBoardState.DESTROYED) {
                    botMove.pointBotHit = null;
                    gameData.player2.shots++;
                    return;
                }

                if (botMove.LEFT.equals(randomDirectionShot)) {
                        botMove.SHOT_DIRECTIONS.removeAll(List.of(botMove.UP, botMove.DOWN));
                    botMove.LEFT.x -= 1;
                } else if (botMove.RIGHT.equals(randomDirectionShot)) {
                        botMove.SHOT_DIRECTIONS.removeAll(List.of(botMove.UP, botMove.DOWN));
                    botMove.RIGHT.x += 1;
                } else if (botMove.UP.equals(randomDirectionShot)) {
                        botMove.SHOT_DIRECTIONS.removeAll(List.of(botMove.LEFT, botMove.RIGHT));
                    botMove.UP.y -= 1;
                } else if (botMove.DOWN.equals(randomDirectionShot)) {
                        botMove.SHOT_DIRECTIONS.removeAll(List.of(botMove.LEFT, botMove.RIGHT));
                    botMove.DOWN.y += 1;
                }
            }
        }

        gameData.player2.shots++;
        botMessageService.sendYouBoard(gameData.player1.dataUser.getUser().getChatId(),
                gameData.player1.dataUser.getUser().getLocale(),
                gameData.player1.board.getPoints(),
                gameData.player1.ships, gameData.player2.shots);
    }

    private void shooting() throws InterruptedException {
        var botMove = new BotMove();

        while ((gameData.player1.ships = gameData.player1.board.getCountLiveShips()) != 0
                && (gameData.player2.ships = gameData.player2.board.getCountLiveShips()) != 0
                && !Thread.currentThread().isInterrupted()) {
            if (gameData.isPlayer1Move) {
                shotByPlayer();
            } else {
                shotByBot(botMove);
            }
        }

        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
    }
}
