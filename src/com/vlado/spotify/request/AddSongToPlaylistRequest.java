package com.vlado.spotify.request;

import com.vlado.spotify.database.SongDatabase;
import com.vlado.spotify.server.response.ResponseStatus;
import com.vlado.spotify.server.response.ServerResponse;
import com.vlado.spotify.song.Song;
import com.vlado.spotify.validations.ParameterValidator;

import java.nio.channels.SelectionKey;

public class AddSongToPlaylistRequest implements Request {
    private final String playlistName;
    private final String songName;
    private final SelectionKey key;
    private final SongDatabase songDatabase;

    public AddSongToPlaylistRequest(String playlistName, String songName, SelectionKey key, SongDatabase songDatabase) {
        this.playlistName = ParameterValidator.checkNull(playlistName, "playlistName");
        this.songName = ParameterValidator.checkNull(songName, "songName");
        this.key = ParameterValidator.checkNull(key, "key");
        this.songDatabase = ParameterValidator.checkNull(songDatabase, "songDatabase");
    }

    @Override
    public ServerResponse execute() {
        checkLoggedIn(key);

        Song song = songDatabase.getSong(songName);
        songDatabase.addSongToPlaylist(playlistName, song);

        return ServerResponse.of(ResponseStatus.OK, String.format(
                "Song: %s by %s, successfully added to %s.",
                song.name(), song.artist(), playlistName));
    }
}
