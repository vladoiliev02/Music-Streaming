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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class LogOutRequestTest {
    private final SelectionKeyStub keyStub = new SelectionKeyStub();
    private final OnlineUsers onlineUsers = mock(OnlineUsers.class);

    @Test
    void testExecuteNotLoggedIn() {
        keyStub.attach(null);
        LogOutRequest request = new LogOutRequest(keyStub, onlineUsers);
        assertThrows(UserErrorException.class, request::execute,
                "Request cannot be executed when user is not logged in.");
    }

    @Test
    void testExecuteCorrectResponse() {
        User user = new User("user");
        keyStub.attach(user);
        doNothing().when(onlineUsers).removeUser(keyStub);
        LogOutRequest request = new LogOutRequest(keyStub, onlineUsers);

        ServerResponse expected = ServerResponse.of(ResponseStatus.LOGGED_OUT,
                String.format("Successfully logged out of: %s.", user.getUsername()));
        assertEquals(expected.toString(), request.execute().toString(),
                "The correct response is returned.");
        assertNull(keyStub.attachment(),
                "Key attachment is changed to null.");

        verify(onlineUsers, times(1)).removeUser(keyStub);
    }
}