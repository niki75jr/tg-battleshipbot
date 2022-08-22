package com.n75jr.battleship.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

@Configuration
public class CommonConfiguration {

    @Value("${spring.messages.basename:messages.properties}")
    private String baseName;

    @Bean
    @Primary
    public MessageSource messageSource() {
        var source = new ReloadableResourceBundleMessageSource();
        source.setDefaultEncoding("UTF-8");
        source.setBasename("classpath:" + baseName);
        return source;
    }
}
