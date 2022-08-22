package com.n75jr.battleship.util;

import com.n75jr.battleship.bot.emoji.BoardEmoji;
import com.n75jr.battleship.domain.Board;
import com.n75jr.battleship.domain.PointShipBoardState;
import lombok.experimental.UtilityClass;

import java.util.Arrays;

@UtilityClass
public class BoardUtils {

    public static BoardEmoji toBoardEmoji(final PointShipBoardState pointShipBoardState) {
        switch (pointShipBoardState) {
            case INIT:
                return BoardEmoji.INIT;
            case MISS:
                return BoardEmoji.MISS;
            case UNAVAILABLE:
                return BoardEmoji.UNAVAILABLE;
            case HEALTHFUL:
                return BoardEmoji.HEALTHFUL;
            case WOUNDED:
                return BoardEmoji.WOUNDED;
            case DESTROYED:
                return BoardEmoji.DESTROYED;
            default:
                throw new RuntimeException();
        }
    }

    public static String toYourBoardString(final Board.Point[][] points) {
        var sb = new StringBuilder();
        Arrays.stream(points)
                .forEach(pointsRow -> {
                    Arrays.stream(pointsRow)
                            .forEach(point -> sb.append(BoardUtils.toBoardEmoji(point.getPointShipBoardState())));
                    sb.append(System.lineSeparator());
                });

        return sb.toString();
    }

    public static String toEnemyBoardCeilString(final Board.Point point) {
        var pointState = point.getPointShipBoardState();

        if (pointState == PointShipBoardState.INIT || pointState == PointShipBoardState.HEALTHFUL) {
            return (char) (65 + point.getY()) + "" + (point.getX() + 1);
        } else {
            return BoardUtils.toBoardEmoji(pointState).getCodeUnits();
        }
    }
}
