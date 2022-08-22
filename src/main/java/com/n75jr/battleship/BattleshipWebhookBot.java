package com.n75jr.battleship;

import com.n75jr.battleship.bot.TelegramFacade;
import com.n75jr.battleship.props.BotProps;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class BattleshipWebhookBot extends TelegramWebhookBot {

    private final BotProps botProps;
    private final TelegramFacade telegramFacade;

    @Override
    public String getBotUsername() {
        return botProps.getName();
    }

    @Override
    public String getBotToken() {
        return botProps.getToken();
    }

    @Override
    public String getBotPath() {
        return botProps.getPath();
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        return telegramFacade.handleUpdate(update);
    }
}
