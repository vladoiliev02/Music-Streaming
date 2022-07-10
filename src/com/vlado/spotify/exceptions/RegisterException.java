package com.vlado.spotify.exceptions;

public class RegisterException extends UserErrorException {
    public RegisterException(String message) {
        super(message);
    }

    public RegisterException(String message, Throwable cause) {
        super(message, cause);
    }
}
