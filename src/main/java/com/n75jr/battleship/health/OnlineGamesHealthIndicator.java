package com.n75jr.battleship.health;

import com.n75jr.battleship.bot.context.BotOnlineGameContext;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OnlineGamesHealthIndicator implements HealthIndicator {

    private final BotOnlineGameContext botOnlineGameContext;

    @Override
    public Health health() {
        return Health.up()
                .withDetail("isGameSearcherPoolTerminated", botOnlineGameContext.isGameSearcherPoolTerminated())
                .withDetail("isGameSearcherPoolShutdown", botOnlineGameContext.isGameSearcherPoolShutdown())
                .withDetail("isShipsPlacementPoolTerminated", botOnlineGameContext.isShipsPlacementPoolTerminated())
                .withDetail("isShipsPlacementPoolShutdown", botOnlineGameContext.isShipsPlacementPoolShutdown())
                .withDetail("isGamesPoolTerminated", botOnlineGameContext.isGamesPoolTerminated())
                .withDetail("isGamesPoolShutdown", botOnlineGameContext.isGamesPoolShutdown())
                .withDetail("sizeUsersQueue", botOnlineGameContext.getCountSearchPlayers())
                .withDetail("sizeGamesMap", botOnlineGameContext.getGamesMapSize())
                .withDetail("sizeGamesFutureMap", botOnlineGameContext.getGamesFutureMapSize())
                .withDetail("sizeShipsPlacementMap", botOnlineGameContext.getShipsPlacementMapSize())
                .build();
    }
}
