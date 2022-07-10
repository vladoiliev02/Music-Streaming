package com.vlado.spotify.request;

import com.vlado.spotify.database.SongDatabase;
import com.vlado.spotify.exceptions.UserErrorException;
import com.vlado.spotify.server.response.ResponseStatus;
import com.vlado.spotify.server.response.ServerResponse;
import com.vlado.spotify.song.Song;
import com.vlado.spotify.stubs.SelectionKeyStub;
import com.vlado.spotify.user.User;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TopNSongsRequestTest {


    private final int n = 2;
    private final SelectionKeyStub keyStub = new SelectionKeyStub();
    private final SongDatabase songDatabase = mock(SongDatabase.class);

    @Test
    void testExecuteNotLoggedIn() {
        keyStub.attach(null);
        TopNSongsRequest request = new TopNSongsRequest(n, keyStub, songDatabase);
        assertThrows(UserErrorException.class, request::execute,
                "Request cannot be executed when user is not logged in.");
    }

    @Test
    void testExecuteEmptyResponse() {
        keyStub.attach(new User("user"));
        TopNSongsRequest request = new TopNSongsRequest(n, keyStub, songDatabase);

        List<Song> resultList = List.of();
        when(songDatabase.getTopNStreamedSongs(n)).thenReturn(resultList);

        ServerResponse expected = ServerResponse.of(ResponseStatus.OK, "No matching results found.");
        assertEquals(expected.toString(), request.execute().toString(),
                "The correct response is returned when no songs are returned.");

        verify(songDatabase, times(1)).getTopNStreamedSongs(n);
    }

    @Test
    void testExecuteCorrectResponse() {
        keyStub.attach(new User("user"));
        TopNSongsRequest request = new TopNSongsRequest(n, keyStub, songDatabase);

        Song s1 = new Song("s1", "a1", 10, Path.of("somePath"));
        Song s2 = new Song("s2", "a2", 9, Path.of("somePath"));
        Song s3 = new Song("s3", "a3", 5, Path.of("somePath"));
        List<Song> resultList = List.of(s1, s2, s3);
        when(songDatabase.getTopNStreamedSongs(n)).thenReturn(resultList);

        String results = String.format(
                "1: s1 by a1 -> 10 streams.%n2: s2 by a2 -> 9 streams.%n3: s3 by a3 -> 5 streams.");

        ServerResponse expected = ServerResponse.of(ResponseStatus.OK, results);
        assertEquals(expected.toString(), request.execute().toString(),
                "The correct response is returned.");

        verify(songDatabase, times(1)).getTopNStreamedSongs(n);
    }
}