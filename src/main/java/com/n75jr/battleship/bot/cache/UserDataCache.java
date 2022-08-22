package com.n75jr.battleship.bot.cache;

import com.n75jr.battleship.domain.DataUser;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
@Component
public class UserDataCache implements Cache<Long, DataUser> {

    private final Map<Long, DataUser> STORAGE = new ConcurrentHashMap<>();

    @Override
    public void add(final DataUser dataUser) {
        var oldSize = STORAGE.size();
        var user = dataUser.getUser();
        STORAGE.put(user.getTelegramUserId(), dataUser);

        if (log.isDebugEnabled()) {
            log.debug("add: (size: {} -> {}) dataUser: {}", oldSize, STORAGE.size(), user);
        }
    }

    @Override
    public DataUser remove(final Long key) {
        var dataUser = STORAGE.remove(key);

        if (log.isDebugEnabled()) {
            log.debug("remove: key: {}, (answer: {})", key, dataUser);
        }

        return dataUser;
    }

    @Override
    public boolean contains(final Long telegramUserId) {
        var containsKey = STORAGE.containsKey(telegramUserId);

        if (log.isDebugEnabled()) {
            log.debug("contains: (answer: {}) telegramUserId: {}", containsKey, telegramUserId);
        }

        return containsKey;
    }

    @Override
    public Optional<DataUser> get(final Long telegramUserDataId) {
        var optionalUserData = Optional.ofNullable(STORAGE.get(telegramUserDataId));

        if (log.isDebugEnabled()) {
            log.debug("get: telegramUserDataId: {} (answer: {})", telegramUserDataId, optionalUserData);
        }

        return optionalUserData;
    }

    @Override
    public Collection<DataUser> getAll() {
        var users = STORAGE.values();

        if (log.isDebugEnabled()) {
            log.debug("getAll: (size: {})", users.size());
        }

        return users;
    }

    @Override
    public int getCount() {
        var size = STORAGE.size();

        if (log.isDebugEnabled()) {
            log.debug("getCount: (answer: {})", size);
        }

        return size;
    }
}
