package com.n75jr.battleship.bot.handler;

import com.n75jr.battleship.domain.DataUser;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DataHolder {

    CallbackQuery query;
    Long chatId;
    Integer messageId;
    DataUser dataUser;
}
