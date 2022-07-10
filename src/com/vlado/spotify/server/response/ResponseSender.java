package com.vlado.spotify.server.response;

import com.vlado.spotify.song.SongFragment;
import com.vlado.spotify.validations.ParameterValidator;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class ResponseSender {
    private static final String NULL_PARAM_ERROR = "%s cannot be null";
    private static final int BUFFER_SIZE = 8192;
    private static final byte MESSAGE = 1;
    private static final byte FRAGMENT = 0;

    private static final ResponseSender INSTANCE = new ResponseSender();

    private final ByteBuffer buffer;
    private final Map<SelectionKey, Deque<byte[]>> waitingResponses;

    private ResponseSender() {
        this.waitingResponses = new HashMap<>();
        this.buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
    }

    public static ResponseSender instance() {
        return INSTANCE;
    }

    public void send(SelectionKey client, ServerResponse response) throws IOException {
        ParameterValidator.checkNull(client, "client");
        ParameterValidator.checkNull(response, "response");

        SocketChannel clientChannel = (SocketChannel) client.channel();
        if (!clientChannel.isOpen()) {
            throw new IllegalStateException("Client channel is closed");
        }

        boolean canWrite = sendQueued(client, clientChannel);

        int responseSize = loadBuffer(response);

        if (canWrite) {
            int written = clientChannel.write(buffer);

            if (written != responseSize) {
                saveResponseRemaining(client, written);
            }
        } else {
            saveResponseRemaining(client, 0);
        }
    }

    public boolean sendQueued(SelectionKey client, SocketChannel clientChannel) throws IOException {
        ParameterValidator.checkNull(client, "client");
        ParameterValidator.checkNull(clientChannel, "clientChannel");

        boolean canWrite = true;

        if (waitingResponses.containsKey(client)) {
            Deque<byte[]> responseDeque = waitingResponses.get(client);

            while (!responseDeque.isEmpty() && canWrite) {
                byte[] byteResponse = responseDeque.peek();
                int loaded = loadBuffer(byteResponse);

                int written = clientChannel.write(buffer);
                if (written != loaded) {
                    canWrite = false;
                    saveResponseRemaining(client, written);
                }

                responseDeque.poll();
            }

            if (responseDeque.isEmpty()) {
                client.interestOps(SelectionKey.OP_READ);
            }
        }

        return canWrite;
    }

    public void removeClientsMessageQueue(SelectionKey client) {
        waitingResponses.remove(client);
    }

    private void saveResponseRemaining(SelectionKey client, int written) {
        ParameterValidator.checkNull(client, "client");

        if (!waitingResponses.containsKey(client)) {
            waitingResponses.put(client, new ArrayDeque<>());
        }

        Deque<byte[]> responseDeque = waitingResponses.get(client);
        byte[] remaining = new byte[buffer.remaining()];
        buffer.get(remaining);
        if (written == 0) {
            responseDeque.addLast(remaining);
        } else {
            responseDeque.addFirst(remaining);
        }

        client.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    }

    private int loadBuffer(ServerResponse response) {
        ParameterValidator.checkNull(response, "response");

        int responseSize;

        buffer.clear();
        if (response.getSongFragment() != null) {
            SongFragment fragment = response.getSongFragment();
            responseSize = fragment.getRead();
            buffer.putInt(responseSize);
            buffer.put(FRAGMENT);

            for (int i = 0; i < fragment.getRead(); ++i) {
                buffer.put(fragment.getFragment()[i]);
            }
        } else {
            byte[] message = response.toString().getBytes(StandardCharsets.UTF_8);
            responseSize = message.length;
            buffer.putInt(responseSize);
            buffer.put(MESSAGE);
            buffer.put(message);
        }
        buffer.flip();

        return responseSize + ResponseBuffer.FRAGMENT_SIZE_BYTES + ResponseBuffer.CODE_BYTES;
    }

    private int loadBuffer(byte[] bytes, int offset, int length) {
        ParameterValidator.checkNull(bytes, "bytes");
        ParameterValidator.checkNonNegative(offset, "offset");
        ParameterValidator.checkNonNegative(length, "length");

        buffer.clear();
        buffer.put(bytes, offset, length);
        buffer.flip();

        return length;
    }

    private int loadBuffer(byte[] bytes) {
        return loadBuffer(bytes, 0, bytes.length);
    }
}
