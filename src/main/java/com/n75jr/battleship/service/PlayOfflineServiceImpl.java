package com.n75jr.battleship.service;

import com.n75jr.battleship.bot.BotMessages;
import com.n75jr.battleship.bot.context.BotOfflineGameContext;
import com.n75jr.battleship.domain.DataUser;
import com.n75jr.battleship.service.game.Game;
import com.n75jr.battleship.service.game.OfflineGame;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

@Log4j2
@Data
@Service
@RequiredArgsConstructor
public class PlayOfflineServiceImpl implements PlayOfflineService {

    private final MessageSource messageSource;
    private final BotMessageService botMessageService;
    private final UserGameService userGameService;
    private final BotOfflineGameContext botOfflineGameContext;

    public void createGame(final DataUser dataUser) {
        var cacheUser = dataUser.getUser();
        var telegramUserId = cacheUser.getTelegramUserId();
        var chatId = cacheUser.getChatId();
        var locale = cacheUser.getLocale();

        botMessageService.sendMessage(chatId, locale, true, BotMessages.PLAY_LOADING);
        botMessageService.sendRulesMessage(chatId, locale,
                Game.DEFAULT_PLACEMENT_DELAY / 1000, Game.DEFAULT_MOVE_DELAY / 1000);

        if (log.isDebugEnabled()) {
            log.debug("createGame: creating: telegramUserId: {}, OFFLINE_GAMES_FUTURE_MAP.size: {}, OFFLINE_GAMES_MAP.size: {}",
                    telegramUserId,
                    botOfflineGameContext.getFutureGamesMapSize(),
                    botOfflineGameContext.getGamesMapSize());
        }

        var offlineGame = new OfflineGame(this,
                messageSource,
                botMessageService,
                userGameService,
                dataUser,
                Game.DEFAULT_PLACEMENT_DELAY,
                Game.DEFAULT_MOVE_DELAY);

        if (log.isDebugEnabled()) {
            log.debug("createGame: created: telegramUserId: {}, OFFLINE_GAMES_FUTURE_MAP.size: {}, OFFLINE_GAMES_MAP.size: {}",
                    telegramUserId,
                    botOfflineGameContext.getFutureGamesMapSize(),
                    botOfflineGameContext.getGamesMapSize());
        }

        botOfflineGameContext.addGame(telegramUserId, offlineGame);
    }

    public void interruptGame(final Long telegramUserId) {
        botOfflineGameContext.interruptGame(telegramUserId);
    }

    public void finishGame(final Long telegramUserId) {
        botOfflineGameContext.finishGame(telegramUserId);
    }

    public void transferUpdate(final Long telegramUserId, final Update update) {
        botOfflineGameContext.transferUpdate(telegramUserId, update);
    }
}
