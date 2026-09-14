package com.recruitsystem.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.jwt")
@Getter
@Setter
public class JwtProperties {

    /** Base64-encoded HMAC-SHA secret used to sign access tokens. */
    private String secret;

    /** Access token lifetime in milliseconds. */
    private long expirationMs;
}
