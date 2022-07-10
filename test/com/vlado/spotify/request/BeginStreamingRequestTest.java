package com.vlado.spotify.request;

import com.vlado.spotify.database.OnlineUsers;
import com.vlado.spotify.server.response.ResponseStatus;
import com.vlado.spotify.server.response.ServerResponse;
import com.vlado.spotify.user.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class BeginStreamingRequestTest {

    private static final String USERNAME = "username";
    private final OnlineUsers onlineUsers = mock(OnlineUsers.class);

    @Test
    void testExecuteCorrectResponse() {
        doNothing().when(onlineUsers).startStreaming(USERNAME);
        BeginStreamingRequest request = new BeginStreamingRequest(USERNAME, onlineUsers);

        ServerResponse expected = ServerResponse.of(ResponseStatus.OK, "Streaming started successfully");
        assertEquals(expected.toString(), request.execute().toString(),
                "The correct response is returned.");

        verify(onlineUsers, times(1)).startStreaming(USERNAME);
    }
}