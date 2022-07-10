package com.vlado.spotify.request;

import com.vlado.spotify.database.OnlineUsers;
import com.vlado.spotify.database.UserDatabase;
import com.vlado.spotify.exceptions.LoginException;
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

class LogInRequestTest {
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private final SelectionKeyStub keyStub = new SelectionKeyStub();
    private final UserDatabase userDatabase = mock(UserDatabase.class);
    private final OnlineUsers onlineUsers = mock(OnlineUsers.class);

    @Test
    void testExecuteAlreadyOnline() {
        keyStub.attach(new User("user2"));
        LogInRequest request = new LogInRequest(USERNAME, PASSWORD, keyStub, userDatabase, onlineUsers);
        assertThrows(LoginException.class, request::execute,
                "Cannot login if user is already online.");
    }

    @Test
    void testExecuteUserDoesNotExist() {
        keyStub.attach(null);
        when(userDatabase.exists(USERNAME)).thenReturn(false);

        LogInRequest request = new LogInRequest(USERNAME, PASSWORD, keyStub, userDatabase, onlineUsers);
        assertThrows(LoginException.class, request::execute,
                "Cannot login if user does not exist.");

        verify(userDatabase, times(1)).exists(USERNAME);
        verify(userDatabase, never()).isCorrectPassword(USERNAME, PASSWORD);
        verify(onlineUsers, never()).isOnline(USERNAME);
    }

    @Test
    void testExecuteIncorrectPassword() {
        keyStub.attach(null);
        when(userDatabase.exists(USERNAME)).thenReturn(true);
        when(userDatabase.isCorrectPassword(USERNAME, PASSWORD)).thenReturn(false);

        LogInRequest request = new LogInRequest(USERNAME, PASSWORD, keyStub, userDatabase, onlineUsers);
        assertThrows(LoginException.class, request::execute,
                "Cannot login with incorrect password.");

        verify(userDatabase, times(1)).exists(USERNAME);
        verify(userDatabase, times(1)).isCorrectPassword(USERNAME, PASSWORD);
        verify(onlineUsers, never()).isOnline(USERNAME);
    }

    @Test
    void testExecuteLoggedInSomewhereElse() {
        keyStub.attach(null);
        when(userDatabase.exists(USERNAME)).thenReturn(true);
        when(userDatabase.isCorrectPassword(USERNAME, PASSWORD)).thenReturn(true);
        when(onlineUsers.isOnline(USERNAME)).thenReturn(true);

        LogInRequest request = new LogInRequest(USERNAME, PASSWORD, keyStub, userDatabase, onlineUsers);
        assertThrows(LoginException.class, request::execute,
                "Cannot login if user is logged somewhere else.");

        verify(userDatabase, times(1)).exists(USERNAME);
        verify(userDatabase, times(1)).isCorrectPassword(USERNAME, PASSWORD);
        verify(onlineUsers, times(1)).isOnline(USERNAME);
    }

    @Test
    void testExecuteCorrectResponse() {
        keyStub.attach(null);
        when(userDatabase.exists(USERNAME)).thenReturn(true);
        when(userDatabase.isCorrectPassword(USERNAME, PASSWORD)).thenReturn(true);
        when(onlineUsers.isOnline(USERNAME)).thenReturn(false);
        doNothing().when(onlineUsers).addUser(keyStub);
        LogInRequest request = new LogInRequest(USERNAME, PASSWORD, keyStub, userDatabase, onlineUsers);

        ServerResponse expected = ServerResponse.of(ResponseStatus.SUCCESSFULLY_LOGGED_IN, USERNAME);
        assertEquals(expected.toString(), request.execute().toString(),
                "The correct response is returned.");
        assertEquals(USERNAME, ((User) keyStub.attachment()).getUsername(),
                "The username attach to the key is correct.");

        verify(userDatabase, times(1)).exists(USERNAME);
        verify(userDatabase, times(1)).isCorrectPassword(USERNAME, PASSWORD);
        verify(onlineUsers, times(1)).isOnline(USERNAME);
    }
}