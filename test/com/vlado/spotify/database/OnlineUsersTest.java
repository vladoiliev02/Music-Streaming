package com.vlado.spotify.database;

import com.vlado.spotify.song.Song;
import com.vlado.spotify.stubs.SelectionKeyStub;
import com.vlado.spotify.user.User;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sound.sampled.AudioFormat;
import java.nio.channels.SelectionKey;
import java.nio.file.Path;

import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(MockitoExtension.class)
class OnlineUsersTest {
    private static final String SONG_NAME = "SongName";
    private static final String ARTIST_NAME = "ArtistName";
    private static final Song song = new Song(SONG_NAME, ARTIST_NAME, 5, Path.of("testResources", "5sec.wav"));

    private static final OnlineUsers onlineUsers = OnlineUsers.instance();

    private static final SongDatabase songDatabase = mock(SongDatabase.class);
    private static final User user = new User("user1", null);
    private static final SelectionKeyStub keyStub = new SelectionKeyStub();

    @BeforeAll
    static void beforeAll() {
        onlineUsers.setSongDatabase(songDatabase);
    }

    @AfterAll
    static void afterAll() {
        onlineUsers.clear();
    }

    @Test
    void testAddUserNullKey() {
        assertThrows(IllegalArgumentException.class, () -> onlineUsers.addUser(null),
                "User key cannot be null.");
    }

    @Test
    @Order(1)
    void testAddUserSuccessful() {
        keyStub.attach(user);
        assertDoesNotThrow(() -> onlineUsers.addUser(keyStub),
                "addUser does not throw for new user.");
        assertTrue(onlineUsers.isOnline(user.getUsername()),
                "New user is online.");
        assertFalse(onlineUsers.isListening(user.getUsername()),
                "New user is not currently listening.");
        assertEquals(keyStub, onlineUsers.get(user.getUsername()),
                "Correct user is added.");
    }

    @Test
    @Order(2)
    void testAddUserAddExisting() {
        keyStub.attach(user);
        assertThrows(IllegalArgumentException.class, () -> onlineUsers.addUser(keyStub),
                "Cannot add already existing user.");
    }

    @Test
    void testRemoveUserNull() {
        assertThrows(IllegalArgumentException.class, () -> onlineUsers.removeUser(null),
                "Cannot remove null user.");
    }

    @Test
    void testRemoveUserNotOnline() {
        keyStub.attach(new User("NotOnline"));
        assertThrows(IllegalArgumentException.class, () -> onlineUsers.removeUser(keyStub),
                "Cannot remove  user, who is not online.");
    }

    @Test
    @Order(3)
    void testRemoveUserSuccess() {
        keyStub.attach(user);
        assertDoesNotThrow(() -> onlineUsers.removeUser(keyStub),
                "Removing existing user does not throw.");
        assertFalse(onlineUsers.isOnline(user.getUsername()),
                "The removed user is not online.");
        assertFalse(onlineUsers.isListening(user.getUsername()),
                "The removed user is not listening.");
        assertNull(onlineUsers.get(user.getUsername()),
                "Removed user can no longer be found.");
    }

    @Test
    void prepareStreamingNullKey() {
        assertThrows(IllegalArgumentException.class,
                () -> onlineUsers.prepareStreaming(null, SONG_NAME),
                "Cannot prepare for streaming if key is null.");
    }

    @Test
    void prepareStreamingNullKeyAttachment() {
        keyStub.attach(null);
        assertThrows(IllegalArgumentException.class,
                () -> onlineUsers.prepareStreaming(keyStub, SONG_NAME),
                "Cannot prepare for streaming if key attachment is null.");
    }

    @Test
    void prepareStreamingNullSongName() {
        keyStub.attach(user);
        assertThrows(IllegalArgumentException.class,
                () -> onlineUsers.prepareStreaming(keyStub, null),
                "Cannot prepare for streaming if song is null.");
    }

    @Test
    void prepareStreamingNotOnlineUser() {
        keyStub.attach(new User("NotOnline"));
        when(songDatabase.getSong(SONG_NAME)).thenReturn(song);
        assertThrows(IllegalArgumentException.class,
                () -> onlineUsers.prepareStreaming(keyStub, SONG_NAME),
                "Cannot prepare for streaming if song the user is not online.");
    }

    @Test
    void prepareStreamingArtistNullKey() {
        assertThrows(IllegalArgumentException.class,
                () -> onlineUsers.prepareStreaming(null, SONG_NAME, ARTIST_NAME),
                "Cannot prepare for streaming if key is null.");
    }

    @Test
    void prepareStreamingArtistKeyAttachment() {
        keyStub.attach(null);
        assertThrows(IllegalArgumentException.class,
                () -> onlineUsers.prepareStreaming(keyStub, SONG_NAME, ARTIST_NAME),
                "Cannot prepare for streaming if key attachment is null.");
    }

    @Test
    void prepareStreamingArtistNullSongName() {
        keyStub.attach(user);
        assertThrows(IllegalArgumentException.class,
                () -> onlineUsers.prepareStreaming(keyStub, null, ARTIST_NAME),
                "Cannot prepare for streaming if songName is null.");
    }

    @Test
    void prepareStreamingArtistNullArtistName() {
        keyStub.attach(user);
        assertThrows(IllegalArgumentException.class,
                () -> onlineUsers.prepareStreaming(keyStub, SONG_NAME, null),
                "Cannot prepare for streaming if artistName is null.");
    }

    @Test
    void prepareStreamingArtistNotOnlineUser() {
        keyStub.attach(new User("NotOnline"));
        when(songDatabase.getSong(SONG_NAME, ARTIST_NAME)).thenReturn(song);
        assertThrows(IllegalArgumentException.class,
                () -> onlineUsers.prepareStreaming(keyStub, SONG_NAME, ARTIST_NAME),
                "Cannot prepare for streaming if user is not online.");
    }

    @Test
    @Order(4)
    void prepareStreamingArtistSuccessful() {
        keyStub.attach(user);
        onlineUsers.addUser(keyStub);
        when(songDatabase.getSong(SONG_NAME, ARTIST_NAME)).thenReturn(song);
        AudioFormat expected = new AudioFormat( 48000, 16, 2, true, false);
        AudioFormat actual =  assertDoesNotThrow(
                () -> onlineUsers.prepareStreaming(keyStub, SONG_NAME, ARTIST_NAME),
                "Prepare streaming works correct for online user");

        assertEquals(expected.getChannels(), actual.getChannels(),
                "Audio format channels are the same.");
        assertEquals(expected.getEncoding(), actual.getEncoding(),
                "Audio format encodings are the same.");
        assertEquals(expected.getSampleRate(), actual.getSampleRate(),
                "Audio format sample rates are the same.");
        assertEquals(expected.getFrameSize(), actual.getFrameSize(),
                "Audio format frame sizes are the same.");
        assertEquals(expected.getSampleSizeInBits(), actual.getSampleSizeInBits(),
                "Audio format sample sizes in bits are the same.");
        assertEquals(expected.getFrameRate(), actual.getFrameRate(),
                "Audio format frame rates are the same.");

        assertFalse(onlineUsers.isListening(user.getUsername()),
                "User  should not yet be listed as listening");
        assertTrue(onlineUsers.isOnline(user.getUsername()),
                "User is already online");
    }

    @Test
    void testStartStreamingNullUsername() {
        assertThrows(IllegalArgumentException.class, () -> onlineUsers.startStreaming(null),
                "Cannot start streaming if username is null.");
    }

    @Test
    void testStartStreamingNotOnline() {
        assertThrows(IllegalArgumentException.class, () -> onlineUsers.startStreaming("NotOnline"),
                "Cannot start streaming if user is offline.");
    }

    @Test
    void testStartStreamingNotPreparedForStreaming() {
        SelectionKeyStub notListening = new SelectionKeyStub();
        notListening.attach(new User("NotListening"));
        onlineUsers.addUser(notListening);
        assertThrows(IllegalArgumentException.class, () -> onlineUsers.startStreaming("NotOnline"),
                "Cannot start streaming id user is not prepared for streaming.");
    }

    @Test
    @Order(5)
    void testStartStreamingNullMusicChannel() {
        assertThrows(IllegalStateException.class, () -> onlineUsers.startStreaming(user.getUsername()),
                "Cannot start streaming if the users music channel is null.");
    }

    @Test
    @Order(6)
    void testStartStreamingSuccessful() {
        keyStub.attach(user);
        onlineUsers.removeUser(keyStub);
        SelectionKeyStub musicKey = new SelectionKeyStub();
        user.setMusicKey(musicKey);
        keyStub.attach(user);
        onlineUsers.addUser(keyStub);
        when(songDatabase.getSong(SONG_NAME, ARTIST_NAME)).thenReturn(song);
        assertDoesNotThrow(() -> onlineUsers.prepareStreaming(keyStub, SONG_NAME, ARTIST_NAME),
                "Should prepare for streaming correctly.");
        assertDoesNotThrow(() -> onlineUsers.startStreaming(user.getUsername()),
                "Should not throw for an online user who is prepared for streaming.");

        assertTrue(onlineUsers.isOnline(user.getUsername()),
                 "User should be online.");
        assertTrue(onlineUsers.isListening(user.getUsername()),
                "User should be marked as listening.");
        assertEquals(SelectionKey.OP_WRITE, user.getMusicKey().interestOps(),
                "Users selection key should be interested in OP_WRITE.");
    }

    @Test
    void testGetSongFragmentNullKey() {
        assertThrows(IllegalArgumentException.class, () -> onlineUsers.getSongFragment(null),
                "Cannot get song fragment if key is null.");
    }

    @Test
    void testGetSongFragmentNotMusicKey() {
        assertThrows(IllegalStateException.class, () -> onlineUsers.getSongFragment(keyStub),
                "Cannot get song fragment if a request key is passed as parameter.");
    }

    @Test
    void testGetSongFragmentNotCurrentlyListening() {
        assertThrows(IllegalStateException.class, () -> onlineUsers.getSongFragment(new SelectionKeyStub()),
                "Cannot get the song fragment for a user who is not currently listening.");
    }

    @Test
    @Order(7)
    void testStopListeningCorrectKey() {
        assertDoesNotThrow(() -> onlineUsers.getSongFragment(user.getMusicKey()),
                "Stop listening with the correct key should not throw.");
    }

    @Test
    @Order(8)
    void testRemoveStreamingUser() {
        keyStub.attach(user);
        assertDoesNotThrow(() -> onlineUsers.removeUser(keyStub),
                "Remove user with existing user should not throw.");
    }

    @Test
    void testStopListeningNullKey() {
        assertThrows(IllegalArgumentException.class, () -> onlineUsers.stopListening(null),
                "Cannot stop listening for null key.");
    }

    @Test
    void testStopListeningUnknownKey() {
        assertThrows(IllegalStateException.class, () -> onlineUsers.stopListening(new SelectionKeyStub()),
                "Cannot stop listening for a key that cannot be found in the map.");
    }

    @Test
    @Order(9)
    void testStopListeningSuccessful() {
        SelectionKeyStub musicKey = new SelectionKeyStub();
        user.setMusicKey(musicKey);
        keyStub.attach(user);
        assertDoesNotThrow(() -> onlineUsers.addUser(keyStub));
        when(songDatabase.getSong(SONG_NAME, ARTIST_NAME)).thenReturn(song);
        assertDoesNotThrow(() -> onlineUsers.prepareStreaming(keyStub, SONG_NAME, ARTIST_NAME),
                "User should be prepared for streaming correctly.");
        assertDoesNotThrow(() -> onlineUsers.startStreaming(user.getUsername()),
                "User's streaming should start correctly.");
        assertDoesNotThrow(() -> onlineUsers.stopListening(musicKey),
                "Stop listening for already listening user should not throw");

        assertEquals(SelectionKey.OP_READ, ((User) keyStub.attachment()).getMusicKey().interestOps(),
                "User's musicKey key should only be interested for reading.");
        assertFalse(onlineUsers.isListening(user.getUsername()),
                "User should no longer be listening.");
    }

    @Test
    @Order(10)
    void testCloseAllStreams() {
        assertDoesNotThrow(() -> onlineUsers.prepareStreaming(keyStub, SONG_NAME, ARTIST_NAME),
                "User should be prepared correctly.");
        assertDoesNotThrow(() -> onlineUsers.startStreaming(user.getUsername()),
                "User should start listening correctly.");

        assertDoesNotThrow(onlineUsers::closeAllStreams, "Closing all running streams should not throw.");
    }

}