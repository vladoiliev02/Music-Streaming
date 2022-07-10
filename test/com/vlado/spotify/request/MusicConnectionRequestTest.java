package com.vlado.spotify.request;

import com.vlado.spotify.database.OnlineUsers;
import com.vlado.spotify.exceptions.UserErrorException;
import com.vlado.spotify.server.response.ResponseStatus;
import com.vlado.spotify.server.response.ServerResponse;
import com.vlado.spotify.stubs.SelectionKeyStub;
import com.vlado.spotify.user.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MusicConnectionRequestTest {

    private static final String USERNAME = "username";
    private final SelectionKeyStub musicKeyStub = new SelectionKeyStub();
    private final OnlineUsers onlineUsers = mock(OnlineUsers.class);

    @Test
    void testExecuteNotOnline() {
        musicKeyStub.attach(null);
        MusicConnectionRequest request = new MusicConnectionRequest(USERNAME, musicKeyStub, onlineUsers);
        when(onlineUsers.isOnline(USERNAME)).thenReturn(false);
        assertThrows(IllegalStateException.class, request::execute,
                "Request cannot be executed when user is not logged in.");

        verify(onlineUsers, times(1)).isOnline(USERNAME);
        verify(onlineUsers, never()).get(USERNAME);
    }

    @Test
    void testExecuteNullUser() {
        musicKeyStub.attach(null);
        when(onlineUsers.isOnline(USERNAME)).thenReturn(true);

        MusicConnectionRequest request = new MusicConnectionRequest(USERNAME, musicKeyStub, onlineUsers);
        when(onlineUsers.get(USERNAME)).thenReturn(new SelectionKeyStub());
        assertThrows(IllegalStateException.class, request::execute,
                "Cannot connect if user is null.");

        verify(onlineUsers, times(1)).isOnline(USERNAME);
        verify(onlineUsers, times(1)).get(USERNAME);
    }

    @Test
    void testExecuteCorrectResponse() {
        SelectionKeyStub userKey = new SelectionKeyStub();
        User user = new User(USERNAME);
        userKey.attach(user);
        when(onlineUsers.isOnline(USERNAME)).thenReturn(true);
        when(onlineUsers.get(USERNAME)).thenReturn(userKey);
        MusicConnectionRequest request = new MusicConnectionRequest(USERNAME, musicKeyStub, onlineUsers);

        ServerResponse expected = ServerResponse.of(ResponseStatus.READY_TO_STREAM,
                String.format("Music channel for %s connected", USERNAME));
        assertEquals(expected.toString(), request.execute().toString(),
                "The correct response is returned.");
        assertEquals(musicKeyStub, user.getMusicKey(),
                "The correct music key is attached.");

        verify(onlineUsers, times(1)).isOnline(USERNAME);
        verify(onlineUsers, times(1)).get(USERNAME);
    }

}