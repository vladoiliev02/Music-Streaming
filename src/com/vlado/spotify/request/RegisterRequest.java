package com.vlado.spotify.request;

import com.vlado.spotify.database.UserDatabase;
import com.vlado.spotify.server.response.ResponseStatus;
import com.vlado.spotify.server.response.ServerResponse;
import com.vlado.spotify.validations.ParameterValidator;

public class RegisterRequest implements Request {
    private final String username;
    private final String password;
    private final UserDatabase userDatabase;

    public RegisterRequest(String username, String password, UserDatabase userDatabase) {
        this.username = ParameterValidator.checkNull(username, "username");
        this.password = ParameterValidator.checkNull(password, "password");
        this.userDatabase = ParameterValidator.checkNull(userDatabase, "userDatabase");
    }

    @Override
    public ServerResponse execute() {
        userDatabase.addUser(username, password);

        return ServerResponse.of(ResponseStatus.OK, String.format("User: %s, successfully registered!", username));
    }
}
