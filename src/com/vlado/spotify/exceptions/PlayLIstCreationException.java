package com.vlado.spotify.exceptions;

public class PlayLIstCreationException extends UserErrorException {
    public PlayLIstCreationException(String message) {
        super(message);
    }

    public PlayLIstCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
