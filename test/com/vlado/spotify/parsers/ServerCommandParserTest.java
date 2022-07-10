package com.vlado.spotify.parsers;

import com.vlado.spotify.server.Server;
import com.vlado.spotify.server.SpotifyServer;
import com.vlado.spotify.server.commands.AddSongCommand;
import com.vlado.spotify.server.commands.QuitCommand;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServerCommandParserTest {

    private final Server server = new SpotifyServer("host", 1234);
    private final ServerCommandParser serverCommandParser = new ServerCommandParser(server);

    @Test
    void testParseNullInput() {
        assertThrows(IllegalArgumentException.class, () -> serverCommandParser.parse(null),
                "Parsing null input throws exception.");
    }

    @Test
    void testParseUnknownCommand() {
        assertThrows(IllegalArgumentException.class, () -> serverCommandParser.parse("unknown command"),
                "Parsing unknown command throws exception.");
    }

    @Test
    void testParseQuitCommandSuccessful() {
        assertEquals(QuitCommand.class, serverCommandParser.parse("quit").getClass(),
                "Parsing quit command returns correct class.");
    }

    @Test
    void testParseQuitCommandInvalidArgs() {
        assertThrows(IllegalArgumentException.class, () -> serverCommandParser.parse("quit arg"),
                "Parsing quit with invalid arguments throw exception.");
    }

    @Test
    void testParseAddSongCommandSuccessful() {
        assertEquals(AddSongCommand.class, serverCommandParser.parse("add-song arg arg2 arg3").getClass(),
                "Parsing add-song command returns correct class.");
    }

    @Test
    void testParseAddSongCommandInvalidArgs() {
        assertThrows(IllegalArgumentException.class, () -> serverCommandParser.parse("add-song"),
                "Parsing add-song with invalid arguments throw exception.");
        assertThrows(IllegalArgumentException.class, () -> serverCommandParser.parse("add-song arg arg2"),
                "Parsing add-song with invalid arguments throw exception.");
        assertThrows(IllegalArgumentException.class, () -> serverCommandParser.parse("add-song arg arg2 arg3 arg4"),
                "Parsing add-song with invalid arguments throw exception.");
    }
}