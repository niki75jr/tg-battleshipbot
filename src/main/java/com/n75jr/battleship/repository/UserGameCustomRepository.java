package com.n75jr.battleship.repository;

import org.springframework.data.domain.PageRequest;

import java.util.Map;
import java.util.Optional;

public interface UserGameCustomRepository {

    Optional<Map<String, ?>> profileInfoByTelegramUserId(Long telegramUserId);
    Map<Long, Long> scoresTable(PageRequest pageRequest);
}
