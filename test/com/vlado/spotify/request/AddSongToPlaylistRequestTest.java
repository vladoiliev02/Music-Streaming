package com.vlado.spotify.request;

import com.vlado.spotify.database.SongDatabase;
import com.vlado.spotify.exceptions.UserErrorException;
import com.vlado.spotify.server.response.ResponseStatus;
import com.vlado.spotify.server.response.ServerResponse;
import com.vlado.spotify.song.Song;
import com.vlado.spotify.stubs.SelectionKeyStub;
import com.vlado.spotify.user.User;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AddSongToPlaylistRequestTest {

    private static final String PLAYLIST_NAME = "playlistName";
    private static final String SONG_NAME = "songName";
    private final SelectionKeyStub keyStub = new SelectionKeyStub();
    private final SongDatabase songDatabase = mock(SongDatabase.class);

    @Test
    void testExecuteNotLoggedIn() {
        keyStub.attach(null);
        AddSongToPlaylistRequest request = new AddSongToPlaylistRequest(PLAYLIST_NAME, SONG_NAME, keyStub, songDatabase);
        assertThrows(UserErrorException.class, request::execute,
                "Request cannot be executed when user is not logged in.");
    }

    @Test
    void testExecuteCorrectResponse() {
        keyStub.attach(new User("user"));
        AddSongToPlaylistRequest request = new AddSongToPlaylistRequest(PLAYLIST_NAME, SONG_NAME, keyStub, songDatabase);

        Song song = new Song(SONG_NAME, "artist", 5, Path.of("somePath"));
        when(songDatabase.getSong(SONG_NAME)).thenReturn(song);
        doNothing().when(songDatabase).addSongToPlaylist(PLAYLIST_NAME, song);

        ServerResponse expected = ServerResponse.of(ResponseStatus.OK, String.format(
                        "Song: %s by %s, successfully added to %s.", song.name(), song.artist(), PLAYLIST_NAME));
        assertEquals(expected.toString(), request.execute().toString(),
                "The correct response is returned.");

        verify(songDatabase, times(1)).getSong(SONG_NAME);
        verify(songDatabase, times(1)).addSongToPlaylist(PLAYLIST_NAME, song);
    }
}