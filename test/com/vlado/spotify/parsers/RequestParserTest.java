package com.vlado.spotify.parsers;

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
import com.vlado.spotify.request.SearchRequest;
import com.vlado.spotify.request.ShowPlaylistRequest;
import com.vlado.spotify.request.StopRequest;
import com.vlado.spotify.request.TopNSongsRequest;
import com.vlado.spotify.stubs.SelectionKeyStub;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RequestParserTest {

    private final RequestParser requestParser = new RequestParser();
    private final SelectionKeyStub keyStub = new SelectionKeyStub();

    @Test
    void testParseNullInput() {
        assertThrows(IllegalArgumentException.class, () -> requestParser.parse(null, keyStub),
                "Parsing null request throws exception.");
    }

    @Test
    void testParseNullKey() {
        assertThrows(IllegalArgumentException.class, () -> requestParser.parse("input", null),
                "Parsing with null key throws exception.");
    }

    @Test
    void testParseUnknownRequest() {
        assertThrows(UserErrorException.class, () -> requestParser.parse("unknown command", keyStub),
                "Parssing an unknown request throws exception.");
    }


    @Test
    void testParseRegister() {
        assertEquals(RegisterRequest.class, requestParser.parse("register username password", keyStub).getClass(),
                "Parsing register returns correct class.");
    }

    @Test
    void testParseRegisterInvalidArguments() {
        assertThrows(UserErrorException.class, () -> requestParser.parse("register", keyStub),
                "Parsing register with invalid arguments throws exception.");
        assertThrows(UserErrorException.class, () -> requestParser.parse("register username", keyStub),
                "Parsing register with invalid arguments throws exception.");
        assertThrows(UserErrorException.class, () -> requestParser.parse("register username password p", keyStub),
                "Parsing register with invalid arguments throws exception.");
    }

    @Test
    void testParseLogIn() {
        assertEquals(LogInRequest.class, requestParser.parse("login username password", keyStub).getClass(),
                "Parsing login returns correct class.");
    }

    @Test
    void testParseLogInInvalidArguments() {
        assertThrows(UserErrorException.class, () -> requestParser.parse("login", keyStub),
                "Parsing login with invalid arguments throws exception.");
        assertThrows(UserErrorException.class, () -> requestParser.parse("login username", keyStub),
                "Parsing login with invalid arguments throws exception.");
        assertThrows(UserErrorException.class, () -> requestParser.parse("login username password p", keyStub),
                "Parsing login with invalid arguments throws exception.");
    }

    @Test
    void testParseLogOut() {
        assertEquals(LogOutRequest.class, requestParser.parse("logout", keyStub).getClass(),
                "Parsing logout returns correct class.");
    }

    @Test
    void testParseLogOutInvalidArguments() {
        assertThrows(UserErrorException.class, () -> requestParser.parse("logout username", keyStub),
                "Parsing logout with invalid arguments throws exception.");
        assertThrows(UserErrorException.class, () -> requestParser.parse("logout username password p", keyStub),
                "Parsing logout with invalid arguments throws exception.");
    }

    @Test
    void testParseDisconnect() {
        assertEquals(DisconnectRequest.class, requestParser.parse("disconnect", keyStub).getClass(),
                "Parsing disconnect returns correct class.");
    }

    @Test
    void testParseDisconnectInvalidArguments() {
        assertThrows(UserErrorException.class, () -> requestParser.parse("disconnect username", keyStub),
                "Parsing disconnect with invalid arguments throws exception.");
        assertThrows(UserErrorException.class, () -> requestParser.parse("disconnect username password p", keyStub),
                "Parsing disconnect with invalid arguments throws exception.");
    }

    @Test
    void testParseSearch() {
        assertEquals(SearchRequest.class, requestParser.parse("search smth", keyStub).getClass(),
                "Parsing search returns correct class.");
        assertEquals(SearchRequest.class, requestParser.parse("search smth smth2", keyStub).getClass(),
                "Parsing search returns correct class.");
        assertEquals(SearchRequest.class, requestParser.parse("search smth smth2 smth3", keyStub).getClass(),
                "Parsing search returns correct class.");
    }

    @Test
    void testParseSearchInvalidArguments() {
        assertThrows(UserErrorException.class, () -> requestParser.parse("search", keyStub),
                "Parsing search with invalid arguments throws exception.");
    }

    @Test
    void testParseTopNSongsRequest() {
        assertEquals(TopNSongsRequest.class, requestParser.parse("top 5", keyStub).getClass(),
                "Parsing top returns correct class.");
    }

    @Test
    void testParseTopNSongsRequestInvalidArguments() {
        assertThrows(UserErrorException.class, () -> requestParser.parse("top", keyStub),
                "Parsing top with invalid arguments throws exception.");
        assertThrows(UserErrorException.class, () -> requestParser.parse("top username", keyStub),
                "Parsing top with invalid arguments throws exception.");
        assertThrows(UserErrorException.class, () -> requestParser.parse("top 0", keyStub),
                "Parsing top with invalid arguments throws exception.");
        assertThrows(UserErrorException.class, () -> requestParser.parse("top -5", keyStub),
                "Parsing top with invalid arguments throws exception.");
        assertThrows(UserErrorException.class, () -> requestParser.parse("top username password p", keyStub),
                "Parsing top with invalid arguments throws exception.");
    }

    @Test
    void testParsePlaySongNameRequest() {
        assertEquals(PlaySongNameRequest.class, requestParser.parse("play songName", keyStub).getClass(),
                "Parsing play returns correct class.");
        assertEquals(PlaySongNameRequest.class, requestParser.parse("play \"song name\"", keyStub).getClass(),
                "Parsing play returns correct class.");
        assertEquals(PlaySongNameArtistRequest.class, requestParser.parse("play songName artistName", keyStub).getClass(),
                "Parsing play returns correct class.");
        assertEquals(PlaySongNameArtistRequest.class, requestParser.parse("play \"song name\" \"artist name\"", keyStub).getClass(),
                "Parsing play returns correct class.");
    }

    @Test
    void testParsePlaySongNameRequestInvalidArguments() {
        assertThrows(UserErrorException.class, () -> requestParser.parse("play", keyStub),
                "Parsing play with invalid arguments throws exception.");
        assertThrows(UserErrorException.class, () -> requestParser.parse("play username password p", keyStub),
                "Parsing play with invalid arguments throws exception.");
    }

    @Test
    void testParseStopRequest() {
        assertEquals(StopRequest.class, requestParser.parse("stop", keyStub).getClass(),
                "Parsing stop returns correct class.");
    }

    @Test
    void testParseStopRequestInvalidArguments() {
        assertThrows(UserErrorException.class, () -> requestParser.parse("stop username", keyStub),
                "Parsing stop with invalid arguments throws exception.");
        assertThrows(UserErrorException.class, () -> requestParser.parse("stop username password p", keyStub),
                "Parsing stop with invalid arguments throws exception.");
    }

    @Test
    void testParseCreatePlaylistRequest() {
        assertEquals(CreatePlaylistRequest.class, requestParser.parse("create-playlist name", keyStub).getClass(),
                "Parsing create-playlist returns correct class.");
    }

    @Test
    void testParseCreatePlaylistRequestInvalidArguments() {
        assertThrows(UserErrorException.class, () -> requestParser.parse("create-playlist", keyStub),
                "Parsing create-playlist with invalid arguments throws exception.");
        assertThrows(UserErrorException.class, () -> requestParser.parse("create-playlist username name", keyStub),
                "Parsing create-playlist with invalid arguments throws exception.");
        assertThrows(UserErrorException.class, () -> requestParser.parse("create-playlist username password p", keyStub),
                "Parsing create-playlist with invalid arguments throws exception.");
    }

    @Test
    void testParseAddSongToPlaylistRequest() {
        assertEquals(AddSongToPlaylistRequest.class, requestParser.parse("add-song-to pname sname", keyStub).getClass(),
                "Parsing add-song-to returns correct class.");
        assertEquals(AddSongToPlaylistRequest.class, requestParser.parse("add-song-to \"p name\" \"s name\"", keyStub).getClass(),
                "Parsing add-song-to returns correct class.");
        assertEquals(AddSongNameArtistToPlaylistRequest.class, requestParser.parse("add-song-to pname sname aname", keyStub).getClass(),
                "Parsing add-song-to returns correct class.");
        assertEquals(AddSongNameArtistToPlaylistRequest.class, requestParser.parse(
                "add-song-to \"p name\" \"s name\" \"a name\"", keyStub).getClass(),
                "Parsing add-song-to returns correct class.");
    }

    @Test
    void testParseAddSongToPlaylistRequestInvalidArguments() {
        assertThrows(UserErrorException.class, () -> requestParser.parse("add-song-to", keyStub),
                "Parsing add-song-to with invalid arguments throws exception.");
        assertThrows(UserErrorException.class, () -> requestParser.parse("add-song-to username", keyStub),
                "Parsing add-song-to with invalid arguments throws exception.");
        assertThrows(UserErrorException.class, () -> requestParser.parse(
                "add-song-to username password p1 p2", keyStub),
                "Parsing add-song-to with invalid arguments throws exception.");
    }

    @Test
    void testParseShowPlaylistRequest() {
        assertEquals(ShowPlaylistRequest.class, requestParser.parse("show-playlist pname", keyStub).getClass(),
                "Parsing show-playlist returns correct class.");
        assertEquals(ShowPlaylistRequest.class, requestParser.parse("show-playlist \"p name\"", keyStub).getClass(),
                "Parsing show-playlist returns correct class.");
    }

    @Test
    void testParseShowPlaylistRequestInvalidArguments() {
        assertThrows(UserErrorException.class, () -> requestParser.parse("show-playlist", keyStub),
                "Parsing show-playlist with invalid arguments throws exception.");
        assertThrows(UserErrorException.class, () -> requestParser.parse("show-playlist username username", keyStub),
                "Parsing show-playlist with invalid arguments throws exception.");
        assertThrows(UserErrorException.class, () -> requestParser.parse("show-playlist username password p", keyStub),
                "Parsing show-playlist with invalid arguments throws exception.");
    }

    @Test
    void testParseMusicConnectionRequest() {
        assertEquals(MusicConnectionRequest.class, requestParser.parse("musicConnect username", keyStub).getClass(),
                "Parsing musicConnect returns correct class.");
    }

    @Test
    void testParseMusicConnectionRequestInvalidArguments() {
        assertThrows(UserErrorException.class, () -> requestParser.parse("musicConnect", keyStub),
                "Parsing musicConnect with invalid arguments throws exception.");
        assertThrows(UserErrorException.class, () -> requestParser.parse("musicConnect username username", keyStub),
                "Parsing musicConnect with invalid arguments throws exception.");
        assertThrows(UserErrorException.class, () -> requestParser.parse("musicConnect username password p", keyStub),
                "Parsing musicConnect with invalid arguments throws exception.");
    }

    @Test
    void testParseBeginStreamingRequest() {
        assertEquals(BeginStreamingRequest.class, requestParser.parse("beginStreaming username", keyStub).getClass(),
                "Parsing beginStreaming returns correct class.");
    }

    @Test
    void testParseBeginStreamingRequestInvalidArguments() {
        assertThrows(UserErrorException.class, () -> requestParser.parse("beginStreaming", keyStub),
                "Parsing beginStreaming with invalid arguments throws exception.");
        assertThrows(UserErrorException.class, () -> requestParser.parse("beginStreaming username username", keyStub),
                "Parsing beginStreaming with invalid arguments throws exception.");
        assertThrows(UserErrorException.class, () -> requestParser.parse("beginStreaming username password p", keyStub),
                "Parsing beginStreaming with invalid arguments throws exception.");
    }
}