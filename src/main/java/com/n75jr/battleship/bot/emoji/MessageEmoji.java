package com.n75jr.battleship.bot.emoji;

public enum MessageEmoji {

    // -----------------------------------------------------------------------------------------------------------------
    LOSE("\uD83D\uDE14"),         // 😔,
    RULES_SIGN("\u2757"),         // ❗
    RULES_ITEM("\u25AA"),         // ▪
    VICTORY("\uD83E\uDD73"),      // 🥳
    SCORES("\uD83C\uDFAF"),       // 🎯
    TIME_IS_UP("\u231B"),         // ⌛
    FIRST_MOVE("\uD83C\uDFB2"),   // 🎲
    YOUR_MOVE("\uD83D\uDFE2"),    // 🟢
    MISS("\uD83D\uDD34"),         // 🔴
    HIT("\uD83D\uDFE2"),          // 🟢
    // DUMMY: ----------------------------------------------------------------------------------------------------------
    DUMMY("DUMMY");
    // -----------------------------------------------------------------------------------------------------------------

    private final String codeUnits;

    MessageEmoji(final String codeUnits) {
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
