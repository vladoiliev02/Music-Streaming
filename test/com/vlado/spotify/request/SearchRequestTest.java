package com.vlado.spotify.request;

import com.vlado.spotify.database.SongDatabase;
import com.vlado.spotify.exceptions.UserErrorException;
import com.vlado.spotify.server.response.ResponseStatus;
import com.vlado.spotify.server.response.ServerResponse;
import com.vlado.spotify.song.Song;
import com.vlado.spotify.stubs.SelectionKeyStub;
import com.vlado.spotify.user.User;
import org.junit.jupiter.api.Test;

import java.nio.channels.SelectionKey;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SearchRequestTest {

    private static final String[] keyWords = new String[]{"k1", "k2", "k3"};
    private final SelectionKeyStub keyStub = new SelectionKeyStub();
    private final SongDatabase songDatabase = mock(SongDatabase.class);

    @Test
    void testExecuteNotLoggedIn() {
        keyStub.attach(null);
        SearchRequest request = new SearchRequest(keyWords, keyStub, songDatabase);
        assertThrows(UserErrorException.class, request::execute,
                "Request cannot be executed when user is not logged in.");
    }

    @Test
    void testExecuteEmptyResponse() {
        keyStub.attach(new User("user"));
        SearchRequest request = new SearchRequest(keyWords, keyStub, songDatabase);

        List<Song> resultList = List.of();
        when(songDatabase.search(keyWords)).thenReturn(resultList);

        ServerResponse expected = ServerResponse.of(ResponseStatus.OK, "No matching results found.");
        assertEquals(expected.toString(), request.execute().toString(),
                "The correct response is returned when no results are found.");

        verify(songDatabase, times(1)).search(keyWords);
    }

    @Test
    void testExecuteCorrectResponse() {
        keyStub.attach(new User("user"));
        SearchRequest request = new SearchRequest(keyWords, keyStub, songDatabase);

        Song s1 = new Song("s1", "a1", 5, Path.of("somePath"));
        Song s2 = new Song("s2", "a2", 9, Path.of("somePath"));
        Song s3 = new Song("s3", "a3", 10, Path.of("somePath"));
        List<Song> resultList = List.of(s1, s2, s3);
        when(songDatabase.search(keyWords)).thenReturn(resultList);

        String results = String.format("1: s1 by a1%n2: s2 by a2%n3: s3 by a3");

        ServerResponse expected = ServerResponse.of(ResponseStatus.OK, results);
        assertEquals(expected.toString(), request.execute().toString(),
                "The correct response is returned.");

        verify(songDatabase, times(1)).search(keyWords);
    }
}