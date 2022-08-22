package com.n75jr.battleship.service;

import com.n75jr.battleship.domain.DataUser;

import java.util.List;

public interface UserDataService {

    DataUser save(DataUser dataUser);
    DataUser remove(Long telegramUserId);
    DataUser findByTelegramUserId(Long telegramUserId);
    List<DataUser> findAll();
    void updateRepo(Long telegramUserId);
}
