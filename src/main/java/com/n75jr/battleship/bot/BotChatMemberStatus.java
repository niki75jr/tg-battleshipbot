package com.n75jr.battleship.bot;

public enum BotChatMemberStatus {

    STARTED("member"),
    LEAVED("kicked");

    private final String status;

    BotChatMemberStatus(final String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
