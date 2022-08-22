package com.n75jr.battleship.service;

import com.n75jr.battleship.bot.BotState;
import com.n75jr.battleship.domain.DataUser;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface PlayOnlineService {

    void createGame(DataUser dataUser1, DataUser dataUser2);
    void finishGame(Long telegramUserId);
    void interruptGame(Long telegramUserId, BotState botState);
    void transferUpdate(Long telegramUserId, Update update);
    int getSearchSize();
}
