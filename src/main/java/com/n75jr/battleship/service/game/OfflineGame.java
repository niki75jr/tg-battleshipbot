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
import org.springframework.context.MessageSource;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


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

    private void shotByBot(Board.Point pointBotHit,
                           List<BotDirectionShot> deltaList) {
        gameData.player1.dataUser.setBotState(BotState.PLAY_OFFLINE_GAME_ENEMY_MOVE);
        int y;
        int x;

        if (pointBotHit == null) {
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
                    pointBotHit = point;
                    deltaList = Arrays.stream(BotDirectionShot.values())
                            .peek(botDirectionShot -> {
                                var delta = botDirectionShot.getDeltaArr();
                                for (int i = 0; i < delta.length; i++) {
                                    if (delta[i] > 0) {
                                        delta[i] = 1;
                                    } else if (delta[i] < 0) {
                                        delta[i] = -1;
                                    }
                                }
                            })
                            .collect(Collectors.toList());
                }
                gameData.player1.board.fillUnavailableNeighbourPoints(shipByPoint, x, y);
            }
        } else {
            y = pointBotHit.getY();
            x = pointBotHit.getX();
            var directionShot = deltaList.get(ThreadLocalRandom.current().nextInt(deltaList.size()));
            y += directionShot.getY();
            x += directionShot.getX();

            if (x < 0 || x >= Board.DEFAULT_X_SIZE
                    || y < 0 || y >= Board.DEFAULT_Y_SIZE
                    || (gameData.player1.board.getPoint(y, x).getPointShipBoardState() != PointShipBoardState.INIT
                    && gameData.player1.board.getPoint(y, x).getPointShipBoardState() != PointShipBoardState.HEALTHFUL)) {
                deltaList.remove(directionShot);
                return;
            }

            var shipByPoint = gameData.player1.board.getShipByPoint(x, y);

            if (shipByPoint == null) {
                gameData.isPlayer1Move = true;
                deltaList.remove(directionShot);

                if (gameData.player1.board.getStateOfPoint(x, y) == PointShipBoardState.INIT) {
                    gameData.player1.board.setStatePoint(x, y, PointShipBoardState.MISS);
                }
            } else {
                shipByPoint.damage(x, y);
                shipByPoint.updateState();
                gameData.player1.board.fillUnavailableNeighbourPoints(shipByPoint, x, y);

                if (shipByPoint.getState() == PointShipBoardState.DESTROYED) {
                    pointBotHit = null;
                    gameData.player2.shots++;
                    return;
                }

                directionShot.setPrevToSeccess();

                switch (directionShot) {
                    case LEFT:
                        if (directionShot.isPrevSuccess())
                            deltaList.removeAll(List.of(BotDirectionShot.UP, BotDirectionShot.DOWN));
                        directionShot.getDeltaArr()[1] -= 1;
                        break;
                    case RIGHT:
                        if (directionShot.isPrevSuccess())
                            deltaList.removeAll(List.of(BotDirectionShot.UP, BotDirectionShot.DOWN));
                        directionShot.getDeltaArr()[1] += 1;
                        break;
                    case UP:
                        if (directionShot.isPrevSuccess())
                            deltaList.removeAll(List.of(BotDirectionShot.LEFT, BotDirectionShot.RIGHT));
                        directionShot.getDeltaArr()[0] -= 1;
                        break;
                    case DOWN:
                        if (directionShot.isPrevSuccess())
                            deltaList.removeAll(List.of(BotDirectionShot.LEFT, BotDirectionShot.RIGHT));
                        directionShot.getDeltaArr()[0] += 1;
                        break;
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
        Board.Point pointBotHit = null;
        List<BotDirectionShot> deltaList = null;

        while ((gameData.player1.ships = gameData.player1.board.getCountLiveShips()) != 0
                && (gameData.player2.ships = gameData.player2.board.getCountLiveShips()) != 0
                && !Thread.currentThread().isInterrupted()) {
            if (gameData.isPlayer1Move) {
                shotByPlayer();
            } else {
                shotByBot(pointBotHit, deltaList);
            }
        }

        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
    }
}
