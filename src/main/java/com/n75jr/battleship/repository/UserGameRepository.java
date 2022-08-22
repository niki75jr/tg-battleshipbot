package com.n75jr.battleship.repository;

import com.n75jr.battleship.domain.UserGame;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserGameRepository extends JpaRepository<UserGame, Long>, UserGameCustomRepository {

    Optional<UserGame> findByUserRecordId_TelegramUserId(Long userId);
}
