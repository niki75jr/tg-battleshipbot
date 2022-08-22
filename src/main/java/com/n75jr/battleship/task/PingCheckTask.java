package com.n75jr.battleship.task;

import com.n75jr.battleship.props.PingCheckTaskProps;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Log4j2
@Component
@RequiredArgsConstructor
public class PingCheckTask {

    public final static String DEFAULT_URL = "https://google.com";
    public final static Long DEFAULT_DELAY = 30_000L;
    private final PingCheckTaskProps props;
    private final ScheduledExecutorService THREAD_POOL = Executors.newSingleThreadScheduledExecutor();
    @Getter
    private int lastStatusCode;
    @Getter
    private Instant lastInstantResponse;

    @PreDestroy
    void tearDown() {
        THREAD_POOL.shutdownNow();
    }

    @PostConstruct
    void start() {
        var uri = props.getUrl() != null ? props.getUrl() : DEFAULT_URL;
        var delay = props.getDelay() != null ? props.getDelay() : DEFAULT_DELAY;
        var httpClient = HttpClient.newBuilder().build();

        THREAD_POOL.scheduleWithFixedDelay(getPingCheckTaskRunnable(httpClient, uri),
                0L, delay, TimeUnit.MILLISECONDS);
    }

    private Runnable getPingCheckTaskRunnable(final HttpClient httpClient, final String url) {
        return () -> {
                try {
                    if (log.isDebugEnabled()) {
                        log.debug("send a request to \"" + url + "\"");
                    }

                    var response = httpClient.send(
                            HttpRequest.newBuilder().GET().uri(URI.create(url)).build(),
                            HttpResponse.BodyHandlers.ofString()
                    );
                    lastStatusCode = response.statusCode();
                    lastInstantResponse = Instant.now();

                    if (log.isDebugEnabled()) {
                        log.debug("statusCode from \"" + url + "\": " + lastStatusCode);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
        };
    }
}
