package com.vlado.spotify.database;

import com.vlado.spotify.server.response.ResponseSender;
import com.vlado.spotify.song.SongFragment;
import com.vlado.spotify.song.Song;
import com.vlado.spotify.user.User;
import com.vlado.spotify.validations.ParameterValidator;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.channels.SelectionKey;
import java.util.HashMap;
import java.util.Map;

public class OnlineUsers {
    private static final int BUFFER_SIZE = 8187;

    private static final OnlineUsers INSTANCE = new OnlineUsers();

    private SongDatabase songDatabase = SongDatabase.instance();
    private final SongFragment songFragment;
    // Maps username to requestKey
    private final Map<String, SelectionKey> online;
    // Maps musicKey to AudioStream
    private final Map<SelectionKey, AudioInputStream> currentlyListening;

    private OnlineUsers() {
        this.online = new HashMap<>();
        this.currentlyListening = new HashMap<>();
        this.songFragment = new SongFragment(BUFFER_SIZE);
    }

    public static OnlineUsers instance() {
        return INSTANCE;
    }

    public void setSongDatabase(SongDatabase songDatabase) {
        this.songDatabase = songDatabase;
    }

    public AudioFormat prepareStreaming(SelectionKey userKey, String songName) {
        ParameterValidator.checkNull(userKey, "userKey");
        ParameterValidator.checkNull(userKey.attachment(), "userKey.attachment()");
        ParameterValidator.checkNull(songName, "songName");

        return prepareStreaming(userKey, songDatabase.getSong(songName));
    }

    public AudioFormat prepareStreaming(SelectionKey userKey, String songName, String artistName) {
        ParameterValidator.checkNull(userKey, "userKey");
        ParameterValidator.checkNull(userKey.attachment(), "userKey.attachment()");
        ParameterValidator.checkNull(songName, "songName");
        ParameterValidator.checkNull(artistName, "artistName");

        return prepareStreaming(userKey, songDatabase.getSong(songName, artistName));
    }

    public void startStreaming(String username) {
        ParameterValidator.checkNull(username, "username");

        if (!online.containsKey(username)) {
            throw new IllegalArgumentException(String.format("User: %s, is not online.", username));
        }
        SelectionKey userKey = online.get(username);

        if (!currentlyListening.containsKey(userKey)) {
            throw new IllegalArgumentException("Key is not prepared for listening");
        }

        SelectionKey musicKey = ((User) userKey.attachment()).getMusicKey();
        if (musicKey == null) {
            throw new IllegalStateException(String.format("User: %s, music channel is not connected", username));
        }

        AudioInputStream audioInputStream = currentlyListening.get(userKey);
        currentlyListening.remove(userKey);

        currentlyListening.put(musicKey, audioInputStream);
        musicKey.interestOps(SelectionKey.OP_WRITE);
    }

    public SongFragment getSongFragment(SelectionKey musicKey) {
        ParameterValidator.checkNull(musicKey, "musicKey");

        if (musicKey.attachment() != null) { // Is not a music channel key
            throw new IllegalStateException("Cannot play music on this key");
        }

        if (!currentlyListening.containsKey(musicKey)) {
            throw new IllegalStateException("Music key was not found");
        }

        AudioInputStream audioInputStream = currentlyListening.get(musicKey);
        if (audioInputStream == null) {
            throw new IllegalStateException("Users audioStream is null");
        }

        try {
            songFragment.read(audioInputStream);
        } catch (IOException e) {
            throw new UncheckedIOException("Song reading error occurred", e);
        }

        if (songFragment.getRead() < 0) {
            stopListening(musicKey);
            return null;
        }

        return songFragment;
    }

    public void stopListening(SelectionKey musicKey) {
        ParameterValidator.checkNull(musicKey, "musicKey");

        if (!currentlyListening.containsKey(musicKey)) {
            throw new IllegalStateException("Not currently listening.");
        }

        musicKey.interestOps(SelectionKey.OP_READ);
        AudioInputStream audioInputStream = currentlyListening.get(musicKey);
        try {
            audioInputStream.close();
        } catch (IOException e) {
            throw new UncheckedIOException("Error occurred while trying to close the audio stream of musicKey", e);
        }

        currentlyListening.remove(musicKey);
    }

    public boolean isListening(String username) {
        ParameterValidator.checkNull(username, "username");

        if (!online.containsKey(username)) {
            return false;
        }

        SelectionKey user = online.get(username);
        ParameterValidator.checkNull(user.attachment(), "user.attachment()");
        User attachment = (User) user.attachment();

        return currentlyListening.get(attachment.getMusicKey()) != null;
    }

    public boolean isOnline(String username) {
        return online.containsKey(username);
    }

    public void addUser(SelectionKey userKey) {
        ParameterValidator.checkNull(userKey, "userKey");

        User user = (User) userKey.attachment();
        if (isOnline(user.getUsername())) {
            throw new IllegalArgumentException(String.format("User: %s, is already online", user.getUsername()));
        }

        online.put(user.getUsername(), userKey);
    }

    public void removeUser(SelectionKey userKey) {
        ParameterValidator.checkNull(userKey, "userKey");

        User user = (User) userKey.attachment();
        if (!isOnline(user.getUsername())) {
            throw new IllegalArgumentException(String.format("User: %s, is not currently online.", user.getUsername()));
        }

        SelectionKey musicKey = user.getMusicKey();
        if (isListening(user.getUsername())) {
            AudioInputStream audioInputStream = currentlyListening.get(musicKey);

            if (audioInputStream != null) {
                try {
                    audioInputStream.close();
                } catch (IOException e) {
                    throw new UncheckedIOException("AudioStream closing error when adding new user", e);
                }
            }
        }

        currentlyListening.remove(musicKey);
        online.remove(user.getUsername());
        ResponseSender.instance().removeClientsMessageQueue(userKey);
    }

    public SelectionKey get(String username) {
        return online.get(username);
    }

    public void closeAllStreams() {
        for (AudioInputStream audioInputStream : currentlyListening.values()) {
            try {
                audioInputStream.close();
            } catch (IOException e) {
                throw new UncheckedIOException("Stream closing error", e);
            }
        }
    }

    public void clear() {
        closeAllStreams();
        online.clear();
        currentlyListening.clear();
    }

    private AudioFormat prepareStreaming(SelectionKey userKey, Song song) {
        ParameterValidator.checkNull(userKey, "userKey");
        ParameterValidator.checkNull(userKey.attachment(), "userKey.attachment()");

        User user = (User) userKey.attachment();

        if (!isOnline(user.getUsername())) {
            throw new IllegalArgumentException(String.format("User: %s, is not online.", user.getUsername()));
        }

        AudioInputStream audioInputStream;
        try {
            audioInputStream = AudioSystem.getAudioInputStream(song.path().toFile());
        } catch (Throwable e) {
            throw new RuntimeException("CurrentlyListening: audioInputStreamError", e);
        }

        if (isListening(user.getUsername())) {
            SelectionKey musicKey = getMusicKey(userKey);
            stopListening(musicKey);
        }

        currentlyListening.put(userKey, audioInputStream);

        songDatabase.updateSong(song.name(), song.artist());
        return audioInputStream.getFormat();
    }

    private SelectionKey getMusicKey(SelectionKey userKey) {
        ParameterValidator.checkNull(userKey, "userKey");
        ParameterValidator.checkNull(userKey.attachment(), "userKey.attachment()");

        User user = (User) userKey.attachment();

        return user.getMusicKey();
    }
}