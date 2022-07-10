package com.vlado.spotify.exceptions;

public class UserErrorException extends RuntimeException {
    public UserErrorException(String message) {
        super(message);
    }

    public UserErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}
