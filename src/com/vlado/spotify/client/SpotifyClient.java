package com.vlado.spotify.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vlado.spotify.exceptions.UserErrorException;
import com.vlado.spotify.logger.Logger;
import com.vlado.spotify.logger.log.Log;
import com.vlado.spotify.logger.log.LogLevel;
import com.vlado.spotify.logger.options.LoggerOptions;
import com.vlado.spotify.server.response.ServerResponse;
import com.vlado.spotify.server.response.ResponseBuffer;
import com.vlado.spotify.validations.ParameterValidator;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class SpotifyClient implements Client {
    private static final String CONNECTION_ERROR =
            "Connection error occurred. Please try again later or contact an administrator.";
    private static final int BUFFER_SIZE = 8192;
    private static final Path LOGS_PATH = Path.of("resources", "client", "logs");
    private static final int LOG_FILE_SIZE = 32768;
    private static final Object MUSIC_KEY_ATTACHMENT = new Object();

    private final AtomicBoolean isConnected;
    private Selector selector;

    private final String host;
    private final int port;
    private final Gson gson;
    private final Logger logger;

    private final ResponseBuffer responseBuffer;
    private final ResponseBuffer songBuffer;

    private boolean isListening;
    private SourceDataLine dataLine;
    private String username;

    private ByteBuffer buffer;

    public SpotifyClient(String host, int port) {
        ParameterValidator.checkNull(host, "host");
        ParameterValidator.checkEmpty(host, "host");
        ParameterValidator.checkBlank(host, "host");

        this.host = host;
        this.port = port;
        this.isConnected = new AtomicBoolean(false);
        this.responseBuffer = new ResponseBuffer(BUFFER_SIZE);
        this.songBuffer = new ResponseBuffer(BUFFER_SIZE);
        this.gson = new GsonBuilder().setLenient().create();
        this.logger = initializeLogger();
    }

    @Override
    public void startClient() {
        try (SocketChannel clientRequestChannel = SocketChannel.open();
             Selector selector = Selector.open()) {
            this.selector = selector;

            SelectionKey requestKey = setUp(clientRequestChannel);
            startRequestSender(requestKey);

            while (isConnected.get()) {
                int ready = selector.select();
                if (ready == 0) {
                    continue;
                }

                var it = selector.selectedKeys().iterator();
                while (it.hasNext()) {
                    SelectionKey key = it.next();

                    try {
                        synchronized (key) {
                            if (key.isReadable()) {
                                receiveResponse(key);
                            }
                        }
                    } catch (SocketException e) {
                        System.out.println(CONNECTION_ERROR);
                        logException(e);
                        isConnected.set(false);
                        break;
                    } catch (UserErrorException e) {
                        System.out.println(e.getMessage());
                        logException(e);
                    } catch (Throwable e) {
                        logException(e);
                    }

                    it.remove();
                }
            }

        } catch (IOException e) {
            System.out.println(CONNECTION_ERROR);
            logException(e);
        }
    }

    public void stopClient() {
        isConnected.set(false);

        if (selector.isOpen()) {
            selector.wakeup();
        }
    }

    public void startRequestSender(SelectionKey clientKey) {
        ParameterValidator.checkNull(clientKey, "clientKey");

        Thread commandSender = new Thread(() -> {
            try (Scanner scanner = new Scanner(System.in)) {
                while (true) {
                    String request = scanner.nextLine().strip();

                    if (request.equals("disconnect")) {
                        stopClient();
                    } else if (request.isEmpty() || request.isBlank()) {
                        continue;
                    }

                    try {
                        synchronized (clientKey) {
                            sendRequest(request, (SocketChannel) clientKey.channel());
                        }
                    } catch (SocketException e) {
                        System.out.println(CONNECTION_ERROR);
                        logException(e);
                    } catch (Throwable e) {
                        System.out.println(e.getMessage());
                        logException(e);
                    }
                }
            }
        });
        commandSender.setName("Command Sender");
        commandSender.setDaemon(true);

        commandSender.start();
    }

    private SelectionKey setUp(SocketChannel client) throws IOException {
        ParameterValidator.checkNull(client, "client");

        client.connect(new InetSocketAddress(host, port));
        client.configureBlocking(false);

        buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
        isConnected.set(true);

        return client.register(selector, SelectionKey.OP_READ);
    }

    private void sendRequest(String request, SocketChannel client) throws IOException {
        ParameterValidator.checkNull(client, "client");
        ParameterValidator.checkNull(request, "request");
        ParameterValidator.checkEmpty(request, "request");
        ParameterValidator.checkBlank(request, "request");

        buffer.clear();
        buffer.put(request.getBytes(StandardCharsets.UTF_8));
        buffer.flip();

        client.write(buffer);
    }

    private void receiveResponse(SelectionKey clientKey) throws IOException, LineUnavailableException {
        ParameterValidator.checkNull(clientKey, "clientKey");

        buffer.clear();
        synchronized (clientKey) {
            SocketChannel client = (SocketChannel) clientKey.channel();
            client.read(buffer);
        }
        buffer.flip();

        if (clientKey.attachment() == MUSIC_KEY_ATTACHMENT && isListening) {
            songBuffer.putBack(buffer);
            readResponseBuffer(songBuffer, clientKey, false);
        } else {
            responseBuffer.putBack(buffer);
            readResponseBuffer(responseBuffer, clientKey, true);
        }
    }

    private void readResponseBuffer(ResponseBuffer responseBuffer, SelectionKey key, boolean print)
            throws LineUnavailableException, IOException {
        ParameterValidator.checkNull(responseBuffer, "responseBuffer");
        ParameterValidator.checkNull(key, "key");

        byte[] responseBytes;
        while ((responseBytes = responseBuffer.getNext()) != null) {
            byte code = responseBytes[0];

            if (code == ResponseBuffer.MESSAGE) {
                String responseJson = new String(responseBytes, 1, responseBytes.length - 1, StandardCharsets.UTF_8);
                ServerResponse response = gson.fromJson(responseJson, ServerResponse.class);
                String serverResponse = handleTextResponse(response, key);

                if (print) {
                    System.out.println(serverResponse);
                } else {
                    logResponse(response);
                }
            } else if (code == ResponseBuffer.FRAGMENT) {
                writeToDataLine(responseBytes, 1, responseBytes.length - 1);
            } else {
                throw new IllegalStateException("Unrecognized response format received: " + code);
            }
        }
    }

    private String handleTextResponse(ServerResponse response, SelectionKey clientKey)
            throws LineUnavailableException, IOException {
        ParameterValidator.checkNull(response, "response");
        ParameterValidator.checkNull(clientKey, "clientKey");

        if (response.isSuccessfulLogInResponse()) {
            this.username = response.getMessage();
            return String.format("Successfully logged in as %s.", this.username);
        } else if (response.isLoggedOutResponse()) {
            this.username = null;
            breakIncomingStream(clientKey);
        } else if (response.isStreamingStoppedResponse()) {
            if (isListening) {
                breakIncomingStream(clientKey);
            } else {
                return "Nothing is currently playing.";
            }
        } else if (response.isReadyToStreamResponse()) {
            SocketChannel musicChannel = (SocketChannel) clientKey.channel();
            sendRequest("beginStreaming " + username, musicChannel);
        } else if (response.isSongFormatResponse()) {
            AudioFormat format = response.getSongFormat().toAudioFormat();
            setUpSourceDataLine(format);
            prepareForIncomingStream(clientKey);
        } else if (response.isSongFragmentResponse()) {
            throw new IllegalStateException("Cannot handle song fragment here");
        }

        return response.getMessage();
    }

    private void writeToDataLine(byte[] fragment, int offset, int length) {
        ParameterValidator.checkNull(fragment, "fragment");
        ParameterValidator.checkNonNegative(offset, "offset");
        ParameterValidator.checkNonNegative(length, "length");

        if (dataLine.isOpen()) {
            dataLine.write(fragment, offset, length);
        } else {
            throw new IllegalStateException("Music cannot play when dataLine is not open");
        }
    }

    private void prepareForIncomingStream(SelectionKey clientKey) throws IOException {
        ParameterValidator.checkNull(clientKey, "clientKey");

        songBuffer.clear();
        breakIncomingStream(clientKey);

        SocketChannel musicChannel = SocketChannel.open();
        SelectionKey musicKey = setUp(musicChannel);
        clientKey.attach(musicKey);
        musicKey.attach(MUSIC_KEY_ATTACHMENT);
        String request = "musicConnect " + username;
        sendRequest(request, musicChannel);
        isListening = true;
    }

    private void breakIncomingStream(SelectionKey clientKey) throws IOException {
        ParameterValidator.checkNull(clientKey, "clientKey");

        if (clientKey.attachment() == MUSIC_KEY_ATTACHMENT) {
            SocketChannel musicChannel = (SocketChannel) clientKey.channel();
            musicChannel.close();
        } else if (clientKey.attachment() != null) {
            SelectionKey musicKey = (SelectionKey) clientKey.attachment();
            SocketChannel musicChannel = (SocketChannel) musicKey.channel();
            musicChannel.close();
            clientKey.attach(null);
        }

        isListening = false;
    }

    private void setUpSourceDataLine(AudioFormat format) throws LineUnavailableException {
        ParameterValidator.checkNull(format, "format");

        if (dataLine != null && dataLine.isOpen()) {
            dataLine.close();
        }

        dataLine = AudioSystem.getSourceDataLine(format);
        dataLine.open(format);
        dataLine.start();
    }

    private Logger initializeLogger() {
        return new Logger(LoggerOptions.builder(LOGS_PATH)
                .setMaxFileSize(LOG_FILE_SIZE)
                .build());
    }

    private void logException(Throwable e) {
        ParameterValidator.checkNull(e, "e");

        logger.log(Log.of(LogLevel.ERROR, String.format("Name: %s%nMessage: %s%nStackTrace: %s",
                e.toString(), e.getMessage(), Arrays.toString(e.getStackTrace()))));
    }

    private void logResponse(ServerResponse response) {
        ParameterValidator.checkNull(response, "response");

        if (logger != null) {
            logger.log(Log.of(LogLevel.MESSAGE,
                    String.format("User: %s%n Response: %s",
                            username, response)));
        }
    }

    public static void main(String[] args) {
        Client client = new SpotifyClient("localhost", 5555);

        client.startClient();
    }
}