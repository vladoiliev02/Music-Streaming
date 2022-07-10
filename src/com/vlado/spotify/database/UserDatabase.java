package com.vlado.spotify.database;

import com.vlado.spotify.exceptions.RegisterException;
import com.vlado.spotify.exceptions.UserAlreadyExistsException;
import com.vlado.spotify.validations.ParameterValidator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

public class UserDatabase {
    private static final String USERNAME_PARAM_NAME = "username";
    private static final String PASSWORD_PARAM_NAME = "password";
    private static final String USER_ALREADY_EXISTS = "The user: %s, already exists.";

    private static final int USERNAME_INDEX = 0;
    private static final int PASSWORD_INDEX = 1;

    private static final UserDatabase INSTANCE = new UserDatabase();

    private final Map<String, String> registeredUsers;
    private Writer writer;

    private UserDatabase() {
        registeredUsers = new HashMap<>();
    }

    public static UserDatabase instance() {
        return INSTANCE;
    }

    public void setWriter(Writer writer) {
        this.writer = ParameterValidator.checkNull(writer, "writer");
    }

    public void addUser(String username, String password) {
        ParameterValidator.checkNull(username, USERNAME_PARAM_NAME);
        ParameterValidator.checkEmpty(username, USERNAME_PARAM_NAME);
        ParameterValidator.checkBlank(username, USERNAME_PARAM_NAME);
        ParameterValidator.checkNull(password, PASSWORD_PARAM_NAME);
        ParameterValidator.checkEmpty(password, PASSWORD_PARAM_NAME);
        ParameterValidator.checkBlank(password, PASSWORD_PARAM_NAME);

        if (exists(username)) {
            throw new UserAlreadyExistsException(String.format(USER_ALREADY_EXISTS, username));
        }

        try {
            ParameterValidator.checkNull(writer, "writer. Please set the writer before adding users.");
            writer.write(String.format("%s %s%n", username, password));
            writer.flush();
        } catch (IOException e) {
            throw new RegisterException("Register error occurred. Please try again.", e);
        }

        registeredUsers.put(username, password);
    }

    public boolean exists(String username) {
        return registeredUsers.containsKey(username);
    }

    public boolean isCorrectPassword(String username, String password) {
        return registeredUsers.containsKey(username) &&
                registeredUsers.get(username).equals(password);
    }

    public void readUsers(Reader reader) {
        ParameterValidator.checkNull(reader, "reader");

        BufferedReader bufferedReader = new BufferedReader(reader);

        bufferedReader.lines()
                .map(line -> line.split("\\s+"))
                .forEach(line -> {
                    if (line.length != 2) {
                        throw new IllegalArgumentException("Invalid format. Every line must contain:" +
                                System.lineSeparator() +
                                "<username> <password>");
                    }

                    String username = line[USERNAME_INDEX];

                    if (exists(username)) {
                        throw new UserAlreadyExistsException(String.format(USER_ALREADY_EXISTS, username));
                    }

                    registeredUsers.put(username, line[PASSWORD_INDEX]);
                });
    }

    public void clear() {
        registeredUsers.clear();
    }
}