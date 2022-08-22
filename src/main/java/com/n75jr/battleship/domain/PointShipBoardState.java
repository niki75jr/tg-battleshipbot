package com.n75jr.battleship.domain;

public enum PointShipBoardState {

    INIT('.'),
    MISS('@'),
    UNAVAILABLE('&'),
    HEALTHFUL('#'),
    WOUNDED('*'),
    DESTROYED('X');
    private final char sign;

    PointShipBoardState(final char sign) {
        this.sign = sign;
    }

    @Override
    public String toString() {
        return String.valueOf(sign);
    }
}
