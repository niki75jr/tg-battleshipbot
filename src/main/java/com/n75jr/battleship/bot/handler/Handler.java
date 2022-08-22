package com.n75jr.battleship.bot.handler;

import com.n75jr.battleship.bot.BotState;

public interface Handler {

    default String name() {
        return this.getClass().getSimpleName();
    }
    BotState getState();
}
