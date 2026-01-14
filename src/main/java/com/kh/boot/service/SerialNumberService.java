package com.kh.boot.service;

import com.kh.boot.entity.KhSerialNumber;
import java.util.List;

/**
 * Service for generating business serial numbers.
 * Supports date-based reset and custom rules.
 */
public interface SerialNumberService {

    /**
     * Generate next serial number
     */
    String generateNext(String businessKey, String defaultPrefix, String defaultDateFormat, int defaultWidth);

    /**
     * Manual reset for a business key
     */
    void reset(String businessKey, long nextValue);

    /**
     * List all rules
     */
    List<KhSerialNumber> list();

    /**
     * Save or update a rule
     */
    void saveRule(KhSerialNumber serialNumber);
}
