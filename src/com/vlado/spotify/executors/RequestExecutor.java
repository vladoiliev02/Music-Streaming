package com.vlado.spotify.executors;

import com.vlado.spotify.exceptions.UserErrorException;
import com.vlado.spotify.logger.Logger;
import com.vlado.spotify.logger.log.Log;
import com.vlado.spotify.logger.log.LogLevel;
import com.vlado.spotify.parsers.RequestParser;
import com.vlado.spotify.request.Request;
import com.vlado.spotify.server.response.ResponseStatus;
import com.vlado.spotify.server.response.ServerResponse;
import com.vlado.spotify.validations.ParameterValidator;

import java.nio.channels.SelectionKey;
import java.util.Arrays;

public class RequestExecutor {
    private final RequestParser requestParser;
    private Logger logger;

    public RequestExecutor() {
        this.requestParser = new RequestParser();
    }

    public RequestExecutor(Logger logger) {
        this();
        this.logger = logger;
    }

    public ServerResponse execute(String request, SelectionKey key) {
        ParameterValidator.checkNull(request, "request");
        ParameterValidator.checkNull(key, "key");

        try {
            Request request1 = requestParser.parse(request, key);
            ServerResponse response = request1.execute();

            // Delete later
            logMessage(response.toString(), key);

            return response;
        } catch (UserErrorException e) {
            logError(e, key);
            return ServerResponse.of(ResponseStatus.ERROR, e.getMessage());
        } catch (Throwable e) {
            logError(e, key);
            return ServerResponse.of(ResponseStatus.ERROR, "Server error occurred.");
        }
    }

    private void logError(Throwable e, SelectionKey key) {
        ParameterValidator.checkNull(e, "e");
        ParameterValidator.checkNull(key, "key");

        if (logger != null) {
            logger.log(Log.of(LogLevel.ERROR,
                    String.format("User: %s%nError message: %s%nStack trace: %s",
                            key.attachment(), e.getMessage(), Arrays.toString(e.getStackTrace()))));
        }
    }

    private void logMessage(String response, SelectionKey key) {
        ParameterValidator.checkNull(response, "response");
        ParameterValidator.checkNull(key, "key");

        if (logger != null) {
            logger.log(Log.of(LogLevel.MESSAGE,
                    String.format("User: %s%n Response: %s",
                            key.attachment(), response)));
        }
    }
}
