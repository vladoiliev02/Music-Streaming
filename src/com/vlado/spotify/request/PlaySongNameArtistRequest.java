package com.vlado.spotify.request;

import com.vlado.spotify.database.OnlineUsers;
import com.vlado.spotify.database.SongDatabase;
import com.vlado.spotify.server.response.ResponseStatus;
import com.vlado.spotify.server.response.ServerResponse;
import com.vlado.spotify.song.Song;
import com.vlado.spotify.song.SongFormat;
import com.vlado.spotify.validations.ParameterValidator;

import javax.sound.sampled.AudioFormat;
import java.nio.channels.SelectionKey;

public class PlaySongNameArtistRequest implements Request {
    private final String songName;
    private final String artistName;
    private final SelectionKey user;
    private final OnlineUsers onlineUsers;
    private final SongDatabase songDatabase;

    public PlaySongNameArtistRequest(String songName, String artistName, SelectionKey user,
                                     OnlineUsers onlineUsers, SongDatabase songDatabase) {
        this.songName = ParameterValidator.checkNull(songName, "songName");
        this.artistName = ParameterValidator.checkNull(artistName, "artistName");
        this.user = ParameterValidator.checkNull(user, "user");
        this.onlineUsers = ParameterValidator.checkNull(onlineUsers, "onlineUsers");
        this.songDatabase = ParameterValidator.checkNull(songDatabase, "songDatabase");
    }



    @Override
    public ServerResponse execute() {
        checkLoggedIn(user);

        Song song = songDatabase.getSong(songName, artistName);
        AudioFormat format = onlineUsers.prepareStreaming(user, songName, artistName);
        SongFormat songFormat = SongFormat.of(format);

        return ServerResponse.of(ResponseStatus.OK,
                String.format("Now playing: %s by %s.", song.name(), song.artist()), songFormat);
    }
}
