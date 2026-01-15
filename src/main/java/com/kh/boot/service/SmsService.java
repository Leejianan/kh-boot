package com.kh.boot.service;

/**
 * Service for sending and verifying SMS codes.
 */
public interface SmsService {

    /**
     * Send verification code to the specified phone number.
     *
     * @param phone Phone number
     * @return true if sent successfully
     */
    boolean sendCode(String phone);

    /**
     * Verify the code for the specified phone number.
     *
     * @param phone Phone number
     * @param code  Verification code
     * @return true if valid
     */
    boolean verifyCode(String phone, String code);
}
