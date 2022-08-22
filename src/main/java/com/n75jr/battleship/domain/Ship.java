package com.n75jr.battleship.domain;

import com.n75jr.battleship.domain.Board.Point;

import java.util.Arrays;

public class Ship {

    public enum Direction {

        HORIZONTAL('H'),
        VERTICAL('V');

        private final char shortState;

        Direction(final char abbreviation) {
            this.shortState = abbreviation;
        }

        public char getShortState() {
            return shortState;
        }
    }

    public static final int DEFAULT_MAX_SIZE = 4;
    private final Point[] points;
    private PointShipBoardState pointShipBoardState;
    private Direction direction;
    private final int size;

    public Ship(int size) {
        this.points = new Point[size];
        this.size = size;
        this.pointShipBoardState = PointShipBoardState.HEALTHFUL;
        this.direction = Direction.HORIZONTAL;
    }

    public static Ship[] createShips() {
        int arrSize = getCountShips(DEFAULT_MAX_SIZE);
        Ship[] ships = new Ship[arrSize];
        int counter = 0;
        for (int i = 0; i < DEFAULT_MAX_SIZE; i++) {
            for (int j = 0; j < DEFAULT_MAX_SIZE - i; j++) {
                ships[counter++] = new Ship(i + 1);
            }
        }
        return ships;
    }

    public Point[] getPoints() {
        return points;
    }

    private static int getCountShips(final int maxShipSize) {
        int n = 0;
        for (int i = 0; i < maxShipSize; i++) {
            n += (maxShipSize - i);
        }

        return n;
    }

    public void destroy() {
        Arrays.stream(points)
                .forEach(point -> point.setPointShipBoardState(PointShipBoardState.DESTROYED));
    }

    public int getCountWoundedPoints() {
        return (int) Arrays.stream(points)
                .filter(point -> point.getPointShipBoardState() == PointShipBoardState.WOUNDED)
                .count();
    }

    public void damage(final int x, final int y) {
        if (pointShipBoardState != PointShipBoardState.DESTROYED) {
            var p = Arrays.stream(points)
                    .filter(point -> point.getX() == x && point.getY() == y)
                    .findAny().get();
            if (p.getPointShipBoardState() == PointShipBoardState.HEALTHFUL) {
                p.setPointShipBoardState(PointShipBoardState.WOUNDED);
                updateState();
            }
        }
    }

    public PointShipBoardState getState() {
        return pointShipBoardState;
    }

    public PointShipBoardState updateState() {
        if (pointShipBoardState != PointShipBoardState.DESTROYED) {
            int nLives = (int) Arrays.stream(points)
                    .filter(point -> point.getPointShipBoardState() == PointShipBoardState.HEALTHFUL)
                    .count();
            if (nLives == 0) {
                pointShipBoardState = PointShipBoardState.DESTROYED;
                destroy();
            } else if (nLives < size) {
                pointShipBoardState = PointShipBoardState.WOUNDED;
            }
        }

        return pointShipBoardState;
    }

    public Direction getDirection() {
        return direction;
    }

    public int getSize() {
        return size;
    }

    public int getFirstX() {
        return points[0].getX();
    }

    public int getFirstY() {
        return points[0].getY();
    }

    public void setDirection(final Direction direction) {
        this.direction = direction;
    }

    @Override
    public String toString() {
        return String.format("[%d][%c][%c,%d][%s]",
                size,
                direction.getShortState(),
                (65 + getFirstX()),
                getFirstY(),
                pointShipBoardState);
    }
}
