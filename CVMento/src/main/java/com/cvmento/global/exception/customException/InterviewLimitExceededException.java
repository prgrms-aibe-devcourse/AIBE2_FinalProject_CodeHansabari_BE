package com.cvmento.global.exception.customException;

public class InterviewLimitExceededException extends InterviewException {
    public InterviewLimitExceededException(String message) {
        super(message);
    }
    public InterviewLimitExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}
