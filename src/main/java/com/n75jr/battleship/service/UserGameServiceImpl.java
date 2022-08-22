package com.n75jr.battleship.service;

import com.n75jr.battleship.exception.UserNotFoundException;
import com.n75jr.battleship.repository.UserGameRepository;
import com.n75jr.battleship.domain.UserGame;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Log4j2
@Service
@RequiredArgsConstructor
public class UserGameServiceImpl implements UserGameService {

    private final UserGameRepository userGameRepository;

    @Override
    @Transactional
    public void save(final UserGame userGame) {
        userGameRepository.save(userGame);

        if (log.isDebugEnabled()) {
            log.debug("save: userGame: {} saved", userGame);
        }
    }

    @Override
    @Transactional
    public void saveAll(final Iterable<UserGame> userGames) {
        userGameRepository.saveAll(userGames);

        if (log.isDebugEnabled()) {
            log.debug("saveAll");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserGame findByUserId(final Long userId) {
        var userGame = userGameRepository.findByUserRecordId_TelegramUserId(userId)
                .orElse(null);

        if (log.isDebugEnabled()) {
            log.debug("findByUserId: userId: {}, (answer: {})", userId, userGame);
        }

        return userGame;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserGame> findAll(final PageRequest pageRequest) {
        var page = userGameRepository.findAll(pageRequest);
        var userGames = page.toList();

        if (log.isDebugEnabled()) {
            log.debug("findAll: pageRequest: {}, size: {}", pageRequest, userGames.size());
        }

        return userGames;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, ?> getProfileInfoByTelegramUserId(final Long telegramUserId) {
        Map<String, ?> stat = userGameRepository.profileInfoByTelegramUserId(telegramUserId)
                .orElseThrow(() -> new UserNotFoundException(telegramUserId));

        if (log.isDebugEnabled()) {
            log.debug("getProfileInfoByTelegramUserId: telegramUserId: {}", telegramUserId);
        }

        return stat;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Long> getTableScores(final PageRequest pageRequest) {
        if (log.isDebugEnabled()) {
            log.debug("getTableScores: pageRequest: {}", pageRequest);
        }

        return userGameRepository.scoresTable(pageRequest);
    }
}
