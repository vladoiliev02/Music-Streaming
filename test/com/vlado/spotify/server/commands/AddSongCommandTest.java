package com.vlado.spotify.server.commands;

import com.vlado.spotify.database.SongDatabase;
import com.vlado.spotify.song.Song;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AddSongCommandTest {
    private static final String SONG_NAME= "songName";
    private static final String ARTIST_NAME= "artistName";
    private static final Path PATH = Path.of("path");
    private final SongDatabase songDatabase = mock(SongDatabase.class);

    @Test
    void testCorrectResponse() {
        Song song = new Song(SONG_NAME, ARTIST_NAME, 0, PATH);
        doNothing().when(songDatabase).addSong(song);

        AddSongCommand command = new AddSongCommand(SONG_NAME, ARTIST_NAME, PATH, songDatabase);
        assertEquals(String.format("Song: %s by %s, successfully added.", SONG_NAME, ARTIST_NAME),
                command.execute(),
                "Add song returns correct response.");

        verify(songDatabase, times(1)).addSong(song);
    }

}