package com.n75jr.battleship.domain;

import com.n75jr.battleship.bot.BotState;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder
@EqualsAndHashCode(of = "user.telegramUserId")
public class DataUser {

    private final User user;
    private BotState botState;
}
