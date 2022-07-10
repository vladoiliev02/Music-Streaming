package com.vlado.spotify.server.response;

import java.nio.ByteBuffer;

import com.carrotsearch.hppc.ByteArrayDeque;
import com.carrotsearch.hppc.ByteDeque;
import com.vlado.spotify.validations.ParameterValidator;

public class ResponseBuffer {
    public static final int FRAGMENT_SIZE_BYTES = 4;
    public static final int CODE_BYTES = 1;
    public static final byte FRAGMENT = 0;
    public static final byte MESSAGE = 1;

    private static final int MINIMAL_MESSAGE_SIZE = FRAGMENT_SIZE_BYTES + CODE_BYTES + 1;
    private static final int PARTIAL_MESSAGE_ERROR = -1;
    private final ByteDeque buffer;

    public ResponseBuffer(int bufferSize) {
        ParameterValidator.checkPositive(bufferSize, "bufferSize");
        this.buffer = new ByteArrayDeque(bufferSize);
    }

    public void putBack(ByteBuffer byteBuffer) {
        ParameterValidator.checkNull(byteBuffer, "byteBuffer");

        while (byteBuffer.hasRemaining()) {
            buffer.addLast(byteBuffer.get());
        }
    }

    public void putBack(byte[] bytes) {
        ParameterValidator.checkNull(bytes, "bytes");

        for (byte b : bytes) {
            buffer.addLast(b);
        }
    }

    public void putFront(byte[] bytes) {
        ParameterValidator.checkNull(bytes, "bytes");

        for (int i = bytes.length - 1; i >= 0; --i) {
            buffer.addFirst(bytes[i]);
        }
    }
    public void putFront(ByteBuffer byteBuffer) {
        ParameterValidator.checkNull(byteBuffer, "byteBuffer");

        byte[] bytes = new byte[byteBuffer.remaining()];
        for (int i = bytes.length - 1; i >= 0; --i) {
            bytes[i] = byteBuffer.get();
        }

        for (byte b : bytes) {
            buffer.addFirst(b);
        }
    }

    public byte[] getNext() {
        int fragmentSize = getResponseSize();

        if (fragmentSize == PARTIAL_MESSAGE_ERROR) {
            return null;
        }

        for (int i = 0; i < FRAGMENT_SIZE_BYTES; ++i) {
            buffer.removeFirst();
        }

        byte[] fragment = new byte[fragmentSize];
        for (int i = 0; i < fragmentSize; ++i) {
            fragment[i] = buffer.removeFirst();
        }

        return fragment;
    }

    public byte[] peekNext() {
        int fragmentSize = getResponseSize();

        if (fragmentSize == PARTIAL_MESSAGE_ERROR) {
            return null;
        }

        var it = buffer.iterator();
        for (int i = 0; i < FRAGMENT_SIZE_BYTES && it.hasNext(); ++i) {
            it.next();
        }

        byte[] fragment = new byte[fragmentSize];
        int i;
        for (i = 0; i < fragmentSize && it.hasNext(); ++i) {
            fragment[i] = it.next().value;
        }

        return fragment;
    }

    public boolean skipNext() {
        int fragmentSize = getResponseSize();

        if (fragmentSize == PARTIAL_MESSAGE_ERROR) {
            return false;
        }

        fragmentSize += FRAGMENT_SIZE_BYTES;
        for (int i = 0; i < fragmentSize; ++i) {
            buffer.removeFirst();
        }

        return true;
    }

    public boolean hasResponse() {
        return getResponseSize() > 0;
    }

    public void clear() {
        buffer.clear();
    }

    public byte getNextCode() {
        if (hasResponse()) {
            var it = buffer.iterator();
            for (int i = 0; i < FRAGMENT_SIZE_BYTES; i++) {
                it.next();
            }

            return it.next().value;
        }

        return -1;
    }

    public boolean isEmpty() {
        return buffer.isEmpty();
    }

    private int getResponseSize() {
        if (buffer.size() < MINIMAL_MESSAGE_SIZE) {
            return PARTIAL_MESSAGE_ERROR;
        }

        int fragmentSize = 0;
        var it = buffer.iterator();
        for (int i = 0; i < FRAGMENT_SIZE_BYTES; i++) {
            fragmentSize <<= Byte.SIZE;
            fragmentSize |= ((int) it.next().value) & 0xFF;
        }

        if (fragmentSize > buffer.size() - FRAGMENT_SIZE_BYTES - CODE_BYTES) {
            return PARTIAL_MESSAGE_ERROR;
        }

        return fragmentSize + CODE_BYTES;
    }
}
