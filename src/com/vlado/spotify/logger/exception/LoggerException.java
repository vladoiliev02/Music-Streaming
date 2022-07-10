package com.vlado.spotify.logger.exception;

public class LoggerException extends RuntimeException {
    public LoggerException(String message) {
        super(message);
    }

    public LoggerException(String message, Throwable cause) {
        super(message, cause);
    }
}
