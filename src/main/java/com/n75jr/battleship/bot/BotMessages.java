package com.n75jr.battleship.bot;

public enum BotMessages {

    // -----------------------------------------------------------------------------------------------------------------
    UNEXPECTED_ERROR("msg.unexpectedError"),
    CMD_START("cmd.start"),
    CMD_MENU_PLAY("cmd.menu.play"),
    CMD_MENU_PLAY_MODE_OFFLINE("cmd.menu.play.mode.offline"),
    CMD_MENU_PLAY_MODE_ONLINE("cmd.menu.play.mode.online"),
    CMD_MENU_PLAY_MODE_RETURN_TO_MAIN_MENU("cmd.menu.play.mode.returnToMainMenu"),
    CMD_MENU_MENU_PROFILE("cmd.menu.profile"),
    CMD_MENU_RATING_TABLE("cmd.menu.ratingTable"),
    CMD_MENU_SETTINGS("cmd.menu.settings"),
    CMD_MENU_SETTINGS_CHANGE_LANGUAGE("cmd.menu.settings.change.language"),
    CMD_MENU_ABOUT("cmd.menu.about"),
    //------------------------------------------------------------------------------------------------------------------
    MENU_MAIN("msg.menu"),
    //------------------------------------------------------------------------------------------------------------------
    PLAY_SELECT_MODE("msg.play.select.mode"),
    PLAY_LOADING("msg.play.loading"),
    PLAY_STARTING("msg.play.starting"),
    PLAY_RULES("msg.play.rules"),
    PLAY_RULES_1("msg.play.rules.rule1"),
    PLAY_RULES_2("msg.play.rules.rule2"),
    PLAY_RULES_3("msg.play.rules.rule3"),
    PLAY_RULES_4("msg.play.rules.rule4"),
    PLAY_RULES_5("msg.play.rules.rule5"),
    PLAY_GAME_MISS("msg.play.game.miss"),
    PLAY_GAME_HIT("msg.play.game.hit"),
    PLAY_GAME_MOVE_FIRST_ENEMY("msg.play.game.move.first.enemy"),
    PLAY_GAME_MOVE_FIRST_YOUR("msg.play.game.move.first.your"),
    PLAY_GAME_MOVE_ENEMY("msg.play.game.move.enemy"),
    PLAY_GAME_MOVE_YOUR("msg.play.game.move.your"),
    PLAY_GAME_MOVE_SKIP("msg.play.game.move.skip"),
    PLAY_MAP_BOARD_SHIPS_SELECT("msg.play.map.board.ships.select"),
    PLAY_MAP_BOARD_SHIPS_TIME_IS_UP("msg.play.map.board.ships.timeIsUp"),
    PLAY_MAP_BOARD_SHIPS_GREAT_CHOICE("msg.play.map.board.ships.greatChoice"),
    PLAY_MAP_BOARD_YOUR_LIVE_SHIPS("msg.play.map.board.your.total.liveShips"),
    PLAY_MAP_BOARD_YOUR_TOTAL_SHOTS("msg.play.map.board.your.total.shots"),
    PLAY_MAP_BOARD_YOUR("msg.play.map.board.your"),
    PLAY_MAP_BOARD_ENEMY("msg.play.map.board.enemy"),
    PLAY_BOARD_ENEMY_TOTAL_LIVE_SHIPS("msg.play.map.board.enemy.total.liveShips"),
    PLAY_BOARD_ENEMY_TOTAL_SHOTS("msg.play.map.board.enemy.total.shots"),
    PLAY_GAME_LOSE("msg.play.game.lose"),
    PLAY_GAME_WIN("msg.play.game.win"),
    PLAY_GAME_TECHNICAL_LOSE("msg.play.game.technical.lose"),
    PLAY_GAME_TECHNICAL_WIN("msg.play.game.technical.win"),
    PLAY_GAME_SCORES("msg.play.game.scores"),
    PLAY_GAME_INTERRUPTED("msg.play.game.interrupted"),
    PLAY_GAME_ONLINE_SEARCH("msg.play.game.online.search"),
    PLAY_GAME_ONLINE_SEARCH_PLAYERS_QUEUE("msg.play.game.online.search.playersQueue"),
    PLAY_GAME_ONLINE_WAITING("msg.play.game.online.waiting"),
    PLAY_GAME_ONLINE_PLACEMENT_NO_TIME("msg.play.game.online.placement.noTime"),
    //------------------------------------------------------------------------------------------------------------------
    PROFILE_YOUR_ID_TELEGRAM("msg.profile.your.id.telegram"),
    PROFILE_YOUR_NAME_FIRST("msg.profile.your.name.first"),
    PROFILE_YOUR_NAME_LAST("msg.profile.your.name.last"),
    PROFILE_YOUR_NAME_NOT_AVAILABLE("msg.profile.your.name.na"),
    PROFILE_YOUR_GAMES_NO("msg.profile.your.games.no"),
    PROFILE_YOUR_GAMES_TOTAL_GAMES("msg.profile.your.games.total.games"),
    PROFILE_YOUR_GAMES_TOTAL_SCORES("msg.profile.your.games.total.scores"),
    PROFILE_YOUR_GAMES_TOTAL_WINS("msg.profile.your.games.total.wins"),
    PROFILE_YOUR_GAMES_TOTAL_LOSES("msg.profile.your.games.total.loses"),
    PROFILE_YOUR_GAMES_TOTAL_SHOTS("msg.profile.your.games.total.shots"),
    //------------------------------------------------------------------------------------------------------------------
    RATING_TABLE("msg.ratingTable"),
    RATING_TABLE_TOP("msg.ratingTable.top"),
    RATING_TABLE_SCORES("msg.ratingTable.scores"),
    //------------------------------------------------------------------------------------------------------------------
    SETTINGS("msg.settings"),
    SETTINGS_CHANGE_LANGUAGE_SHOW("msg.settings.change.language.show"),
    SETTINGS_CHANGE_LANGUAGE_CHANGED_SUCCESS("msg.settings.change.language.changed.success"),
    SETTINGS_CHANGE_LANGUAGE_CHANGED_FAILURE("msg.settings.change.language.changed.failure"),
    SETTINGS_CHANGE_LANGUAGE_NOT_AVAILABLE("msg.settings.change.language.na"),
    //------------------------------------------------------------------------------------------------------------------
    ABOUT_WRITE_TO_AUTHOR("msg.about.writeToAuthor"),
    ABOUT_GITHUB_REPO("msg.about.githubRepo"),
    //------------------------------------------------------------------------------------------------------------------
    DEFAULT_INCORRECT_MESSAGE("msg.default.incorrectMessage");
    // -----------------------------------------------------------------------------------------------------------------

    private final String messageKey;

    BotMessages(final String messageKey) {
        this.messageKey = messageKey;
    }

    public String getKey() {
        return messageKey;
    }
}
