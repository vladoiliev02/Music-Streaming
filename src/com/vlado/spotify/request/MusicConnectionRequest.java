package com.vlado.spotify.request;

import com.vlado.spotify.database.OnlineUsers;
import com.vlado.spotify.server.response.ResponseStatus;
import com.vlado.spotify.server.response.ServerResponse;
import com.vlado.spotify.user.User;
import com.vlado.spotify.validations.ParameterValidator;

import java.nio.channels.SelectionKey;

public class MusicConnectionRequest implements Request {
    private final String username;
    private final SelectionKey musicKey;
    private final OnlineUsers onlineUsers;

    public MusicConnectionRequest(String username, SelectionKey musicKey, OnlineUsers onlineUsers) {
        this.username = ParameterValidator.checkNull(username, "username");
        this.musicKey = ParameterValidator.checkNull(musicKey, "musicKey");
        this.onlineUsers = ParameterValidator.checkNull(onlineUsers, "onlineUsers");
    }

    @Override
    public ServerResponse execute() {
        if (!onlineUsers.isOnline(username)) {
            throw new IllegalStateException(String.format("User: %s, is not online.", username));
        }

        User user = (User) onlineUsers.get(username).attachment();

        if (user == null) {
            throw new IllegalStateException("Null user bound to username");
        }

        user.setMusicKey(musicKey);

        return ServerResponse.of(ResponseStatus.READY_TO_STREAM,
                String.format("Music channel for %s connected", username));
    }
}
