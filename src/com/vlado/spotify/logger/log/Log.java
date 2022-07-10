package com.vlado.spotify.logger.log;

import java.time.LocalDateTime;

public record Log(LocalDateTime time,
                  LogLevel level,
                  String message) {

    public static Log of(LocalDateTime time, LogLevel level, String message) {
        return new Log(time, level, message);
    }

    public static Log of(LogLevel level, String message) {
        return Log.of(LocalDateTime.now(), level, message);
    }
}
