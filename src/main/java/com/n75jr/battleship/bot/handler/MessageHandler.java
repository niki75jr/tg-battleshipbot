package com.n75jr.battleship.bot.handler;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface MessageHandler extends Handler {

    BotApiMethod<?> handleMessage(Message message);
}
