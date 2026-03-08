package com.navi.exception;

public class ConsentVerifyException extends RuntimeException {
    private final String code;
    public ConsentVerifyException(String code, String message) {
        super(message);
        this.code = code;
    }
    public String getCode() { return code; }
}
