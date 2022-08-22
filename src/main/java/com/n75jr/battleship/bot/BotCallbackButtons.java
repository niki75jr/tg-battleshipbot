package com.n75jr.battleship.bot;

public enum BotCallbackButtons {

    SETTINGS_CHANGE_LANGUAGE("btn_settings_change_language"),
    SETTINGS_CHANGE_LANGUAGE_SELECT("btn_settings_change_language_select_"),
    PLAY_SHIPS_PLACEMENT_NEW_RANDOM("btn_play_ships_placement_new_random_"),
    PLAY_SHIPS_PLACEMENT_CONFIRM("btn_play_ships_placement_confirm_");

    private final String queryData;

    BotCallbackButtons(final String queryData) {
        this.queryData = queryData;
    }

    public String getQueryData() {
        return queryData;
    }
}
