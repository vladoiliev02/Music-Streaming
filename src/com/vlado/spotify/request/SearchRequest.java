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

public class SearchRequest implements Request {
    private final String[] keyWords;
    private final SelectionKey key;
    private final SongDatabase songDatabase;

    public SearchRequest(String[] keyWords, SelectionKey key, SongDatabase songDatabase) {
        this.keyWords = ParameterValidator.checkNull(keyWords, "keyWords");
        this.key = ParameterValidator.checkNull(key, "key");
        this.songDatabase = ParameterValidator.checkNull(songDatabase, "songDatabase");
    }

    @Override
    public ServerResponse execute() {
        checkLoggedIn(key);

        AtomicInteger i = new AtomicInteger(1);
        List<Song> songs = songDatabase.search(keyWords);
        String results = songs.stream()
                .map(song -> String.format("%d: %s by %s", i.getAndIncrement(), song.name(), song.artist()))
                .collect(Collectors.joining(System.lineSeparator()));

        if (results.isEmpty()) {
            return ServerResponse.of(ResponseStatus.OK, "No matching results found.");
        }
        return ServerResponse.of(ResponseStatus.OK, results);
    }
}
