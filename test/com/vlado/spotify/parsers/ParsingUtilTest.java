package com.vlado.spotify.parsers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ParsingUtilTest {

    @Test
    void testGetFirstWordNull() {
        assertThrows(IllegalArgumentException.class, () -> ParsingUtil.getFirstWord(null),
                "Parsing null input throws exception.");
    }

    @Test
    void testGetFirstWordEmpty() {
        assertEquals("", (ParsingUtil.getFirstWord("")),
                "Parsing empty input throws exception.");
    }

    @Test
    void testGetFirstWordBlank() {
        assertEquals("", (ParsingUtil.getFirstWord("   ")),
                "Parsing blank input throws exception.");
    }

    @Test
    void testGetFirstWordOneWord() {
        assertEquals("OneWord", (ParsingUtil.getFirstWord("OneWord")),
                "Get first word with single word returns the whole word.");
    }

    @Test
    void testGetFirstWordMultipleWords() {
        assertEquals("One", (ParsingUtil.getFirstWord("One Word")),
                "Get first word returns correct word from multiple words.");
    }

    @Test
    void testMultipleWordArgsSplitNull() {
        assertThrows(IllegalArgumentException.class, () -> ParsingUtil.multipleWordArgsSplit(null),
                "Multiple word arguments parsing throws exception for null input.");
    }

    @Test
    void testMultipleWordArgsSplitEmpty() {
        assertArrayEquals(new String[]{}, ParsingUtil.multipleWordArgsSplit(""),
                "Multiple word arguments parsing throws exception for empty input.");
    }

    @Test
    void testMultipleWordArgsSplitBlank() {
        assertArrayEquals(new String[]{}, ParsingUtil.multipleWordArgsSplit("    "),
                "Multiple word arguments parsing throws exception for blank input.");
    }

    @Test
    void testMultipleWordArgsSplitNoSpaces() {
        assertArrayEquals(new String[]{"two", "words", "!!!"}, ParsingUtil.multipleWordArgsSplit("two words     !!!"),
                "Multiple word arguments parsing returns correct when quotes are not used.");
    }

    @Test
    void testMultipleWordArgsSplitWithSpaces() {
        assertArrayEquals(new String[]{"two", "words", " ! ! ! "}, ParsingUtil.multipleWordArgsSplit("two words     \" ! ! ! \""),
                "Multiple word arguments parsing returns correct when there is an argument in quotes.");
    }
}