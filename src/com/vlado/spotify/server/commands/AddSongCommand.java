package com.vlado.spotify.server.commands;

import com.vlado.spotify.database.SongDatabase;
import com.vlado.spotify.song.Song;
import com.vlado.spotify.validations.ParameterValidator;

import java.nio.file.Path;

public class AddSongCommand implements Command {
    private final String songName;
    private final String artistName;
    private final Path path;
    private final SongDatabase songDatabase;

    public AddSongCommand(String songName, String artistName, Path path, SongDatabase songDatabase) {
        this.songName = ParameterValidator.checkNull(songName, "songName");
        this.artistName = ParameterValidator.checkNull(artistName, "artistName");
        this.path = ParameterValidator.checkNull(path, "path");
        this.songDatabase = ParameterValidator.checkNull(songDatabase, "songDatabase");
    }


    @Override
    public String execute() {
        Song newSong = new Song(songName, artistName, 0, path);
        songDatabase.addSong(newSong);

        return String.format("Song: %s by %s, successfully added.", songName, artistName);
    }
}
