package com.example.em_card_service.config;

import com.example.em_card_service.data.properties.EncryptionProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigTest {

    SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        EncryptionProperties properties = new EncryptionProperties();
        properties.setPassword(KeyGenerators.string().generateKey());
        properties.setSalt(KeyGenerators.string().generateKey());
        securityConfig = new SecurityConfig(properties);
    }

    /**
     * @apiNote The encryptor has to be deterministic so that the {@code UNIQUE} constraint
     * on card numbers actually makes sense. Otherise, it would be possible to persist cards
     * with the same card number over and over again: encryption output would be different each time.
     */
    @Test
    void textEncryptor_givenSameInput_producesSameOutput() {
        String input = "1234 5678 9012 3456";
        TextEncryptor textEncryptor = securityConfig.textEncryptor();

        String firstOutput = textEncryptor.encrypt(input);
        String secondOutput = textEncryptor.encrypt(input);

        assertThat(firstOutput).isEqualTo(secondOutput);
    }

    @Test
    void textEncryptor_onEncryptionDecryptionCycle_inputNotChanged() {
        String input = "1234 5678 9012 3456";
        TextEncryptor textEncryptor = securityConfig.textEncryptor();

        String output = textEncryptor.encrypt(input);
        String decryptedOutput = textEncryptor.decrypt(output);

        assertThat(decryptedOutput).isEqualTo(input);
    }
}