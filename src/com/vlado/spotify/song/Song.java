package com.vlado.spotify.song;

import com.vlado.spotify.validations.ParameterValidator;

import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class Song implements Serializable {
    private static final String SONG_FORMAT = ".wav";

    private final String name;
    private final String artist;
    private int streams;
    private final Path path;

    public Song(String name, String artist, int streams, Path path) {
        this.name = ParameterValidator.checkNull(name, "name");
        this.artist = ParameterValidator.checkNull(artist, "artist");
        this.streams = ParameterValidator.checkNonNegative(streams, "streams");
        this.path = ParameterValidator.checkNull(path, "path");
    }

    public String name() {
        return name;
    }

    public String artist() {
        return artist;
    }

    public int streams() {
        return streams;
    }

    public Path path() {
        return path;
    }

    public void incrementStreams() {
        streams += 1;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Song) obj;
        return Objects.equals(this.name.toLowerCase(), that.name.toLowerCase()) &&
                Objects.equals(this.artist.toLowerCase(), that.artist.toLowerCase());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name.toLowerCase(), artist.toLowerCase());
    }

    @Override
    public String toString() {
        return "Song[" +
                "name=" + name + ", " +
                "artist=" + artist + ", " +
                "streams=" + streams + ", " +
                "path=" + path + ']';
    }

}
