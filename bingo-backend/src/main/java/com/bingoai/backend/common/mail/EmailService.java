package com.bingoai.backend.common.mail;

public interface EmailService {
    void sendPasswordResetEmail(String toEmail, String code);
}
