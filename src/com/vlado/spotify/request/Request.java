package com.vlado.spotify.request;

import com.vlado.spotify.exceptions.UserErrorException;
import com.vlado.spotify.server.response.ServerResponse;
import com.vlado.spotify.validations.ParameterValidator;

import java.nio.channels.SelectionKey;

public interface Request {
    default void checkLoggedIn(SelectionKey key) {
        ParameterValidator.checkNull(key, "key");
        if (key.attachment() == null) {
            throw new UserErrorException("You are not logged in. Please log in to use this feature.");
        }
    }

    ServerResponse execute();
}
