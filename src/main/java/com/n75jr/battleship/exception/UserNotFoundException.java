package com.n75jr.battleship.exception;

public class UserNotFoundException extends CacheException {

    public UserNotFoundException() {
    }

    public UserNotFoundException(Long telegramUserId) {
        super("Not found user with id = " + telegramUserId);
    }
}
