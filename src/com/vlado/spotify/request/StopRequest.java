package com.vlado.spotify.request;

import com.vlado.spotify.database.OnlineUsers;
import com.vlado.spotify.exceptions.UserErrorException;
import com.vlado.spotify.server.response.ResponseStatus;
import com.vlado.spotify.server.response.ServerResponse;
import com.vlado.spotify.user.User;
import com.vlado.spotify.validations.ParameterValidator;

import java.nio.channels.SelectionKey;

public class StopRequest implements Request {

    private final SelectionKey key;
    private final OnlineUsers onlineUsers;

    public StopRequest(SelectionKey key, OnlineUsers onlineUsers) {
        this.key = ParameterValidator.checkNull(key, "key");
        this.onlineUsers = ParameterValidator.checkNull(onlineUsers, "onlineUsers");
    }

    @Override
    public ServerResponse execute() {
        checkLoggedIn(key);

        User user = (User) key.attachment();
        if (onlineUsers.isListening(user.getUsername())) {
            onlineUsers.stopListening(user.getMusicKey());
        }

        return ServerResponse.of(ResponseStatus.STOP_STREAMING, "Song stopped.");
    }
}
