package com.example.em_card_service.util;

import org.jasypt.iv.IvGenerator;

public class FixedIVGenerator implements IvGenerator {

    private final byte[] iv;

    public FixedIVGenerator(byte[] iv) {
        this.iv = iv.clone();
    }

    @Override
    public byte[] generateIv(int lengthBytes) {
        byte[] result = new byte[lengthBytes];
        System.arraycopy(iv, 0, result, 0, Math.min(iv.length, lengthBytes));
        return result;
    }

    @Override
    public boolean includePlainIvInEncryptionResults() {
        return false;
    }
}