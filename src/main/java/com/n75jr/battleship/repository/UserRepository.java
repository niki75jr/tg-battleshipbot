package com.n75jr.battleship.repository;

import com.n75jr.battleship.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Map;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByTelegramUserId(long telegramUserId);
    @Query("select u.telegramUserId, sum(ug.scoresGame) from UserGame ug join ug.userRecordId u group by u.telegramUserId, ug.scoresGame")
    Map<Long, Long> map();
}
