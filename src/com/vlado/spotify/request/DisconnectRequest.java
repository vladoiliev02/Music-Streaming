package com.vlado.spotify.request;

import com.vlado.spotify.database.OnlineUsers;
import com.vlado.spotify.server.response.ResponseStatus;
import com.vlado.spotify.server.response.ServerResponse;
import com.vlado.spotify.user.User;
import com.vlado.spotify.validations.ParameterValidator;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class DisconnectRequest implements Request {
    private final SelectionKey key;
    private final OnlineUsers onlineUsers;

    public DisconnectRequest(SelectionKey key, OnlineUsers onlineUsers) {
        this.key = ParameterValidator.checkNull(key, "key");
        this.onlineUsers = ParameterValidator.checkNull(onlineUsers, "onlineUsers");
    }

    @Override
    public ServerResponse execute() {
        User user = (User) key.attachment();
        if (user != null) {
            new LogOutRequest(key, onlineUsers).execute();
        }

        SocketChannel client = (SocketChannel) key.channel();
        try {
            client.close();
        } catch (IOException e) {
            throw new UncheckedIOException("User disconnect error occurred", e);
        }

        return ServerResponse.of(ResponseStatus.LOGGED_OUT, "Disconnected from server.");
    }
}
