package com.n75jr.battleship.exception;

public class UserHasNotTelegramIdException extends RuntimeException {

    public UserHasNotTelegramIdException() {
        super();
    }

    public UserHasNotTelegramIdException(String message) {
        super(message);
    }
}
