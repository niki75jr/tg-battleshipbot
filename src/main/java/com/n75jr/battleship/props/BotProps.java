package com.n75jr.battleship.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("tgbot")
public class BotProps {

    private String name;
    private String token;
    private String path;
}
