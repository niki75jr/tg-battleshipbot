package com.n75jr.battleship.bot.emoji;

public enum MenuEmoji {

    // Main menu: ------------------------------------------------------------------------------------------------------
    UNEXPECTED_ERROR("\uD83D\uDEAB"),           // ๐ซ
    PLAY("\uD83D\uDD79"),                       // ๐น
    USER_PROFILE("\uD83D\uDC64"),               // ๐ค
    RATING_TABLE("\uD83D\uDCC8"),               // ๐
    SETTINGS("\u2699"),                         // โ
    SETTINGS_CHANGE_LANGUAGE("\uD83C\uDF0E"),   // ๐
    // Play: -----------------------------------------------------------------------------------------------------------
    OFFLINE("\uD83D\uDDA5"),                    // ๐ฅ๏ธ
    ONLINE("\uD83C\uDF10"),                     // ๐
    NEW_RANDOM("\uD83D\uDD04"),                 // ๐
    CONFIRM("\u2705"),                          // โ
    RETURN_TO_MAIN_MENU("\u2934"),              // โคด
    // Rating table: ---------------------------------------------------------------------------------------------------
    TROPHY("\uD83C\uDFC6"),                     // ๐
    FIRST("\uD83E\uDD47"),                      // ๐ฅ
    SECOND("\uD83E\uDD48"),                     // ๐ฅ
    THIRD("\uD83E\uDD49"),                      // ๐ฅ
    ANCHOR("\u2693"),                           // โ
    // About: ----------------------------------------------------------------------------------------------------------
    ABOUT("\u2692"),                            // โ
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
