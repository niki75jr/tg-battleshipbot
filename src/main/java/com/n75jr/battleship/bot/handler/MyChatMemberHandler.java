package com.n75jr.battleship.bot.handler;

import org.telegram.telegrambots.meta.api.objects.ChatMemberUpdated;

public interface MyChatMemberHandler extends Handler {

    void handleMyChatMemberUpdate(ChatMemberUpdated memberUpdated);
}
