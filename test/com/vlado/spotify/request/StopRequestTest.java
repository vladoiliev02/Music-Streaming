package com.vlado.spotify.request;

import com.vlado.spotify.database.OnlineUsers;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StopRequestTest {

    private static final String USERNAME = "username";
    private final SelectionKeyStub keyStub = new SelectionKeyStub();
    private final OnlineUsers onlineUsers = mock(OnlineUsers.class);

    @Test
    void testExecuteNotLoggedIn() {
        keyStub.attach(null);
        StopRequest request = new StopRequest(keyStub, onlineUsers);
        assertThrows(UserErrorException.class, request::execute,
                "Request cannot be executed when user is not logged in.");
    }

    @Test
    void testExecuteNotListening() {
        keyStub.attach(new User(USERNAME));
        StopRequest request = new StopRequest(keyStub, onlineUsers);

        when(onlineUsers.isListening(USERNAME)).thenReturn(false);

        ServerResponse actual = assertDoesNotThrow(request::execute);
        ServerResponse expected = ServerResponse.of(ResponseStatus.STOP_STREAMING, "Song stopped.");
        assertEquals(expected.toString(), actual.toString(),
                "The correct response is returned when user is not listening to anything.");

        verify(onlineUsers, times(1)).isListening(USERNAME);
        verify(onlineUsers, never()).stopListening(any(SelectionKey.class));
    }

    @Test
    void testExecuteCorrectResponse() {
        keyStub.attach(new User(USERNAME));
        StopRequest request = new StopRequest(keyStub, onlineUsers);

        when(onlineUsers.isListening(USERNAME)).thenReturn(true);
        doNothing().when(onlineUsers).stopListening(null);

        ServerResponse expected = ServerResponse.of(ResponseStatus.STOP_STREAMING, "Song stopped.");
        assertEquals(expected.toString(), request.execute().toString(),
                "The correct response is returned.");

        verify(onlineUsers, times(1)).isListening(USERNAME);
        verify(onlineUsers, times(1)).stopListening(null);
    }
}