package com.vlado.spotify.request;

import com.vlado.spotify.database.SongDatabase;
import com.vlado.spotify.exceptions.UserErrorException;
import com.vlado.spotify.server.response.ResponseStatus;
import com.vlado.spotify.server.response.ServerResponse;
import com.vlado.spotify.stubs.SelectionKeyStub;
import com.vlado.spotify.user.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class CreatePlaylistRequestTest {

    private static final String PLAYLIST_NAME = "playlistName";
    private final SelectionKeyStub keyStub = new SelectionKeyStub();
    private final SongDatabase songDatabase = mock(SongDatabase.class);

    @Test
    void testExecuteNotLoggedIn() {
        keyStub.attach(null);
        CreatePlaylistRequest request = new CreatePlaylistRequest(PLAYLIST_NAME, keyStub, songDatabase);
        assertThrows(UserErrorException.class, request::execute,
                "Request cannot be executed when user is not logged in.");
    }

    @Test
    void testExecuteCorrectResponse() {
        keyStub.attach(new User("user"));
        CreatePlaylistRequest request = new CreatePlaylistRequest(PLAYLIST_NAME, keyStub, songDatabase);

        doNothing().when(songDatabase).createPlaylist(PLAYLIST_NAME);

        ServerResponse expected = ServerResponse.of(ResponseStatus.OK, String.format("Playlist: %s, successfully created.", PLAYLIST_NAME));
        assertEquals(expected.toString(), request.execute().toString(),
                "The correct response is returned.");

        verify(songDatabase, times(1)).createPlaylist(PLAYLIST_NAME);
    }
}