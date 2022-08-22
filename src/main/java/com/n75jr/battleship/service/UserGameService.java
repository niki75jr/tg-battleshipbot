package com.n75jr.battleship.service;

import com.n75jr.battleship.domain.UserGame;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Map;

public interface UserGameService {

    void save(UserGame userGame);
    void saveAll(Iterable<UserGame> userGames);
    UserGame findByUserId(Long userId);
    List<UserGame> findAll(PageRequest pageRequest);
    Map<String, ?> getProfileInfoByTelegramUserId(Long telegramUserId);
    Map<Long, Long> getTableScores(PageRequest pageRequest);
}
