package com.vlado.spotify.request;

import com.vlado.spotify.database.OnlineUsers;
import com.vlado.spotify.server.response.ResponseStatus;
import com.vlado.spotify.server.response.ServerResponse;
import com.vlado.spotify.validations.ParameterValidator;

public class BeginStreamingRequest implements Request {
    private final String username;
    private final OnlineUsers onlineUsers;

    public BeginStreamingRequest(String username, OnlineUsers onlineUsers) {
        this.username = ParameterValidator.checkNull(username, "username");
        this.onlineUsers = ParameterValidator.checkNull(onlineUsers, "onlineUsers");
    }

    @Override
    public ServerResponse execute() {
        onlineUsers.startStreaming(username);

        return ServerResponse.of(ResponseStatus.OK, "Streaming started successfully");
    }
}
