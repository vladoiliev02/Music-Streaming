package com.vlado.spotify.request;

import com.vlado.spotify.database.SongDatabase;
import com.vlado.spotify.server.response.ResponseStatus;
import com.vlado.spotify.server.response.ServerResponse;
import com.vlado.spotify.validations.ParameterValidator;

import java.nio.channels.SelectionKey;

public class CreatePlaylistRequest implements Request {
    private final String playlistName;
    private final SelectionKey key;
    private final SongDatabase songDatabase;

    public CreatePlaylistRequest(String playlistName, SelectionKey key, SongDatabase songDatabase) {
        this.playlistName = ParameterValidator.checkNull(playlistName, "playlistName");
        this.key = ParameterValidator.checkNull(key, "key");
        this.songDatabase = ParameterValidator.checkNull(songDatabase, "songDatabase");
    }

    @Override
    public ServerResponse execute() {
        checkLoggedIn(key);

        songDatabase.createPlaylist(playlistName);

        return ServerResponse.of(ResponseStatus.OK, String.format("Playlist: %s, successfully created.", playlistName));
    }
}
