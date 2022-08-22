package com.n75jr.battleship.domain;

import com.n75jr.battleship.domain.Ship.Direction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import static com.n75jr.battleship.domain.Ship.createShips;

public class Board {

    @Data
    @EqualsAndHashCode(of = {"x", "y"})
    @AllArgsConstructor(staticName = "of")
    public static class Point {
        private PointShipBoardState pointShipBoardState;
        private boolean isBoard;
        private int x;
        private int y;

        @Override
        public String toString() {
            String kind = isBoard ? "b" : "s";
            return String.format("[%s][%s][%d,%d]", kind, pointShipBoardState, x, y);
        }
    }

    public static final int DEFAULT_X_SIZE = 10;
    public static final int DEFAULT_Y_SIZE = 10;
    private final Point[][] points;
    private final Ship[] ships;

    public Board() {
        this.points = initPoints(DEFAULT_X_SIZE, DEFAULT_Y_SIZE);
        this.ships = createShips();
    }

    public Point getPoint(final int y, final int x) {
        return points[y][x];
    }

    private Point[][] initPoints(final int xSize, final int ySize) {
        Point[][] newPoints = new Point[ySize][];

        for (int y = 0; y < ySize; y++) {
            newPoints[y] = new Point[ySize];
            for (int x = 0; x < xSize; x++) {
                newPoints[y][x] = Point.of(PointShipBoardState.INIT, true, x, y);
            }
        }

        return newPoints;
    }

    public Point[][] getPoints() {
        return points;
    }

    public Ship getShipByPoint(final int x, final int y) {
        return Arrays.stream(ships)
                .filter(ship -> Arrays.stream(ship.getPoints()).
                        anyMatch(point -> point.getX() == x && point.getY() == y))
                .findAny()
                .orElse(null);
    }

    public PointShipBoardState getStateOfPoint(final int x, final int y) {
        return points[y][x].getPointShipBoardState();
    }

    public void setStatePoint(final int x, final int y, PointShipBoardState pointShipBoardState) {
        points[y][x].setPointShipBoardState(pointShipBoardState);
    }

    public short getCountLiveShips() {
        return (short) Arrays.stream(ships)
                .filter(ship -> ship.getState() != PointShipBoardState.DESTROYED)
                .count();
    }

    public void fillUnavailableNeighbourPoints(final Ship ship, final int x, final int y) {
        var direction = ship.getDirection();
        var size = ship.getSize();
        var state = ship.getState();

        if (state == PointShipBoardState.WOUNDED) {
            var countWoundedPoints = ship.getCountWoundedPoints();

            if (countWoundedPoints == 2) {
                var prevPoint = Arrays.stream(ship.getPoints())
                        .filter(point -> point.getPointShipBoardState() == PointShipBoardState.WOUNDED)
                        .filter(point -> !point.equals(points[y][x]))
                        .findAny().get();
                fillWoundedShip(prevPoint.getX(), prevPoint.getY(), direction);
                fillWoundedShip(x, y, direction);
            } else if (countWoundedPoints > 2) {
                fillWoundedShip(x, y, direction);
            }

        } else if (state == PointShipBoardState.DESTROYED) {
            fillDestroyedShip(ship.getFirstX(), ship.getFirstY(), size, direction);
        }
    }

    private void fillDestroyedShip(final int x, final int y, final int size, final Direction direction) {
        var xx = 1;
        var yy = 1;

        if (direction == Direction.HORIZONTAL) {
            xx = size;
        } else {
            yy = size;
        }

        for (var iY = -1; iY <= yy; iY++) {
            var yTmp = y + iY;

            if (yTmp < 0 || yTmp >= DEFAULT_Y_SIZE) {
                continue;
            }

            for (var iX = -1; iX <= xx; iX++) {
                var xTmp = x + iX;

                if (xTmp < 0 || xTmp >= DEFAULT_X_SIZE) {
                    continue;
                }

                var state = points[yTmp][xTmp].getPointShipBoardState();

                if (state == PointShipBoardState.INIT || state == PointShipBoardState.MISS) {
                    points[yTmp][xTmp].setPointShipBoardState(PointShipBoardState.UNAVAILABLE);
                }
            }
        }
    }

    private void fillWoundedShip(final int x, final int y, final Direction direction) {
        var tmpX = x;
        var tmpY = y;

        for (int i = -1; i <= 1; i++) {
            var tmp = 0;

            if (direction == Direction.HORIZONTAL) {
                tmpY = i + y;
                tmp = tmpY;
            } else {
                tmpX = i + x;
                tmp = tmpX;
            }

            if (tmp < 0 || i == 0 || tmp >= DEFAULT_X_SIZE) {
                continue;
            }

            var state = points[tmpY][tmpX].getPointShipBoardState();

            if (state == PointShipBoardState.INIT || state == PointShipBoardState.MISS) {
                points[tmpY][tmpX].setPointShipBoardState(PointShipBoardState.UNAVAILABLE);
            }
        }
    }

    private void placeShip(final Ship ship, final int x, final int y) {
        Point[] shipPoints = ship.getPoints();
        if (ship.getDirection() == Direction.HORIZONTAL) {
            for (int i = 0; i < shipPoints.length; i++) {
                points[y][x + i].setBoard(false);
                points[y][x + i].setPointShipBoardState(PointShipBoardState.HEALTHFUL);
                shipPoints[i] = points[y][x + i];
            }
        } else {
            for (int i = 0; i < shipPoints.length; i++) {
                points[y + i][x].setBoard(false);
                points[y + i][x].setPointShipBoardState(PointShipBoardState.HEALTHFUL);
                shipPoints[i] = points[y + i][x];
            }
        }
    }

    public void randomPlaceShips() {
        var pointsList = Arrays.stream(points)
                .flatMap(Arrays::stream)
                .filter(point -> point.getPointShipBoardState() == PointShipBoardState.INIT)
                .collect(Collectors.toList());

        for (Ship ship : ships) {
            int x;
            int y;
            int randIndex;

            do {
                randIndex = ThreadLocalRandom.current().nextInt(pointsList.size());
                var randPoint = pointsList.get(randIndex);
                x = randPoint.getX();
                y = randPoint.getY();
                int dir = (int) (Math.random() * 2);
                ship.setDirection(Direction.values()[dir]);
            } while (!isAvailablePlace(ship.getDirection(), ship.getSize(), x, y));

            placeShip(ship, x, y);
            pointsList.removeAll(List.of(ship.getPoints()));
        }
    }

    public void reset() {
        Arrays.stream(ships)
                .forEach(ship -> Arrays.stream(ship.getPoints())
                        .forEach(point -> point.setPointShipBoardState(PointShipBoardState.INIT)));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String newLine = System.lineSeparator();
        sb.append("   ");

        for (int i = 0; i < 10; i++) {
            sb.append((char) (65 + i));
        }
        sb.append(newLine);

        for (int y = 0; y < points.length; y++) {
            sb.append(String.format("%2d ", y + 1));
            for (int x = 0; x < points[y].length; x++) {
                sb.append(points[y][x].getPointShipBoardState());
            }
            sb.append(newLine);
        }
        sb.delete(sb.length() - 1, sb.length());

        return sb.toString();
    }

    public boolean isAvailablePlace(final Direction dir, final int size, final int x, final int y) {
        if (dir == Direction.HORIZONTAL) {
            if (x + size > DEFAULT_X_SIZE) {
                return false;
            }
        } else {
            if (y + size > DEFAULT_Y_SIZE) {
                return false;
            }
        }

        if (!isFreeField(x, y, size, dir)) {
            return false;
        }

        return areAvailableNeighborPointsAndCorners(x, y, size, dir);
    }

    private boolean isFreeField(final int x, final int y, final int size, final Direction dir) {
        int xx = 0;
        int yy = 0;

        for (int i = 0; i < size; i++) {
            if (dir == Direction.HORIZONTAL) {
                xx = i;
            } else {
                yy = i;
            }

            if (getStateOfPoint(x + xx, y + yy) != PointShipBoardState.INIT) {
                return false;
            }
        }

        return true;
    }

    private boolean areAvailableNeighborPointsAndCorners(final int x, final int y, final int size, final Direction dir) {
        return walkNeighborsPoints(x, y, size, dir,
                (coordX, coordY) -> (getStateOfPoint(coordX, coordY) != PointShipBoardState.INIT));
    }

    private boolean walkNeighborsPoints(final int x, final int y, final int size,
                                        final Direction dir,
                                        final BiPredicate<Integer, Integer> predicate) {
        int limitX = 1;
        int limitY = 1;

        if (dir == Direction.HORIZONTAL) {
            limitX = size;
        } else {
            limitY = size;
        }

        for (int xx = -1; xx <= limitX; xx++) {
            for (int yy = -1; yy <= limitY; yy++) {
                if (xx == 0 || yy == 0) {
                    if (xx == 0 && yy == 0) {
                        continue;
                    }
                    if (dir == Direction.HORIZONTAL) {
                        if (yy == 0 && (xx >= 0 && xx < size)) {
                            continue;
                        }
                    } else {
                        if (xx == 0 && (yy >= 0 && yy < size)) {
                            continue;
                        }
                    }
                }
                if (isOnMap(xx + x, yy + y)) {
                    if (predicate != null) {
                        if (predicate.test(xx + x, yy + y)) {
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    private boolean isOnMap(final int x, final int y) {
        return x >= 0 && y >= 0 && x < DEFAULT_X_SIZE && y < DEFAULT_Y_SIZE;
    }
}
