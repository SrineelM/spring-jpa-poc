package com.example.demo.exception;

/**
 * Runtime exception for business rule violations
 */
public class BusinessRuleViolationException extends RuntimeException {
    
    private final String ruleType;
    private final Object violatedValue;

    public BusinessRuleViolationException(String message, String ruleType, Object violatedValue) {
        super(message);
        this.ruleType = ruleType;
        this.violatedValue = violatedValue;
    }

    public BusinessRuleViolationException(String message) {
        super(message);
        this.ruleType = "UNKNOWN";
        this.violatedValue = null;
    }

    public String getRuleType() {
        return ruleType;
    }

    public Object getViolatedValue() {
        return violatedValue;
    }
}
