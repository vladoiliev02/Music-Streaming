package com.vlado.spotify.parsers;

import com.vlado.spotify.database.OnlineUsers;
import com.vlado.spotify.database.SongDatabase;
import com.vlado.spotify.database.UserDatabase;
import com.vlado.spotify.exceptions.UserErrorException;
import com.vlado.spotify.request.AddSongNameArtistToPlaylistRequest;
import com.vlado.spotify.request.AddSongToPlaylistRequest;
import com.vlado.spotify.request.BeginStreamingRequest;
import com.vlado.spotify.request.CreatePlaylistRequest;
import com.vlado.spotify.request.DisconnectRequest;
import com.vlado.spotify.request.LogInRequest;
import com.vlado.spotify.request.LogOutRequest;
import com.vlado.spotify.request.MusicConnectionRequest;
import com.vlado.spotify.request.PlaySongNameArtistRequest;
import com.vlado.spotify.request.PlaySongNameRequest;
import com.vlado.spotify.request.RegisterRequest;
import com.vlado.spotify.request.Request;
import com.vlado.spotify.request.SearchRequest;
import com.vlado.spotify.request.ShowPlaylistRequest;
import com.vlado.spotify.request.StopRequest;
import com.vlado.spotify.request.TopNSongsRequest;
import com.vlado.spotify.validations.ParameterValidator;

import java.nio.channels.SelectionKey;
import java.util.Arrays;

public class RequestParser {
    private static final String SKIP_WHITE_SPACES = "\\s+";
    private static final String INVALID_ARGUMENTS_TO_COMMAND = "Invalid arguments.%nCorrect use: %s";

    private static final int NO_ARGUMENTS_COMMAND_LENGTH = 1;
    private static final int ONE_ARGUMENT_COMMAND_LENGTH = 2;
    private static final int TWO_ARGUMENTS_COMMAND_LENGTH = 3;
    private static final int THREE_ARGUMENTS_COMMAND_LENGTH = 4;

    private static final String REGISTER = "register";
    private static final String LOG_IN = "login";
    private static final String LOG_OUT = "logout";
    private static final String DISCONNECT = "disconnect";
    private static final String SEARCH = "search";
    private static final String TOP = "top";
    private static final String PLAY = "play";
    private static final String STOP = "stop";
    private static final String CREATE_PLAYLIST = "create-playlist";
    private static final String ADD_SONG_TO_PLAYLIST = "add-song-to";
    private static final String SHOW_PLAYLIST = "show-playlist";

    private static final String MUSIC_CONNECT = "musicConnect";
    private static final String BEGIN_STREAMING = "beginStreaming";

    private static final int ARG_1 = 1;
    private static final int ARG_2 = 2;
    private static final int ARG_3 = 3;

    public RequestParser() {
    }

    public Request parse(String request, SelectionKey key) {
        ParameterValidator.checkNull(request, "request");
        ParameterValidator.checkNull(key, "key");

        String command = ParsingUtil.getFirstWord(request);

        return switch (command) {
            case REGISTER -> parseRegister(request);
            case LOG_IN -> parseLogIn(request, key);
            case LOG_OUT -> parseLogOut(request, key);
            case PLAY -> parsePlay(request, key);
            case DISCONNECT -> parseDisconnect(request, key);
            case MUSIC_CONNECT -> parseMusicConnect(request, key);
            case BEGIN_STREAMING -> parseBeginStreaming(request);
            case STOP -> parseStop(request, key);
            case SEARCH -> parseSearch(request, key);
            case TOP -> parseTop(request, key);
            case CREATE_PLAYLIST -> parseCreatePlaylist(request, key);
            case ADD_SONG_TO_PLAYLIST -> parseAddSongTo(request, key);
            case SHOW_PLAYLIST -> parseShowPlaylist(request, key);
            default -> throw new UserErrorException("Unknown request: " + request);
        };
    }

    private Request parseRegister(String input) {
        ParameterValidator.checkNull(input, "input");

        String[] request = input.split(SKIP_WHITE_SPACES);

        if (request.length != TWO_ARGUMENTS_COMMAND_LENGTH) {
            throw new UserErrorException(String.format(
                    INVALID_ARGUMENTS_TO_COMMAND, "register <username> <password>"));
        }

        return new RegisterRequest(request[ARG_1], request[ARG_2], UserDatabase.instance());
    }

    private Request parseLogIn(String input, SelectionKey key) {
        ParameterValidator.checkNull(input, "input");
        ParameterValidator.checkNull(key, "key");

        String[] request = input.split(SKIP_WHITE_SPACES);

        if (request.length != TWO_ARGUMENTS_COMMAND_LENGTH) {
            throw new UserErrorException(String.format(
                    INVALID_ARGUMENTS_TO_COMMAND, "login <username> <password>"));
        }

        return new LogInRequest(request[ARG_1], request[ARG_2], key, UserDatabase.instance(), OnlineUsers.instance());
    }

    private Request parseLogOut(String input, SelectionKey key) {
        ParameterValidator.checkNull(input, "input");
        ParameterValidator.checkNull(key, "key");

        String[] request = input.split(SKIP_WHITE_SPACES);

        if (request.length != NO_ARGUMENTS_COMMAND_LENGTH) {
            throw new UserErrorException(String.format(
                    INVALID_ARGUMENTS_TO_COMMAND, "logout"));
        }

        return new LogOutRequest(key, OnlineUsers.instance());
    }

    private Request parseDisconnect(String input, SelectionKey key) {
        ParameterValidator.checkNull(input, "input");
        ParameterValidator.checkNull(key, "key");

        String[] request = input.split(SKIP_WHITE_SPACES);

        if (request.length != NO_ARGUMENTS_COMMAND_LENGTH) {
            throw new UserErrorException(String.format(
                    INVALID_ARGUMENTS_TO_COMMAND, "disconnect"));
        }

        return new DisconnectRequest(key, OnlineUsers.instance());
    }

    private Request parsePlay(String input, SelectionKey key) {
        ParameterValidator.checkNull(input, "input");
        ParameterValidator.checkNull(key, "key");

        String[] request = ParsingUtil.multipleWordArgsSplit(input);

        if (request.length < ONE_ARGUMENT_COMMAND_LENGTH || request.length > TWO_ARGUMENTS_COMMAND_LENGTH) {
            throw new UserErrorException(String.format(
                    INVALID_ARGUMENTS_TO_COMMAND, "play <songName>" + System.lineSeparator() +
                            "play <songName> <artist>"));
        }

        if (request.length == ONE_ARGUMENT_COMMAND_LENGTH) {
            return new PlaySongNameRequest(request[ARG_1], key, OnlineUsers.instance(), SongDatabase.instance());
        } else {
            return new PlaySongNameArtistRequest(request[ARG_1], request[ARG_2], key,
                    OnlineUsers.instance(), SongDatabase.instance());
        }
    }

    private Request parseMusicConnect(String input, SelectionKey key) {
        ParameterValidator.checkNull(input, "input");
        ParameterValidator.checkNull(key, "key");

        String[] request = input.split(SKIP_WHITE_SPACES);

        if (request.length != ONE_ARGUMENT_COMMAND_LENGTH) {
            throw new UserErrorException(String.format(
                    INVALID_ARGUMENTS_TO_COMMAND, "musicConnect <username>"));
        }

        return new MusicConnectionRequest(request[ARG_1], key, OnlineUsers.instance());
    }

    private Request parseBeginStreaming(String input) {
        ParameterValidator.checkNull(input, "input");

        String[] request = input.split(SKIP_WHITE_SPACES);

        if (request.length != ONE_ARGUMENT_COMMAND_LENGTH) {
            throw new UserErrorException(String.format(
                    INVALID_ARGUMENTS_TO_COMMAND, "beginStreaming <username>"));
        }

        return new BeginStreamingRequest(request[ARG_1], OnlineUsers.instance());
    }

    private Request parseStop(String input, SelectionKey key) {
        ParameterValidator.checkNull(input, "input");
        ParameterValidator.checkNull(key, "key");

        String[] request = input.split(SKIP_WHITE_SPACES);

        if (request.length != NO_ARGUMENTS_COMMAND_LENGTH) {
            throw new UserErrorException(String.format(
                    INVALID_ARGUMENTS_TO_COMMAND, "stop"));
        }

        return new StopRequest(key, OnlineUsers.instance());
    }

    private Request parseSearch(String input, SelectionKey key) {
        ParameterValidator.checkNull(input, "input");
        ParameterValidator.checkNull(key, "key");

        String[] request = ParsingUtil.multipleWordArgsSplit(input);

        if (request.length < ONE_ARGUMENT_COMMAND_LENGTH) {
            throw new UserErrorException(String.format(
                    INVALID_ARGUMENTS_TO_COMMAND, "search <keyWords...>"));
        }

        String[] keyWords = Arrays.stream(request).skip(1).toArray(String[]::new);
        return new SearchRequest(keyWords, key, SongDatabase.instance());
    }

    private Request parseTop(String input, SelectionKey key) {
        ParameterValidator.checkNull(input, "input");
        ParameterValidator.checkNull(key, "key");

        String[] request = input.split(SKIP_WHITE_SPACES);

        if (request.length != ONE_ARGUMENT_COMMAND_LENGTH) {
            throw new UserErrorException(String.format(
                    INVALID_ARGUMENTS_TO_COMMAND, "top n"));
        }

        try {
            return new TopNSongsRequest(Integer.parseInt(request[ARG_1]), key, SongDatabase.instance());
        } catch (NumberFormatException e) {
            throw new UserErrorException("Parameter of request: top <n>, must be a number.");
        }
    }

    private Request parseCreatePlaylist(String input, SelectionKey key) {
        ParameterValidator.checkNull(input, "input");
        ParameterValidator.checkNull(key, "key");

        String[] request = ParsingUtil.multipleWordArgsSplit(input);

        if (request.length != ONE_ARGUMENT_COMMAND_LENGTH) {
            throw new UserErrorException(String.format(
                    INVALID_ARGUMENTS_TO_COMMAND, "create-playlist <name>"));
        }

        return new CreatePlaylistRequest(request[ARG_1], key, SongDatabase.instance());
    }

    private Request parseAddSongTo(String input, SelectionKey key) {
        ParameterValidator.checkNull(input, "input");
        ParameterValidator.checkNull(key, "key");

        String[] request = ParsingUtil.multipleWordArgsSplit(input);

        if (request.length < TWO_ARGUMENTS_COMMAND_LENGTH || request.length > THREE_ARGUMENTS_COMMAND_LENGTH) {
            throw new UserErrorException(String.format(
                    INVALID_ARGUMENTS_TO_COMMAND, "add-song-to <playlistName> <songName>" + System.lineSeparator() +
                            "add-song-to <playlistName> <songName> <artistName>"));
        }

        if (request.length == TWO_ARGUMENTS_COMMAND_LENGTH) {
            return new AddSongToPlaylistRequest(request[ARG_1], request[ARG_2], key, SongDatabase.instance());
        } else {
            return new AddSongNameArtistToPlaylistRequest(request[ARG_1], request[ARG_2], request[ARG_3], key,
                    SongDatabase.instance());
        }
    }

    private Request parseShowPlaylist(String input, SelectionKey key) {
        ParameterValidator.checkNull(input, "input");
        ParameterValidator.checkNull(key, "key");

        String[] request = ParsingUtil.multipleWordArgsSplit(input);

        if (request.length != ONE_ARGUMENT_COMMAND_LENGTH) {
            throw new UserErrorException(String.format(
                    INVALID_ARGUMENTS_TO_COMMAND, "show-playlist <playlistName>"));
        }

        return new ShowPlaylistRequest(request[ARG_1], key, SongDatabase.instance());
    }
}
