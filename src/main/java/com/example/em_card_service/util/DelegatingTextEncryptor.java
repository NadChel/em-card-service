package com.example.em_card_service.util;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.crypto.encrypt.TextEncryptor;

import java.util.function.UnaryOperator;

public class DelegatingTextEncryptor implements TextEncryptor {

    private final UnaryOperator<String> encryptionFunction;
    private final UnaryOperator<String> decryptionFunction;

    private DelegatingTextEncryptor(UnaryOperator<String> encryptionFunction,
                                    UnaryOperator<String> decryptionFunction) {
        this.encryptionFunction = encryptionFunction;
        this.decryptionFunction = decryptionFunction;
    }

    public static DelegatingTextEncryptor of(UnaryOperator<String> encryptionFunction,
                                             UnaryOperator<String> decryptionFunction) {
        return new DelegatingTextEncryptor(encryptionFunction, decryptionFunction);
    }

    @Override
    @NonNull
    public String encrypt(@Nullable String text) {
        return encryptionFunction.apply(text);
    }

    @Override
    @NonNull
    public String decrypt(@Nullable String encryptedText) {
        return decryptionFunction.apply(encryptedText);
    }
}
