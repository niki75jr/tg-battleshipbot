package com.n75jr.battleship.bot.emoji;

public enum MenuEmoji {

    // Main menu: ------------------------------------------------------------------------------------------------------
    UNEXPECTED_ERROR("\uD83D\uDEAB"),           // 🚫
    PLAY("\uD83D\uDD79"),                       // 🕹
    USER_PROFILE("\uD83D\uDC64"),               // 👤
    RATING_TABLE("\uD83D\uDCC8"),               // 📈
    SETTINGS("\u2699"),                         // ⚙
    SETTINGS_CHANGE_LANGUAGE("\uD83C\uDF0E"),   // 🌎
    // Play: -----------------------------------------------------------------------------------------------------------
    OFFLINE("\uD83D\uDDA5"),                    // 🖥️
    ONLINE("\uD83C\uDF10"),                     // 🌐
    NEW_RANDOM("\uD83D\uDD04"),                 // 🔄
    CONFIRM("\u2705"),                          // ✅
    RETURN_TO_MAIN_MENU("\u2934"),              // ⤴
    // Rating table: ---------------------------------------------------------------------------------------------------
    TROPHY("\uD83C\uDFC6"),                     // 🏆
    FIRST("\uD83E\uDD47"),                      // 🥇
    SECOND("\uD83E\uDD48"),                     // 🥈
    THIRD("\uD83E\uDD49"),                      // 🥉
    ANCHOR("\u2693"),                           // ⚓
    // About: ----------------------------------------------------------------------------------------------------------
    ABOUT("\u2692"),                            // ⚒
    // DUMMY: ----------------------------------------------------------------------------------------------------------
    DUMMY("DUMMY");
    // -----------------------------------------------------------------------------------------------------------------

    private final String codeUnits;

    MenuEmoji(final String codeUnits) {
        this.codeUnits = codeUnits;
    }

    public String getCodeUnits() {
        return codeUnits;
    }

    @Override
    public String toString() {
        return codeUnits;
    }
}
