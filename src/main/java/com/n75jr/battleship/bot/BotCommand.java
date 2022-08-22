package com.n75jr.battleship.bot;

public enum BotCommand {

    START("cmd.start"),
    MENU("cmd.menu"),
    PLAY("cmd.menu.play"),
    PROFILE("cmd.menu.profile"),
    RATING_TABLE("cmd.menu.ratingTable"),
    SETTING("cmd.menu.settings"),
    SETTING_CHANGE_LANGUAGE("cmd.menu.settings.change.language");

    private final String messagePropertyKey;

    BotCommand(final String messagePropertyKey) {
        this.messagePropertyKey = messagePropertyKey;
    }

    public String getMessagePropertyKey() {
        return messagePropertyKey;
    }
}
