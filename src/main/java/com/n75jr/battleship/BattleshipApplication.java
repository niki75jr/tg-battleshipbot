package com.n75jr.battleship;

import com.n75jr.battleship.props.BotProps;
import com.n75jr.battleship.props.PingCheckTaskProps;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties({BotProps.class, PingCheckTaskProps.class})
@SpringBootApplication
public class BattleshipApplication {

	public static void main(String[] args) {
		SpringApplication.run(BattleshipApplication.class, args);
	}

}
