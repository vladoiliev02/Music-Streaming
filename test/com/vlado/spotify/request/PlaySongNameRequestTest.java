package com.vlado.spotify.request;

import com.vlado.spotify.database.OnlineUsers;
import com.vlado.spotify.database.SongDatabase;
import com.vlado.spotify.exceptions.UserErrorException;
import com.vlado.spotify.server.response.ResponseStatus;
import com.vlado.spotify.server.response.ServerResponse;
import com.vlado.spotify.song.Song;
import com.vlado.spotify.song.SongFormat;
import com.vlado.spotify.stubs.SelectionKeyStub;
import com.vlado.spotify.user.User;
import org.junit.jupiter.api.Test;

import javax.sound.sampled.AudioFormat;
import java.nio.channels.SelectionKey;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlaySongNameRequestTest {

    private static final String SONG_NAME = "songName";
    private final SelectionKeyStub keyStub = new SelectionKeyStub();
    private final OnlineUsers onlineUsers = mock(OnlineUsers.class);
    private final SongDatabase songDatabase = mock(SongDatabase.class);


    @Test
    void testExecuteNotLoggedIn() {
        keyStub.attach(null);
        PlaySongNameRequest request = new PlaySongNameRequest(SONG_NAME, keyStub, onlineUsers, songDatabase);
        assertThrows(UserErrorException.class, request::execute,
                "Request cannot be executed when user is not logged in.");

        verify(songDatabase, never()).getSong(anyString(), anyString());
        verify(onlineUsers, never()).prepareStreaming(any(SelectionKey.class), anyString(), anyString());
    }

    @Test
    void testExecuteCorrectResponse() {
        keyStub.attach(new User("user"));
        PlaySongNameRequest request = new PlaySongNameRequest(SONG_NAME, keyStub,
                onlineUsers, songDatabase);

        Song song = new Song(SONG_NAME, "artist", 5, Path.of("somePath"));
        AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 1, 1, 1,
                1, 1, false);
        when(songDatabase.getSong(SONG_NAME)).thenReturn(song);
        when(onlineUsers.prepareStreaming(keyStub, SONG_NAME)).thenReturn(audioFormat);


        ServerResponse expected = ServerResponse.of(ResponseStatus.OK,
                String.format("Now playing: %s by %s.", song.name(), song.artist()), SongFormat.of(audioFormat));
        assertEquals(expected.toString(), request.execute().toString(),
                "The correct response is returned.");

        verify(songDatabase, times(1)).getSong(SONG_NAME);
        verify(onlineUsers, times(1)).prepareStreaming(keyStub, SONG_NAME);
    }
}