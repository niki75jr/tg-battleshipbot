package com.n75jr.battleship.bot.emoji;

public enum LanguageEmoji {

    EN("\uD83C\uDDFA\uD83C\uDDF8"), // πΊπΈ
    RU("\uD83C\uDDF7\uD83C\uDDFA"); // π·πΊ

    private final String codeUnits;

    LanguageEmoji(final String codeUnits) {
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
