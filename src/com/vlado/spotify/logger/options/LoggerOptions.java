package com.vlado.spotify.logger.options;

import com.vlado.spotify.logger.log.LogLevel;

import java.nio.file.Path;

public class LoggerOptions {
    private final Path directory;
    private final int maxFileSize;
    private boolean shouldThrowErrors;
    private int maxLogsCount;
    private LogLevel minLogLevel;

    private LoggerOptions(LoggerOptionsBuilder builder) {
        this.directory = builder.directory;
        this.maxFileSize = builder.maxFileSize;
        this.shouldThrowErrors = builder.shouldThrowErrors;
        this.minLogLevel = builder.minLogLevel;
        this.maxLogsCount = builder.maxLogsCount;
    }

    public static LoggerOptionsBuilder builder(Path directory) {
        return new LoggerOptionsBuilder(directory);
    }

    public Path getDirectory() {
        return directory;
    }

    public int getMaxFileSize() {
        return maxFileSize;
    }

    public boolean shouldThrowErrors() {
        return shouldThrowErrors;
    }

    public LogLevel getMinLogLevel() {
        return minLogLevel;
    }

    public int getMaxLogsCount() {
        return maxLogsCount;
    }

    public void setShouldThrowErrors(boolean shouldThrowErrors) {
        this.shouldThrowErrors = shouldThrowErrors;
    }

    public void setMinLogLevel(LogLevel minLogLevel) {
        this.minLogLevel = minLogLevel;
    }

    public void setMaxLogsCount(int maxLogsCount) {
        this.maxLogsCount = maxLogsCount;
    }

    public static class LoggerOptionsBuilder {
        private final Path directory;

        private int maxFileSize = 4096;
        private boolean shouldThrowErrors = false;
        private LogLevel minLogLevel = LogLevel.MESSAGE;
        private int maxLogsCount = 10;

        public LoggerOptionsBuilder(Path directory) {
            this.directory = directory;
        }

        public LoggerOptionsBuilder setMaxFileSize(int maxFileSize) {
            this.maxFileSize = maxFileSize;
            return this;
        }

        public LoggerOptionsBuilder setShouldThrowErrors(boolean shouldThrowErrors) {
            this.shouldThrowErrors = shouldThrowErrors;
            return this;
        }

        public LoggerOptionsBuilder setMinLogLevel(LogLevel minLogLevel) {
            this.minLogLevel = minLogLevel;
            return this;
        }

        public LoggerOptionsBuilder maxLogsCount(int maxLogsCount) {
            this.maxLogsCount = maxLogsCount;
            return this;
        }

        public LoggerOptions build() {
            return new LoggerOptions(this);
        }
    }
}
