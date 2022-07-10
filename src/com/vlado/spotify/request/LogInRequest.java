package com.vlado.spotify.request;

import com.vlado.spotify.database.OnlineUsers;
import com.vlado.spotify.database.UserDatabase;
import com.vlado.spotify.exceptions.LoginException;
import com.vlado.spotify.server.response.ResponseStatus;
import com.vlado.spotify.server.response.ServerResponse;
import com.vlado.spotify.user.User;
import com.vlado.spotify.validations.ParameterValidator;

import java.nio.channels.SelectionKey;

public class LogInRequest implements Request {
    private final String username;
    private final String password;
    private final SelectionKey key;
    private final UserDatabase userDatabase;
    private final OnlineUsers onlineUsers;

    public LogInRequest(String username, String password, SelectionKey key,
                        UserDatabase userDatabase, OnlineUsers onlineUsers) {
        this.username = ParameterValidator.checkNull(username, "username");
        this.password = ParameterValidator.checkNull(password, "password");
        this.key = ParameterValidator.checkNull(key, "key");
        this.userDatabase = ParameterValidator.checkNull(userDatabase, "userDatabase");
        this.onlineUsers = ParameterValidator.checkNull(onlineUsers, "onlineUsers");
    }

    @Override
    public ServerResponse execute() {
        if (key.attachment() != null) {
            throw new LoginException(String.format(
                    "You are already logged in: %s.%nPlease first log out.", key.attachment()));
        }

        if (!userDatabase.exists(username)) {
            throw new LoginException(String.format(
                    "User: %s, does not exist.\nPlease try registering.", username));
        }

        if (!userDatabase.isCorrectPassword(username, password)) {
            throw new LoginException("Incorrect password, please try again.");
        }

        if (onlineUsers.isOnline(username)) {
            throw new LoginException("You are already logged in on another device. Please first logout.");
        }

        key.attach(new User(username));

        onlineUsers.addUser(key);
        return ServerResponse.of(ResponseStatus.SUCCESSFULLY_LOGGED_IN, username);
    }
}
