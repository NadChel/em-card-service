package com.example.em_card_service.data.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.encryption")
public class EncryptionProperties {

    private String password;
    private String salt;
}
