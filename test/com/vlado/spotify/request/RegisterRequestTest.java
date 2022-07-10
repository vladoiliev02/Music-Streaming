package com.vlado.spotify.request;

import com.vlado.spotify.database.UserDatabase;
import com.vlado.spotify.server.response.ResponseStatus;
import com.vlado.spotify.server.response.ServerResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class RegisterRequestTest {

    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private final UserDatabase userDatabase = mock(UserDatabase.class);

    @Test
    void testExecuteCorrectResponse() {
        doNothing().when(userDatabase).addUser(USERNAME, PASSWORD);

        RegisterRequest request = new RegisterRequest(USERNAME, PASSWORD, userDatabase);

        ServerResponse expected = ServerResponse.of(ResponseStatus.OK, String.format("User: %s, successfully registered!", USERNAME));
        assertEquals(expected.toString(), request.execute().toString(),
                "The correct response is returned.");

        verify(userDatabase, times(1)).addUser(USERNAME, PASSWORD);
    }
}