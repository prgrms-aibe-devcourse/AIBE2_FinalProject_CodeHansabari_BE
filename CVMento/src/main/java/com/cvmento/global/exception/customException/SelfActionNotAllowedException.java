package com.cvmento.global.exception.customException;

public class SelfActionNotAllowedException extends RuntimeException {
    public SelfActionNotAllowedException(String message) { super(message); }
    public SelfActionNotAllowedException(String message, Throwable cause) { super(message, cause); }
}
