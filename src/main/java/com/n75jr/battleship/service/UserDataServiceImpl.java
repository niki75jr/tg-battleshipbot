package com.n75jr.battleship.service;

import com.n75jr.battleship.bot.cache.Cache;
import com.n75jr.battleship.domain.DataUser;
import com.n75jr.battleship.exception.UserNotFoundException;
import com.n75jr.battleship.repository.UserRepository;
import com.n75jr.battleship.util.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class UserDataServiceImpl implements UserDataService {

    private final UserRepository userRepository;
    private final Cache<Long, DataUser> userDataCache;

    @Override
    @Transactional
    public DataUser save(final DataUser dataUser) {
        if (!userDataCache.contains(dataUser.getUser().getTelegramUserId())) {
            userDataCache.add(dataUser);
            userRepository.save(dataUser.getUser());
        }

        if (log.isDebugEnabled()) {
            log.debug("save: dataUser: {}", dataUser);
        }

        return dataUser;
    }

    @Override
    public DataUser remove(final Long telegramUserId) {
        return userDataCache.remove(telegramUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public DataUser findByTelegramUserId(final Long telegramUserId) {
        var dataUser = userDataCache.get(telegramUserId)
                .orElseGet(() -> UserUtils.toDataUser(userRepository.findByTelegramUserId(telegramUserId)
                        .orElseThrow(() -> new UserNotFoundException(telegramUserId))));

        if (log.isDebugEnabled()) {
            log.debug("findByTelegramUserId: telegramUserId: {}", telegramUserId);
        }

        if (!userDataCache.contains(telegramUserId)) {
            if (log.isDebugEnabled()) {
                log.debug("\tadding to cache");
            }

            userDataCache.add(dataUser);
        }

        return dataUser;
    }

    @Override
    public List<DataUser> findAll() {
        var users = userRepository.findAll().stream()
                .map(UserUtils::toDataUser)
                .collect(Collectors.toList());

        if (log.isDebugEnabled()) {
            log.debug("findAll: (size: {})", users.size());
        }

        return users;
    }

    @Override
    @Transactional
    public void updateRepo(final Long telegramUserId) {
        if (userDataCache.contains(telegramUserId)) {
            userRepository.save(userDataCache.get(telegramUserId)
                    .map(DataUser::getUser)
                    .orElseThrow(() -> new UserNotFoundException(telegramUserId)));
        }

        if (log.isDebugEnabled()) {
            log.debug("updateRepo: {} updated", telegramUserId);
        }
    }
}
