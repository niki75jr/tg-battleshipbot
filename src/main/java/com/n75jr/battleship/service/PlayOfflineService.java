package com.n75jr.battleship.service;

import com.n75jr.battleship.domain.DataUser;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface PlayOfflineService {

    void createGame(DataUser dataUser);
    void finishGame(Long telegramUserId);
    void interruptGame(Long telegramUserId);
    void transferUpdate(Long telegramUserId, Update update);
}
