package com.vlado.spotify.server.commands;

import com.vlado.spotify.server.Server;

public class QuitCommand implements Command {

    private final Server server;

    public QuitCommand(Server server) {
        this.server = server;
    }

    @Override
    public String execute() {
        server.stopServer();
        return "Exiting...";
    }
}
