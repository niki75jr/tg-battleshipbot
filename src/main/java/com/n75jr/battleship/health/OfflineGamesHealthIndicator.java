package com.n75jr.battleship.health;

import com.n75jr.battleship.bot.context.BotOfflineGameContext;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OfflineGamesHealthIndicator implements HealthIndicator {

    private final BotOfflineGameContext botOfflineGameContext;

    @Override
    public Health health() {
        return Health.up()
                .withDetail("isTerminated", botOfflineGameContext.isGamesPoolTerminated())
                .withDetail("isShutdown", botOfflineGameContext.isGamesPoolShutdown())
                .withDetail("sizeGamesMap", botOfflineGameContext.getGamesMapSize())
                .withDetail("sizeGamesFutureMap", botOfflineGameContext.getFutureGamesMapSize())
                .build();
    }
}
