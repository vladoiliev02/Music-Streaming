package com.vlado.spotify.request;

import com.vlado.spotify.database.SongDatabase;
import com.vlado.spotify.exceptions.UserErrorException;
import com.vlado.spotify.server.response.ResponseStatus;
import com.vlado.spotify.server.response.ServerResponse;
import com.vlado.spotify.song.Song;
import com.vlado.spotify.validations.ParameterValidator;

import java.nio.channels.SelectionKey;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class TopNSongsRequest implements Request {
    private final int n;
    private final SelectionKey key;
    private final SongDatabase songDatabase;

    public TopNSongsRequest(int n, SelectionKey key, SongDatabase songDatabase) {
        if (n <= 0) {
            throw new UserErrorException("Request: top <n>, n must be positive.");
        }

        this.n = n;
        this.key = ParameterValidator.checkNull(key, "key");
        this.songDatabase = ParameterValidator.checkNull(songDatabase, "songDatabase");
    }

    @Override
    public ServerResponse execute() {
        checkLoggedIn(key);

        AtomicInteger i = new AtomicInteger(1);
        List<Song> songs = songDatabase.getTopNStreamedSongs(n);
        String results = songs.stream()
                .map(song -> String.format("%d: %s by %s -> %d streams.",
                        i.getAndIncrement(), song.name(), song.artist(), song.streams()))
                .collect(Collectors.joining(System.lineSeparator()));

        if (results.isEmpty()) {
            return ServerResponse.of(ResponseStatus.OK, "No matching results found.");
        }
        return ServerResponse.of(ResponseStatus.OK, results);
    }
}
