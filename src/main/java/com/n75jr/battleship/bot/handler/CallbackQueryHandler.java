package com.n75jr.battleship.bot.handler;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

public interface CallbackQueryHandler extends Handler {

    BotApiMethod<?> handleQuery(CallbackQuery query);
}
