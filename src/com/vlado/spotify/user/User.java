package com.vlado.spotify.user;

import com.vlado.spotify.validations.ParameterValidator;

import java.nio.channels.SelectionKey;
import java.util.Objects;

public class User {
    private final String username;
    private SelectionKey musicKey;

    public User(String username) {
        ParameterValidator.checkNull(username, "username");
        ParameterValidator.checkEmpty(username, "username");
        ParameterValidator.checkBlank(username, "username");

        this.username = username;
    }

    public User(String username, SelectionKey musicKey) {
        this(username);
        this.musicKey = musicKey;
    }

    public String getUsername() {
        return username;
    }

    public SelectionKey getMusicKey() {
        return musicKey;
    }

    public void setMusicKey(SelectionKey musicKey) {
        this.musicKey = musicKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

    @Override
    public String toString() {
        return username;
    }
}
