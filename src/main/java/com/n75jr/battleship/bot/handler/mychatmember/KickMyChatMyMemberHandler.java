package com.n75jr.battleship.bot.handler.mychatmember;

import com.n75jr.battleship.bot.BotState;
import com.n75jr.battleship.bot.handler.MyChatMemberHandler;
import com.n75jr.battleship.service.PlayOfflineService;
import com.n75jr.battleship.service.PlayOnlineServiceImpl;
import com.n75jr.battleship.service.UserDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.ChatMemberUpdated;

@Component
@RequiredArgsConstructor
public class KickMyChatMyMemberHandler implements MyChatMemberHandler {

    private final UserDataService userDataService;
    private final PlayOfflineService playOfflineService;
    private final PlayOnlineServiceImpl playOnlineService;

    @Override
    public BotState getState() {
        return BotState.LEAVE;
    }

    @Override
    public void handleMyChatMemberUpdate(final ChatMemberUpdated memberUpdated) {
        var telegramUserId = memberUpdated.getFrom().getId();
        var dataUser = userDataService.findByTelegramUserId(telegramUserId);
        var oldBotState = dataUser.getBotState();

        dataUser.setBotState(BotState.LEAVE);

        switch (oldBotState) {
            case PLAY_OFFLINE_GAME_SHIP_PLACEMENT:
            case PLAY_OFFLINE_GAME_YOUR_MOVE:
            case PLAY_OFFLINE_GAME_ENEMY_MOVE:
            case PLAY_OFFLINE_GAME:
                playOfflineService.interruptGame(telegramUserId);
                break;
            case PLAY_ONLINE_GAME_SEARCH:
            case PLAY_ONLINE_GAME_SHIPS_PLACEMENT:
            case PLAY_ONLINE_GAME_SHIPS_PLACEMENT_CONFIRM:
            case PLAY_ONLINE_GAME_YOUR_MOVE:
            case PLAY_ONLINE_GAME_ENEMY_MOVE:
                System.out.println("#DEBUG: handleMyChatMemberUpdate: " + telegramUserId);
                playOnlineService.interruptGame(telegramUserId, oldBotState);
                break;
        }

        userDataService.remove(telegramUserId);
    }
}
