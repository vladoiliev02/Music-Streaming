package com.vlado.spotify.request;

import com.vlado.spotify.database.OnlineUsers;
import com.vlado.spotify.server.response.ResponseStatus;
import com.vlado.spotify.server.response.ServerResponse;
import com.vlado.spotify.user.User;
import com.vlado.spotify.validations.ParameterValidator;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.channels.SelectionKey;

public class LogOutRequest implements Request {
    private final SelectionKey key;
    private final OnlineUsers onlineUsers;

    public LogOutRequest(SelectionKey key, OnlineUsers onlineUsers) {
        this.key = ParameterValidator.checkNull(key, "key");
        this.onlineUsers = ParameterValidator.checkNull(onlineUsers, "onlineUsers");
    }

    @Override
    public ServerResponse execute() {
        checkLoggedIn(key);

        onlineUsers.removeUser(key);

        User user = (User) key.attachment();

        if (user.getMusicKey() != null) {
            try {
                user.getMusicKey().channel().close();
            } catch (IOException e) {
                throw new UncheckedIOException(String.format(
                        "%s music channel closing problem.", user.getUsername()), e);
            }
        }

        key.attach(null);
        return ServerResponse.of(ResponseStatus.LOGGED_OUT,
                String.format("Successfully logged out of: %s.", user.getUsername()));
    }
}
