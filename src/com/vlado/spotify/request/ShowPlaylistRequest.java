package com.vlado.spotify.request;

import com.vlado.spotify.database.SongDatabase;
import com.vlado.spotify.server.response.ResponseStatus;
import com.vlado.spotify.server.response.ServerResponse;
import com.vlado.spotify.song.Song;
import com.vlado.spotify.validations.ParameterValidator;

import java.nio.channels.SelectionKey;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ShowPlaylistRequest implements Request {
    private final String playlistName;
    private final SelectionKey key;
    private final SongDatabase songDatabase;

    public ShowPlaylistRequest(String playlistName, SelectionKey key, SongDatabase songDatabase) {
        this.playlistName = ParameterValidator.checkNull(playlistName, "playlistName");
        this.key = ParameterValidator.checkNull(key, "key");
        this.songDatabase = ParameterValidator.checkNull(songDatabase, "songDatabase");
    }

    @Override
    public ServerResponse execute() {
        checkLoggedIn(key);

        List<Song> playlist = songDatabase.getPlaylist(playlistName);
        AtomicInteger i = new AtomicInteger(1);
        String results = playlist.stream()
                .map(song -> String.format("%d: %s by %s.",
                        i.getAndIncrement(), song.name(), song.artist()))
                .collect(Collectors.joining(System.lineSeparator()));

        if (results.isEmpty()) {
            return ServerResponse.of(ResponseStatus.OK, String.format("Playlist: %s, is empty.", playlistName));
        }
        return ServerResponse.of(ResponseStatus.OK, results);
    }
}
