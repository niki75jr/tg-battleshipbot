package com.n75jr.battleship.props;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.ValidationException;

@Getter @Setter
@ConfigurationProperties(prefix = "task.ping-check")
public class PingCheckTaskProps {

    private Long delay;
    private String url;

    public void setUrl(final String url) {
        if (!validateUrl(url)) {
            throw new ValidationException("The URL is not valid");
        }

        this.url = url;
    }

    private boolean validateUrl(final String url) {
        return url.startsWith("http") || url.startsWith("https");
    }
}
