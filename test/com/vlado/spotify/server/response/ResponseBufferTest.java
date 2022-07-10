package com.vlado.spotify.server.response;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

class ResponseBufferTest {

    @Test
    void testResponseBufferNegativeSize() {
        assertThrows(IllegalArgumentException.class, () -> new ResponseBuffer(-5),
                "Response buffer cannot have negative size.");
    }

    @Test
    void testResponseBufferZeroSize() {
        assertThrows(IllegalArgumentException.class, () -> new ResponseBuffer(0),
                "Response buffer cannot have 0 size.");
    }

    @Test
    void testResponseBufferPositiveSize() {
        assertDoesNotThrow(() -> new ResponseBuffer(5),
                "Response buffer with positive size does not throw.");
    }

    @Test
    void testPutBackByteBufferNull() {
        ResponseBuffer responseBuffer = new ResponseBuffer(5);
        assertThrows(IllegalArgumentException.class, () -> responseBuffer.putBack((ByteBuffer) null),
                "Cannot put back null byte buffer.");
        assertTrue(responseBuffer.isEmpty(),
                "Response buffer stays empty.");
        assertFalse(responseBuffer.hasResponse(),
                "Response buffer has no response.");
    }

    @Test
    void testPutBackByteBufferSuccessful() {
        ResponseBuffer responseBuffer = new ResponseBuffer(5);
        assertDoesNotThrow(() -> responseBuffer.putBack(ByteBuffer.wrap(new byte[]{0, 0, 0, 3, 1, 1, 2, 3})),
                "Put back of non null byte buffer does not throw.");

        assertTrue(responseBuffer.hasResponse(),
                "Buffer has a response with big enough buffer added.");
        assertArrayEquals(new byte[]{1, 1, 2, 3}, responseBuffer.peekNext(),
                "Has correct bytes inside.");
        assertArrayEquals(new byte[]{1, 1, 2, 3}, responseBuffer.getNext(),
                "Has correct bytes inside.");
        assertTrue(responseBuffer.isEmpty(),
                "Buffer is empty after bytes are extracted.");
        assertFalse(responseBuffer.hasResponse(),
                "Empty buffer has no response.");
    }

    @Test
    void testPutBackByteArrayNull() {
        ResponseBuffer responseBuffer = new ResponseBuffer(5);
        assertThrows(IllegalArgumentException.class, () -> responseBuffer.putBack((byte[]) null),
                "PutBack with null byte array throws exception.");
        assertTrue(responseBuffer.isEmpty(),
                "Buffer stays empty.");
        assertFalse(responseBuffer.hasResponse(),
                "Empty buffer has no response.");
    }

    @Test
    void testPutBackByteArraySuccessful() {
        ResponseBuffer responseBuffer = new ResponseBuffer(5);
        assertDoesNotThrow(() -> responseBuffer.putBack(new byte[]{0, 0, 0, 3, 1, 1, 2, 3}),
                "Adding non null byte array does not throw.");

        assertTrue(responseBuffer.hasResponse(),
                "Byte array is big enough and the buffer has a response ready.");
        assertArrayEquals(new byte[]{1, 1, 2, 3}, responseBuffer.peekNext(),
                "The response is correct.");
        assertArrayEquals(new byte[]{1, 1, 2, 3}, responseBuffer.getNext(),
                "The response is correct.");
        assertTrue(responseBuffer.isEmpty(),
                "Buffer is empty after response is extracted.");
        assertFalse(responseBuffer.hasResponse(),
                "Response buffer has no response when empty.");
    }

    @Test
    void testPutFrontByteBufferNull() {
        ResponseBuffer responseBuffer = new ResponseBuffer(5);
        assertThrows(IllegalArgumentException.class, () -> responseBuffer.putFront((ByteBuffer) null),
                "PutFront null nyte buffer throws exception.");
        assertTrue(responseBuffer.isEmpty(),
                "Buffer stays empty.");
        assertFalse(responseBuffer.hasResponse(),
                "empty buffer has no response.");
    }

    @Test
    void testPutFrontByteBufferSuccessful() {
        ResponseBuffer responseBuffer = new ResponseBuffer(5);
        assertDoesNotThrow(() -> responseBuffer.putFront(ByteBuffer.wrap(new byte[]{0, 0, 0, 3, 1, 1, 2, 3})),
                "Put front of non null byte buffer does not throw.");

        assertTrue(responseBuffer.hasResponse(),
                "Buffer has a response with big enough buffer added.");
        assertArrayEquals(new byte[]{1, 1, 2, 3}, responseBuffer.peekNext(),
                "Has correct bytes inside.");
        assertArrayEquals(new byte[]{1, 1, 2, 3}, responseBuffer.getNext(),
                "Has correct bytes inside.");
        assertTrue(responseBuffer.isEmpty(),
                "Buffer is empty after bytes are extracted.");
        assertFalse(responseBuffer.hasResponse(),
                "Empty buffer has no response.");
    }

    @Test
    void testPutFrontByteArrayNull() {
        ResponseBuffer responseBuffer = new ResponseBuffer(5);
        assertThrows(IllegalArgumentException.class, () -> responseBuffer.putFront((byte[]) null),
                "PutFront throws exception for null byte array.");
        assertTrue(responseBuffer.isEmpty(),
                "Buffer stays empty.");
        assertFalse(responseBuffer.hasResponse(),
                "empty buffer has no response.");
    }

    @Test
    void testPutFrontByteArraySuccessful() {
        ResponseBuffer responseBuffer = new ResponseBuffer(5);
        assertDoesNotThrow(() -> responseBuffer.putFront(new byte[]{0, 0, 0, 3, 1, 1, 2, 3}),
                "Put front does not throw when non null byte array is added.");

        assertTrue(responseBuffer.hasResponse(),
                "Buffer has response when byte array is big enough.");
        assertArrayEquals(new byte[]{1, 1, 2, 3}, responseBuffer.peekNext(),
                "The response bytes are correct.");
        assertArrayEquals(new byte[]{1, 1, 2, 3}, responseBuffer.getNext(),
                "The response bytes are correct.");
        assertTrue(responseBuffer.isEmpty(),
                "Buffer is empty after bytes are extracted.");
        assertFalse(responseBuffer.hasResponse(),
                "Empty buffer has no response.");
    }

    @Test
    void testGetNextNotEnoughBytes() {
        ResponseBuffer responseBuffer = new ResponseBuffer(5);
        assertDoesNotThrow(() -> responseBuffer.putFront(new byte[]{0, 0, 0, 3, 1, 1, 2}),
                "Adding partial non null buffer does not throw.");

        assertNull(responseBuffer.peekNext(),
                "Not all bytes have arrived cannot get response yet.");
        assertNull(responseBuffer.getNext(),
                "Not all bytes have arrived cannot get response yet.");

        ResponseBuffer responseBuffer2 = new ResponseBuffer(1);
        assertDoesNotThrow(() -> responseBuffer2.putFront(new byte[]{0, 0, 0, 3}),
                "Adding partial non null buffer does not throw.");

        assertNull(responseBuffer2.peekNext(),
                "Not all bytes have arrived cannot get response yet.");
        assertNull(responseBuffer2.getNext(),
                "Not all bytes have arrived cannot get response yet.");
        assertFalse(responseBuffer2.isEmpty(),
                "Buffer is not empty.");
    }

    @Test
    void testGetNextCodeNoResponseAvailable() {
        ResponseBuffer responseBuffer = new ResponseBuffer(1);
        assertDoesNotThrow(() -> responseBuffer.putFront(new byte[]{0, 0, 0, 3}),
                "Adding not big enough non null byte array does not throw.");
        assertEquals(-1,  responseBuffer.getNextCode(),
                "Code is not yet available.");
    }

    @Test
    void testGetNextCodeResponseAvailable() {
        ResponseBuffer responseBuffer = new ResponseBuffer(8);
        assertDoesNotThrow(() -> responseBuffer.putFront(new byte[]{0, 0, 0, 3, 1, 1, 2, 3}),
                "Adding big enough non null byte array does not throw.");
        assertEquals(1,  responseBuffer.getNextCode(),
                "Code is returned correctly.");
    }

    @Test
    void testSkipNextResponseAvailable() {
        ResponseBuffer responseBuffer = new ResponseBuffer(8);
        assertDoesNotThrow(() -> responseBuffer.putFront(new byte[]{0, 0, 0, 3, 1, 1, 2, 3}),
                "Adding big enough non null byte array does not throw.");
        assertTrue(responseBuffer.skipNext(),
                "Skip next returns true if the response is kipped correctly.");
    }

    @Test
    void testSkipNextNoResponseAvailable() {
        ResponseBuffer responseBuffer = new ResponseBuffer(1);
        assertDoesNotThrow(() -> responseBuffer.putFront(new byte[]{0, 0, 0, 3}),
                "Adding not big enough non null byte array does not throw.");
        assertFalse(responseBuffer.skipNext(),
                "Response cannot be skipped until it has all arrived.");
    }

    @Test
    void testClear() {
        ResponseBuffer responseBuffer = new ResponseBuffer(1);
        assertDoesNotThrow(() -> responseBuffer.putFront(new byte[]{0, 0, 0, 3}),
                "Adding non null buffer does not throw.");
        assertDoesNotThrow(responseBuffer::clear,
                "Clear does not throw.");
        assertTrue(responseBuffer.isEmpty(),
                "Buffer is empty after clear.");
    }
}