package com.kh.boot.service;

public interface EmailService {
    /**
     * Send verification code to the specified email.
     *
     * @param email The target email
     * @return The verification code sent
     */
    String sendCode(String email);

    /**
     * Verify the code.
     *
     * @param email The email
     * @param code  The code input by user
     * @return true if valid
     */
    boolean verifyCode(String email, String code);

    /**
     * Send generic email
     *
     * @param to      recipient
     * @param subject subject
     * @param content content
     */
    void sendEmail(String to, String subject, String content);
}
