package com.n75jr.battleship.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.PageRequest;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Log4j2
@RequiredArgsConstructor
public class UserGameCustomRepositoryImpl implements UserGameCustomRepository {

    @PersistenceContext
    private final EntityManager em;

    @Override
    public Optional<Map<String, ?>> profileInfoByTelegramUserId(final Long telegramUserId) {
        var query = em.createQuery(
                "SELECT u.telegramUserId                                      AS telegram_id, " +
                        "       u.firstName                                           AS first_name, " +
                        "       u.lastName                                            AS last_name, " +
                        "       COUNT(ug)                                             AS total_games, " +
                        "       SUM(ug.scoresGame)                                    AS total_scores, " +
                        "       SUM(CASE ug.isWin WHEN true then 1 else 0 END)        AS total_wins, " +
                        "       SUM(CASE ug.isWin WHEN false then 1 else 0 END)       AS total_loses, " +
                        "       SUM(ug.totalShots)                                    AS total_shots " +
                        "FROM UserGame ug " +
                        "LEFT JOIN ug.userRecordId u " +
                        "WHERE u.telegramUserId = :telegramUserId " +
                        "GROUP BY u.telegramUserId, u.firstName, u.lastName",
                Tuple.class);
        query.setParameter("telegramUserId", telegramUserId);

        if (log.isDebugEnabled()) {
            log.debug("profileInfoByTelegramUserId: telegramUserId: {}", telegramUserId);
        }

        Tuple tuple;
        try {
            tuple = query.getSingleResult();

            if (log.isDebugEnabled()) {
                log.debug("\texist a record");
            }
        } catch (NoResultException e) {
            if (log.isDebugEnabled()) {
                log.debug("\tno a record");
            }

            return Optional.empty();
        }

        return Optional.of(getProfileInfo(tuple));
    }

    @Override
    public Map<Long, Long> scoresTable(final PageRequest pageRequest) {
        var query = em.createQuery(
                "SELECT u.telegramUserId AS user_id," +
                        "    SUM(ug.scoresGame) AS total_scores " +
                        "FROM UserGame ug " +
                        "JOIN ug.userRecordId u " +
                        "GROUP BY u.telegramUserId " +
                        "ORDER BY total_scores DESC",
                Tuple.class);

        query.setFirstResult(pageRequest.getPageNumber() * pageRequest.getPageSize());
        query.setMaxResults(pageRequest.getPageSize());

        if (log.isDebugEnabled()) {
            log.debug("scoresTable: pageRequest: {}", pageRequest);
        }

        return query.getResultStream()
                .collect(Collectors.toMap(
                        tuple -> (Long) tuple.get("user_id"),
                        tuple -> (Long) tuple.get("total_scores")
                ));
    }

    private Map<String, ?> getProfileInfo(final Tuple tuple) {
        var columns = new String[]{
                "telegram_id", "first_name",
                "last_name", "total_games",
                "total_scores", "total_wins",
                "total_loses", "total_shots"
        };

        var map = new HashMap<String, Object>();
        Arrays.stream(columns)
                .forEach(c -> map.put(c, tuple.get(c)));

        return map;
    }
}
