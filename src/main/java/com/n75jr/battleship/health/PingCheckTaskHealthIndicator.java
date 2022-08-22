package com.n75jr.battleship.health;

import com.n75jr.battleship.task.PingCheckTask;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class PingCheckTaskHealthIndicator implements HealthIndicator {

    private final PingCheckTask pingCheckTask;

    @Override
    public Health health() {
        var lastStatusCode = pingCheckTask.getLastStatusCode();
        var lastInstantResponse = pingCheckTask.getLastInstantResponse();

        if (lastInstantResponse == null) {
            return Health.unknown()
                    .withDetail("time", "N\\A")
                    .build();
        }

        return Health.up()
                .withDetail("lastStatusCode", lastStatusCode)
                .withDetail("lastInstantResponse",
                        Duration.between(lastInstantResponse, Instant.now()))
                .build();
    }
}
