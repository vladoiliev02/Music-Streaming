package com.vlado.spotify.parsers;

import com.vlado.spotify.database.SongDatabase;
import com.vlado.spotify.server.Server;
import com.vlado.spotify.server.commands.AddSongCommand;
import com.vlado.spotify.server.commands.Command;
import com.vlado.spotify.server.commands.QuitCommand;
import com.vlado.spotify.validations.ParameterValidator;

import java.nio.file.Path;

public class ServerCommandParser {
    private static final String INVALID_ARGUMENTS = "Invalid arguments.%n Correct use: %s";
    private static final String QUIT = "quit";
    private static final String ADD_SONG = "add-song";

    private static final int ARG_1 = 1;
    private static final int ARG_2 = 2;
    private static final int ARG_3 = 3;

    private final Server server;

    public ServerCommandParser(Server server) {
        this.server = server;
    }

    public Command parse(String input) {
        ParameterValidator.checkNull(input, "input");

        String command = ParsingUtil.getFirstWord(input);

        return switch (command) {
            case QUIT -> parseQuit(input);
            case ADD_SONG -> parseAddSong(input);
            default -> throw new IllegalArgumentException("Unknown server command: " + input);
        };
    }

    private Command parseQuit(String input) {
        ParameterValidator.checkNull(input, "input");

        String[] command = input.split("\\s+");

        if (command.length != 1) {
            throw new IllegalArgumentException(String.format(INVALID_ARGUMENTS, "quit"));
        }

        return new QuitCommand(server);
    }

    private Command parseAddSong(String input) {
        ParameterValidator.checkNull(input, "input");

        String[] command = ParsingUtil.multipleWordArgsSplit(input);

        if (command.length != 4) {
            throw new IllegalArgumentException(String.format(INVALID_ARGUMENTS,
                    "add-song <songName> <artistName> <pathToSong>"));
        }

        return new AddSongCommand(command[ARG_1], command[ARG_2], Path.of(command[ARG_3]), SongDatabase.instance());
    }
}
