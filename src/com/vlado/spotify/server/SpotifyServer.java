package com.vlado.spotify.server;

import com.vlado.spotify.database.OnlineUsers;
import com.vlado.spotify.database.SongDatabase;
import com.vlado.spotify.database.UserDatabase;
import com.vlado.spotify.request.DisconnectRequest;
import com.vlado.spotify.server.response.ResponseSender;
import com.vlado.spotify.song.SongFragment;
import com.vlado.spotify.logger.Logger;
import com.vlado.spotify.logger.log.Log;
import com.vlado.spotify.logger.log.LogLevel;
import com.vlado.spotify.logger.options.LoggerOptions;
import com.vlado.spotify.executors.RequestExecutor;
import com.vlado.spotify.executors.CommandExecutor;
import com.vlado.spotify.server.response.ResponseStatus;
import com.vlado.spotify.server.response.ServerResponse;
import com.vlado.spotify.validations.ParameterValidator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class SpotifyServer implements Server {
    private static final int BUFFER_SIZE = 8192;

    private static final int LOG_FILE_SIZE = 32768;
    private static final String LOG_MESSAGE_FORMAT = "Exception: %s%nMessage: %s%nStackTrace: %s";
    private static final Path CLIENT_LOGS_PATH = Path.of("resources", "server", "clientRequestsLogs");
    private static final Path SERVER_LOGS_PATH = Path.of("resources", "server", "serverCommandsLogs");
    private static final Object MUSIC_CHANNEL_ATTACHMENT = null;

    private final String host;
    private final int port;

    private ByteBuffer buffer;
    private Selector selector;

    private final Logger clientLogger;
    private final AtomicBoolean isWorking = new AtomicBoolean(false);
    private final RequestExecutor requestExecutor;

    public SpotifyServer(String host, int port) {
        this.host = host;
        this.port = port;
        this.clientLogger = initializeClientLogger();
        this.requestExecutor = new RequestExecutor(clientLogger);
    }

    @Override
    public void startServer() {
        if (host == null) {
            throw new IllegalStateException("Host cannot be null");
        }

        try (ServerSocketChannel server = ServerSocketChannel.open()) {
            setUpServer(server);

            while (isWorking.get()) {
                int ready = selector.select();
                if (ready == 0) {
                    continue;
                }

                var it = selector.selectedKeys().iterator();
                while (it.hasNext()) {
                    SelectionKey key = it.next();

                    try {
                        handleKey(key);
                    } catch (Throwable e) {
                        logError(e);
                    }

                    it.remove();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setUpServer(ServerSocketChannel server) throws IOException {
        ParameterValidator.checkNull(server, "server");

        server.bind(new InetSocketAddress(host, port));
        server.configureBlocking(false);

        selector = Selector.open();
        server.register(selector, SelectionKey.OP_ACCEPT);

        buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);

        startServerCommandExecutor();
        isWorking.set(true);
    }

    @Override
    public void stopServer() {
        SongDatabase.instance().saveSongs();

        OnlineUsers.instance().closeAllStreams();
        isWorking.set(false);

        if (selector.isOpen()) {
            selector.wakeup();
        }
    }

    private void handleKey(SelectionKey key) throws IOException {
        ParameterValidator.checkNull(key, "key");

        if (key.isReadable()) {
            ServerResponse response = readClientRequest(key);

            if (response != null && key.channel().isOpen()) {
                ResponseSender.instance().send(key, response);
            }
        } else if (key.isWritable()) {
            if (key.attachment() == MUSIC_CHANNEL_ATTACHMENT) {
                ServerResponse songFragment = getSongFragment(key);
                ResponseSender.instance().send(key, songFragment);
            } else {
                ResponseSender.instance().sendQueued(key, (SocketChannel) key.channel());
            }
        } else if (key.isAcceptable()) {
            acceptConnection(key);
        }
    }

    private void acceptConnection(SelectionKey key) throws IOException {
        ParameterValidator.checkNull(key, "key");

        key.attach(null);

        ServerSocketChannel server = (ServerSocketChannel) key.channel();
        SocketChannel client = server.accept();

        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
    }

    private ServerResponse readClientRequest(SelectionKey key) throws IOException {
        ParameterValidator.checkNull(key, "key");

        SocketChannel client = (SocketChannel) key.channel();

        buffer.clear();
        try {
            int readBytes = client.read(buffer);
            if (readBytes < 0) {
                new DisconnectRequest(key, OnlineUsers.instance()).execute();
                client.close();
                return null;
            }
            buffer.flip();
        } catch (IOException e) {
            new DisconnectRequest(key, OnlineUsers.instance()).execute();
            client.close();
            throw new RuntimeException("Connection error occurred");
        }

        byte[] temp = new byte[buffer.remaining()];
        buffer.get(temp);
        String command = new String(temp, StandardCharsets.UTF_8);

        return requestExecutor.execute(command, key);
    }

    private ServerResponse getSongFragment(SelectionKey key) {
        ParameterValidator.checkNull(key, "key");

        ServerResponse response;

        SongFragment fragment = OnlineUsers.instance().getSongFragment(key);

        if (fragment != null) {
            response = ServerResponse.of(ResponseStatus.OK, fragment);
        } else {
            response = ServerResponse.of(ResponseStatus.STOP_STREAMING, "Song ended.");
        }

        return response;
    }

    private void startServerCommandExecutor() {
        Thread commandExecutor = new Thread(new CommandExecutor(this, SERVER_LOGS_PATH));
        commandExecutor.setDaemon(true);
        commandExecutor.start();
    }

    private void logError(Throwable e) {
        ParameterValidator.checkNull(e, "e");

        clientLogger.log(Log.of(LogLevel.ERROR,
                String.format(LOG_MESSAGE_FORMAT,
                        e.toString(), e.getMessage(), Arrays.toString(e.getStackTrace()))));
    }

    private Logger initializeClientLogger() {
        LoggerOptions options = LoggerOptions.builder(CLIENT_LOGS_PATH)
                .setMaxFileSize(LOG_FILE_SIZE)
                .build();
        return new Logger(options);
    }

    public static void main(String[] args) {
        SpotifyServer server = new SpotifyServer("localhost", 5555);

        try (var writer = new BufferedWriter(new FileWriter("resources\\userdata.txt", true));
             var reader = new BufferedReader(new FileReader("resources\\userdata.txt"))) {

            UserDatabase.instance().setWriter(writer);
            UserDatabase.instance().readUsers(reader);
            SongDatabase.instance().loadSongs();
            SongDatabase.instance().loadPlaylists();

            server.startServer();
        } catch (Throwable e) {
            System.err.println(e.getMessage());
            server.logError(e);
        }
    }
}
