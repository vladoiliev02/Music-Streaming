package com.vlado.spotify.logger.log;

public enum LogLevel {
    INFO(1),
    MESSAGE(2),
    ERROR(3);

    private final int level;

    LogLevel(int level) {
        this.level = level;
    }

    public int getLevel() {
        return this.level;
    }
}
