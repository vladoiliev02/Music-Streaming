package com.vlado.spotify.exceptions;

public class SongNotAddedToPlaylistException extends UserErrorException {
    public SongNotAddedToPlaylistException(String message) {
        super(message);
    }

    public SongNotAddedToPlaylistException(String message, Throwable cause) {
        super(message, cause);
    }
}
