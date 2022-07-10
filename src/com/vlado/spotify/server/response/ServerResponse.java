package com.vlado.spotify.server.response;

import com.google.gson.Gson;
import com.vlado.spotify.song.SongFragment;
import com.vlado.spotify.song.SongFormat;
import com.vlado.spotify.validations.ParameterValidator;

public class ServerResponse {
    private final ResponseStatus status;
    private final String message;
    private final SongFormat songFormat;
    private final SongFragment songFragment;

    private ServerResponse(ResponseStatus status, String message, SongFormat songFormat, SongFragment songFragment) {
        this.status = status;
        this.message = message;
        this.songFormat = songFormat;
        this.songFragment = songFragment;
    }

    public static ServerResponse of(ResponseStatus status, String message, SongFormat songFormat) {
        ParameterValidator.checkNull(status, "status");
        ParameterValidator.checkNull(message, "message");
        ParameterValidator.checkNull(songFormat, "songFormat");

        return new ServerResponse(status, message, songFormat, null);
    }

    public static ServerResponse of(ResponseStatus status, String message) {
        ParameterValidator.checkNull(status, "status");
        ParameterValidator.checkNull(message, "message");

        return new ServerResponse(status, message, null, null);
    }

    public static ServerResponse of(ResponseStatus status, SongFragment songFragment) {
        ParameterValidator.checkNull(status, "status");
        ParameterValidator.checkNull(songFragment, "songFragment");

        return new ServerResponse(status, null, null, songFragment);
    }

    public ResponseStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public SongFormat getSongFormat() {
        return songFormat;
    }

    public SongFragment getSongFragment() {
        return songFragment;
    }

    public boolean isSuccessfulLogInResponse() {
        return status.equals(ResponseStatus.SUCCESSFULLY_LOGGED_IN);
    }

    public boolean isReadyToStreamResponse() {
        return status.equals(ResponseStatus.READY_TO_STREAM);
    }

    public boolean isSongFormatResponse() {
        return songFormat != null;
    }

    public boolean isSongFragmentResponse() {
        return songFragment != null;
    }

    public boolean isStreamingStoppedResponse() {
        return status.equals(ResponseStatus.STOP_STREAMING);
    }

    public boolean isLoggedOutResponse() {
        return status.equals(ResponseStatus.LOGGED_OUT);
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
