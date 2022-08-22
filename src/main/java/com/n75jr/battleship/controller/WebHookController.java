package com.n75jr.battleship.controller;

import com.n75jr.battleship.bot.TelegramFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

@Log4j2
@RestController
@RequiredArgsConstructor
public class WebHookController {

    private final TelegramFacade telegramFacade;

    @PostMapping("/")
    public BotApiMethod<?> onUpdateReceived(@RequestBody Update update) {
        if (log.isDebugEnabled()) {
            log.debug("onUpdateReceived: update: {}",
                    update);
        }

        return telegramFacade.handleUpdate(update);
    }
}
