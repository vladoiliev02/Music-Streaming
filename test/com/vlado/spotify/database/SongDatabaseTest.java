package com.vlado.spotify.database;

import com.vlado.spotify.exceptions.SongNotFoundException;
import com.vlado.spotify.song.Song;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SongDatabaseTest {
    private static final String SONG_NAME = "s name";
    private static final String ARTIST_NAME = "a name";
    private static final String ARTIST_2_NAME = "a2 name";
    private static final Path SOME_PATH = Path.of("somePath");
    private static final SongDatabase songDatabase = SongDatabase.instance();

    @Test
    void testAddNullSong() {
        assertThrows(IllegalArgumentException.class, () -> songDatabase.addSong(null));
    }

    @Test
    @Order(1)
    void testAddSongNotExisting() {
        Song song = new Song(SONG_NAME, ARTIST_NAME, 0, SOME_PATH);
        assertDoesNotThrow(() -> songDatabase.addSong(song));

        assertDoesNotThrow(() -> songDatabase.getSong(SONG_NAME),
                "Adding not existent song should not throw.");
        assertDoesNotThrow(() -> songDatabase.getSong("s Name"),
                "Searching should be case insensitive");
        assertDoesNotThrow(() -> songDatabase.getSong("S NaMe"),
                "Searching should be case insensitive");

        assertThrows(SongNotFoundException.class, () -> songDatabase.getSong(ARTIST_NAME),
                "Cannot find song just by artist.");

        assertEquals(0, songDatabase.getSong(SONG_NAME).streams(),
                "Song streams are correct.");
    }

    @Test
    @Order(2)
    void testAddSongReplaceExisting() {
        Song song = new Song(SONG_NAME, ARTIST_NAME, 100, SOME_PATH);
        assertDoesNotThrow(() -> songDatabase.addSong(song),
                "Adding song with same name and artist as existent should replace the old song.");

        assertDoesNotThrow(() -> songDatabase.getSong(SONG_NAME),
                "Finding the just added song should not throw.");

        assertThrows(SongNotFoundException.class, () -> songDatabase.getSong(ARTIST_NAME),
                "Cannot find song just by artist.");

        assertEquals(100, songDatabase.getSong(SONG_NAME).streams(),
                "Song streams are correct.");
    }

    @Test
    void testGetSongByNameNull() {
        assertThrows(IllegalArgumentException.class, () -> songDatabase.getSong(null),
                "Cannot find song if name is null.");
    }

    @Test
    @Order(3)
    void testGetSongByNameSuccessful() {
        Song song = new Song(SONG_NAME, ARTIST_NAME, 100, SOME_PATH);
        assertEquals(song, songDatabase.getSong(SONG_NAME),
                "New song replaces the old one successfully.");
        assertEquals(song, songDatabase.getSong("S Name"),
                "Searching for a song should be case insensitive.");
        assertEquals(song, songDatabase.getSong("s nAme"),
                "Searching for a song should be case insensitive.");
        assertEquals(song, songDatabase.getSong("s NamE"),
                "Searching for a song should be case insensitive.");
    }

    @Test
    void testGetSongByNameSongNotFound() {
        assertThrows(SongNotFoundException.class, () -> songDatabase.getSong("No Such Song"),
                "Cannot find a nonexistent song.");
    }

    @Test
    void testGetSongByNullNameArtist() {
        assertThrows(IllegalArgumentException.class, () -> songDatabase.getSong(null, ARTIST_NAME),
                "Searching for a song with arist when name is null throws exception.");
    }

    @Test
    void testGetSongByNameNullArtist() {
        assertThrows(IllegalArgumentException.class, () -> songDatabase.getSong(SONG_NAME, null),
                "Searching for a song with arist when artist is null throws exception.");
    }

    @Test
    @Order(4)
    void testGetSongByNameArtistSuccessful() {
        Song song = new Song(SONG_NAME, ARTIST_NAME, 100, SOME_PATH);
        assertEquals(song, songDatabase.getSong(SONG_NAME, "a Name"),
                "Replacing old song should not throw.");
        assertEquals(song, songDatabase.getSong("S Name", "a NAme"),
                "Searching for a song by name and artist should be case insensitive.");
        assertEquals(song, songDatabase.getSong("s nAme", "A NAme"),
                "Searching for a song by name and artist should be case insensitive.");
        assertEquals(song, songDatabase.getSong("s NamE", "a NAmE"),
                "Searching for a song by name and artist should be case insensitive.");
    }

    @Test
    void testGetSongByNameArtistSongNotFound() {
        assertThrows(SongNotFoundException.class,
                () -> songDatabase.getSong("No Such Song", "No Such Artist"),
                "Searching for nonexistent song throws exception.");
    }

    @Test
    @Order(5)
    void testGetSongByNameArtistSameNameSongsDifferentArtist() {
        Song song = new Song(SONG_NAME, ARTIST_NAME, 100, SOME_PATH);
        Song song2 = new Song(SONG_NAME, ARTIST_2_NAME, 5, SOME_PATH);
        songDatabase.addSong(song2);

        assertEquals(song, songDatabase.getSong(SONG_NAME, "a Name"),
                "The song by the correct artist is returned.");
        assertEquals(song, songDatabase.getSong("S Name", "a NAme"),
                "The song by the correct artist is returned.");
        assertEquals(song2, songDatabase.getSong("s nAme", "A2 NAme"),
                "The song by the correct artist is returned.");
        assertEquals(song2, songDatabase.getSong("s NamE", "a2 NAmE"),
                "The song by the correct artist is returned.");
    }

    @Test
    @Order(6)
    void testGetSongByNameArtistNonexistentArtist() {
        assertThrows(SongNotFoundException.class,
                () -> songDatabase.getSong(SONG_NAME, "Nonexistent Artist"),
                "Searching for an existing songName by nonexistent artist should throw exception.");
    }

    @Test
    void testRemoveSongNullName() {
        assertThrows(IllegalArgumentException.class, () -> songDatabase.removeSong(null, ARTIST_NAME),
                "Remove song with null name throws exception.");
    }

    @Test
    void testRemoveSongNullArtist() {
        assertThrows(IllegalArgumentException.class, () -> songDatabase.removeSong(SONG_NAME, null),
                "Remove song with null artist name throws exception.");
    }

    @Test
    void testRemoveSongNonexistent() {
        assertThrows(SongNotFoundException.class, () -> songDatabase.removeSong("Nonexistent", ARTIST_NAME),
                "Try to remove nonexistent song throws exception.");
    }

    @Test
    @Order(7)
    void testRemoveSongNonexistentArtist() {
        assertThrows(SongNotFoundException.class, () -> songDatabase.removeSong(SONG_NAME, "Nonexistent"),
                "Removing song by nonexistent artist throws exception.");
    }

    @Test
    @Order(8)
    void testRemoveSongSuccessful() {
        assertNotNull(songDatabase.getSong(SONG_NAME, ARTIST_NAME),
                "Song should be found.");
        assertDoesNotThrow(() -> songDatabase.removeSong(SONG_NAME, ARTIST_NAME),
                "Song is removed without an exception being thrown.");
        assertThrows(SongNotFoundException.class, () -> songDatabase.getSong(SONG_NAME, ARTIST_NAME),
                "Song is no longer found in the database.");
    }

    @Test
    void testUpdateSongNullName() {
        assertThrows(IllegalArgumentException.class, () -> songDatabase.updateSong(null, ARTIST_NAME),
                "Update song with null name throws exception.");
    }

    @Test
    void testUpdateSongNullArtist() {
        assertThrows(IllegalArgumentException.class, () -> songDatabase.updateSong(SONG_NAME, null),
                "Update song with null artist name throws exception.");
    }

    @Test
    void testUpdateSongNonexistent() {
        assertThrows(SongNotFoundException.class, () -> songDatabase.updateSong("Nonexistent", ARTIST_NAME),
                "Updating a nonexistent song throws exception.");
    }

    @Test
    @Order(9)
    void testUpdateSongNonexistentArtist() {
        assertThrows(SongNotFoundException.class, () -> songDatabase.updateSong(SONG_NAME, "nonexistent"),
                "Updating a song by a nonexistent artist throws exception.");
    }

    @Test
    @Order(10)
    void testUpdateSongSuccessful() {
        assertDoesNotThrow(() -> songDatabase.addSong(new Song(SONG_NAME, ARTIST_NAME, 5, SOME_PATH)),
                "Adding a correct song does no throw.");
        assertNotNull(songDatabase.getSong(SONG_NAME, ARTIST_NAME),
                "New song is found correctly.");
        assertEquals(5, songDatabase.getSong(SONG_NAME, ARTIST_NAME).streams(),
                "The song has 5 streams.");
        assertDoesNotThrow(() -> songDatabase.updateSong(SONG_NAME, ARTIST_NAME),
                "The song is updated correctly.");
        assertEquals(6, songDatabase.getSong(SONG_NAME, ARTIST_NAME).streams(),
                "The song has 6 streams after the update.");
        assertNotNull(songDatabase.getSong(SONG_NAME, ARTIST_NAME),
                "The song is still found in the database.");
    }

    @Test
    void testGetTopNStreamedSongsNegativeParameter() {
        assertThrows(IllegalArgumentException.class, () -> songDatabase.getTopNStreamedSongs(-1),
                "Get top N throws for negative n.");
        assertThrows(IllegalArgumentException.class, () -> songDatabase.getTopNStreamedSongs(-2),
                "Get top N throws for negative n.");
        assertThrows(IllegalArgumentException.class, () -> songDatabase.getTopNStreamedSongs(-5),
                "Get top N throws for negative n.");
    }

    @Test
    void testGetTopNStreamedSongsZeroParameter() {
        assertThrows(IllegalArgumentException.class, () -> songDatabase.getTopNStreamedSongs(0),
                "Get top N throws for 0 n.");
    }

    @Test
    @Order(11)
    void testGetTopNStreamedSongsSuccessful() {
        Song top = songDatabase.getSong(SONG_NAME, ARTIST_NAME);
        Song second = songDatabase.getSong(SONG_NAME, ARTIST_2_NAME);
        assertIterableEquals(List.of(top), songDatabase.getTopNStreamedSongs(1),
                "The top song is found successfully.");
        assertIterableEquals(List.of(top, second), songDatabase.getTopNStreamedSongs(2),
                "The top two songs are found successfully.");
        assertIterableEquals(List.of(top, second), songDatabase.getTopNStreamedSongs(3),
                "The only two songs are returned in correct order when asking for 3 songs.");
        assertIterableEquals(List.of(top, second), songDatabase.getTopNStreamedSongs(5),
                "The only two songs are returned in correct order when asking for 5 songs.");
    }

    @Test
    void testSearchKeyWordsNull() {
        assertThrows(IllegalArgumentException.class, () -> songDatabase.search((String[]) null),
                "Search keyWords cannot be null.");
    }

    @Test
    void testSearchNullKeyWord() {
        assertThrows(IllegalArgumentException.class, () -> songDatabase.search(" ", null, "asda"),
                "Search keyWords cannot contain a null word.");
    }

    @Test
    void testSearchSuccessfulSongName() {
        Song s1 = songDatabase.getSong(SONG_NAME, ARTIST_NAME);
        Song s2 = songDatabase.getSong(SONG_NAME, ARTIST_2_NAME);

        List<Song> expected = List.of(s1, s2);
        List<Song> actual = assertDoesNotThrow(() -> songDatabase.search(SONG_NAME));
        assertTrue(expected.containsAll(actual),
                "Correct songs a returned when searching by name.");
        assertTrue(actual.containsAll(expected),
                "Correct songs a returned when searching by name.");
    }

    @Test
    void testSearchSuccessfulArtistName() {
        Song s1 = songDatabase.getSong(SONG_NAME, ARTIST_NAME);
        Song s2 = songDatabase.getSong(SONG_NAME, ARTIST_2_NAME);

        List<Song> expected = List.of(s1);
        List<Song> actual = assertDoesNotThrow(() -> songDatabase.search(ARTIST_NAME));
        assertTrue(expected.containsAll(actual),
                "Correct songs a returned when searching by artist.");
        assertTrue(actual.containsAll(expected),
                "Correct songs a returned when searching by artist.");
    }
}