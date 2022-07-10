package com.vlado.spotify.logger;

import com.vlado.spotify.logger.exception.LoggerException;
import com.vlado.spotify.logger.log.Log;
import com.vlado.spotify.logger.options.LoggerOptions;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class Logger {
    private static final String LOG_FORMAT = "[%s] %s%n%s%n-------------------%n";
    private static final String LOG_FILE_NAME_FORMAT = "log-%d.txt";
    private int currentFileNum = 0;
    private final LoggerOptions options;

    private BufferedWriter logWriter;

    private Path currentFile;

    public Logger(LoggerOptions options) {
        this.options = options;
        this.currentFile = generateLogFilePath();
        try {
            Files.createDirectories(options.getDirectory());
            logWriter = openLogFile();
        } catch (IOException e) {
            if (options.shouldThrowErrors()) {
                throw new LoggerException("Log file opening error", e);
            }
        }
    }

    public LoggerOptions getOptions() {
        return options;
    }

    public void log(Log log) {
        if (log.level().getLevel() < options.getMinLogLevel().getLevel()) {
            return;
        }

        try {
            checkFileSize();

            logWriter.write(String.format(LOG_FORMAT,
                    log.level().name(), log.time(), log.message()));
            logWriter.flush();
        } catch (IOException e) {
            if (options.shouldThrowErrors()) {
                throw new LoggerException("Logging error occurred", e);
            }
        }
    }

    private void checkFileSize() throws IOException {
        if (Files.size(currentFile) >= options.getMaxFileSize()) {
            logWriter.close();
            currentFile = generateLogFilePath();
            logWriter = openLogFile();
        }
    }

    private Path generateLogFilePath() {
        if (options.getMaxLogsCount() > 0 && currentFileNum > options.getMaxLogsCount()) {
            currentFileNum = 0;
        }

        return Path.of(options.getDirectory().toString(), String.format(LOG_FILE_NAME_FORMAT, currentFileNum++));
    }

    private BufferedWriter openLogFile() throws IOException {
        return Files.newBufferedWriter(currentFile,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
