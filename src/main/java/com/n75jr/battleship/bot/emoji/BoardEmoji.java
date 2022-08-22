package com.n75jr.battleship.bot.emoji;

public enum BoardEmoji {

    INIT("\uD83D\uDFE6"),           // 🟦
    MISS("\u2B1C"),                 // ⬜
    UNAVAILABLE("\uD83D\uDFE5"),    // 🟥
    HEALTHFUL("\uD83D\uDFE9"),      // 🟩
    WOUNDED("\uD83D\uDFE7"),        // 🟧
    DESTROYED("\u274C");            // ❌

    private final String codeUnits;

    BoardEmoji(final String codeUnits) {
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
