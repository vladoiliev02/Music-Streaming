package com.vlado.spotify.executors;

import com.vlado.spotify.logger.Logger;
import com.vlado.spotify.logger.log.Log;
import com.vlado.spotify.logger.log.LogLevel;
import com.vlado.spotify.logger.options.LoggerOptions;
import com.vlado.spotify.parsers.ServerCommandParser;
import com.vlado.spotify.server.Server;
import com.vlado.spotify.validations.ParameterValidator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Scanner;

public class CommandExecutor implements Runnable {
    private static final int LOG_FILE_SIZE = 32768;
    private final ServerCommandParser commandParser;
    private Logger logger;

    public CommandExecutor(Server server) {
        ParameterValidator.checkNull(server, "server");

        this.commandParser = new ServerCommandParser(server);
    }

    public CommandExecutor(Server server, Path loggerPath) {
        this(server);
        ParameterValidator.checkNull(loggerPath, "loggerPath");

        try {
            Files.createDirectories(loggerPath);
            this.logger = new Logger(LoggerOptions.builder(loggerPath)
                    .setMaxFileSize(LOG_FILE_SIZE)
                    .build());
        } catch (IOException e) {
            this.logger = null;
        }
    }

    @Override
    public void run() {
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                String command = scanner.nextLine();

                try {
                    String result = execute(command);
                    System.out.println(result);
                } catch (Throwable e) {
                    System.out.println(e.getMessage());
                    logError(e);
                }
            }
        }
    }

    private String execute(String command) {
        return commandParser.parse(command).execute();
    }

    private void logError(Throwable e) {
        ParameterValidator.checkNull(e, "e");

        if (logger != null) {
            logger.log(Log.of(LogLevel.ERROR, String.format("Message: %s%nStackTrace: %s",
                    e.getMessage(), Arrays.toString(e.getStackTrace()))));
        }
    }
}
