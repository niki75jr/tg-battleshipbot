package com.n75jr.battleship.bot.context;

import com.n75jr.battleship.bot.BotState;
import com.n75jr.battleship.bot.handler.CallbackQueryHandler;
import com.n75jr.battleship.bot.handler.MyChatMemberHandler;
import com.n75jr.battleship.bot.handler.MessageHandler;
import com.n75jr.battleship.exception.NotFountHandlerForBotStateException;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@Component
public class BotStateContext {

    private final Map<BotState, MessageHandler> messageHandlers;
    private final Map<BotState, CallbackQueryHandler> callbackHandlers;
    private final Map<BotState, MyChatMemberHandler> myChatMemberHandlers;

    public BotStateContext(List<MessageHandler> messageHandlers,
                           List<CallbackQueryHandler> callbackHandlers,
                           List<MyChatMemberHandler> myChatMemberHandlers) {
        this.messageHandlers = new HashMap<>();
        this.callbackHandlers = new HashMap<>();
        this.myChatMemberHandlers = new HashMap<>();

        messageHandlers.forEach(h -> this.messageHandlers.put(h.getState(), h));
        callbackHandlers.forEach(c -> this.callbackHandlers.put(c.getState(), c));
        myChatMemberHandlers.forEach(c -> this.myChatMemberHandlers.put(c.getState(), c));

        if (log.isDebugEnabled()) {
            log.debug("BotStateContext<init>: messageHandlers: {}\n" +
                            "callbackHandlers: {}\n" +
                            "myChatMemberHandlers: {}",
                    messageHandlers.size(), callbackHandlers.size(), myChatMemberHandlers.size());
        }
    }

    public MessageHandler getMessageHandler(BotState botState) {
        var inputMessageHandler = messageHandlers.get(botState);
        if (inputMessageHandler == null) {
            throw new NotFountHandlerForBotStateException("Not found a inputMessageHandler for " + botState);
        }

        return inputMessageHandler;
    }

    public CallbackQueryHandler getCallbackQueryHandler(BotState botState) {
        var callbackQueryHandler = callbackHandlers.get(botState);
        if (callbackQueryHandler == null) {
            throw new NotFountHandlerForBotStateException("Not found a callbackQueryHandler for " + botState);
        }

        return callbackQueryHandler;
    }

    public MyChatMemberHandler getChatMemberHandler(BotState botState) {
        var chatMemberHandler = myChatMemberHandlers.get(botState);
        if (chatMemberHandler == null) {
            throw new NotFountHandlerForBotStateException("Not found a chatMemberHandler for " + botState);
        }

        return chatMemberHandler;
    }
}
