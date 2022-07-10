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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AddSongNameArtistToPlaylistRequestTest {

    private static final String PLAYLIST_NAME = "playlistName";
    private static final String SONG_NAME = "songName";
    private static final String ARTIST_NAME = "artistName";
    private final SelectionKeyStub keyStub = new SelectionKeyStub();
    private final SongDatabase songDatabase = mock(SongDatabase.class);

    @Test
    void testExecuteNotLoggedIn() {
        keyStub.attach(null);
        AddSongNameArtistToPlaylistRequest request = new AddSongNameArtistToPlaylistRequest(
                PLAYLIST_NAME, SONG_NAME, ARTIST_NAME, keyStub, songDatabase);
        assertThrows(UserErrorException.class, request::execute,
                "Request cannot be executed when user is not logged in.");
    }

    @Test
    void testExecuteCorrectResponse() {
        keyStub.attach(new User("user"));
        AddSongNameArtistToPlaylistRequest request = new AddSongNameArtistToPlaylistRequest(
                PLAYLIST_NAME, SONG_NAME, ARTIST_NAME, keyStub, songDatabase);

        Song song = new Song(SONG_NAME, ARTIST_NAME, 5, Path.of("somePath"));
        when(songDatabase.getSong(SONG_NAME, ARTIST_NAME)).thenReturn(song);
        doNothing().when(songDatabase).addSongToPlaylist(PLAYLIST_NAME, song);

        ServerResponse expected = ServerResponse.of(ResponseStatus.OK, String.format(
                "Song: %s by %s, successfully added to %s.", song.name(), song.artist(), PLAYLIST_NAME));
        assertEquals(expected.toString(), request.execute().toString(),
                "The correct response is returned.");

        verify(songDatabase, times(1)).getSong(SONG_NAME, ARTIST_NAME);
        verify(songDatabase, times(1)).addSongToPlaylist(PLAYLIST_NAME, song);
    }

}