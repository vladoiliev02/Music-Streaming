package com.vlado.spotify.database;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.vlado.spotify.exceptions.PlayLIstCreationException;
import com.vlado.spotify.exceptions.SongNotAddedToPlaylistException;
import com.vlado.spotify.exceptions.SongNotFoundException;
import com.vlado.spotify.exceptions.UserErrorException;
import com.vlado.spotify.parsers.ParsingUtil;
import com.vlado.spotify.song.Song;
import com.vlado.spotify.validations.ParameterValidator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SongDatabase {
    private static final Path SONGS_FILE = Path.of("resources", "songs", "songs.txt");
    private static final Path PLAYLIST_FOLDER = Path.of("resources", "songs", "playlists");
    private static final String PLAYLIST_FORMAT = ".txt";

    private static final String SONG_NOT_FOUND = "Song: %s, not found.";
    private static final String PLAYLIST_NOT_FOUND = "The playlist: %s, was not found.";
    private static final String NAME_PARAM = "name";
    private static final String ARTIST_PARAM = "artist";
    private static final String SONG_PARAM = "song";
    private static final String PLAYLIST_NAME_PARAM = "playlistName";

    private static final SongDatabase INSTANCE = new SongDatabase();

    static final int SONG_NAME_INDEX = 0;
    static final int ARTIST_NAME_INDEX = 1;

    private final Gson gson;
    // Map name of a song to a map of songs with the same name by different artists
    private final Map<String, Map<String, Song>> songs;
    // Orders songs by streams
    private final SortedSet<Song> topSongs;
    // PlayLists
    private final Map<String, Set<Song>> playlists;

    private SongDatabase() {
        this.songs = new ConcurrentHashMap<>();
        this.topSongs = new ConcurrentSkipListSet<>((a, b) -> {
            int streamDiff;
            int nameDiff;

            return (streamDiff = b.streams() - a.streams()) != 0 ? streamDiff :
                    (nameDiff = b.name().compareTo(a.name())) != 0 ? nameDiff :
                            b.artist().compareTo(a.artist());
        });

        this.playlists = new HashMap<>();
        this.gson = new GsonBuilder()
                .registerTypeHierarchyAdapter(Path.class, new PathConverter())
                .create();
    }

    public static SongDatabase instance() {
        return INSTANCE;
    }

    public Song getSong(String name) {
        ParameterValidator.checkNull(name, NAME_PARAM);

        Map<String, Song> sameNameSongs = songs.getOrDefault(name.toLowerCase(), null);
        if (sameNameSongs == null || sameNameSongs.isEmpty()) {
            throw new SongNotFoundException(String.format(SONG_NOT_FOUND, name));
        }

        return sameNameSongs.values().iterator().next();
    }

    public Song getSong(String name, String artist) {
        ParameterValidator.checkNull(name, NAME_PARAM);
        ParameterValidator.checkNull(artist, ARTIST_PARAM);

        Map<String, Song> sameNameSongs = songs.getOrDefault(name.toLowerCase(), null);
        if (sameNameSongs == null) {
            throw new SongNotFoundException(String.format(SONG_NOT_FOUND, name + " by " + artist));
        }

        Song result = sameNameSongs.getOrDefault(artist.toLowerCase(), null);
        if (result == null) {
            throw new SongNotFoundException(String.format(SONG_NOT_FOUND, name + " by " + artist));
        }

        return result;
    }

    public void addSong(Song song) {
        ParameterValidator.checkNull(song, NAME_PARAM);

        Map<String, Song> newVal = new ConcurrentHashMap<>();
        newVal.put(song.artist().toLowerCase(), song);

        AtomicReference<Song> old = new AtomicReference<>();
        songs.merge(song.name().toLowerCase(), newVal, (oldValue, newValue) -> {
            old.set(oldValue.put(song.artist().toLowerCase(), song));
            return oldValue;
        });

        if (old.get() != null) {
            topSongs.remove(old.get());
        }

        topSongs.add(song);
    }

    public void removeSong(String name, String artist) {
        ParameterValidator.checkNull(name, NAME_PARAM);
        ParameterValidator.checkNull(artist, ARTIST_PARAM);

        Map<String, Song> sameNameSongs = songs.getOrDefault(name.toLowerCase(), null);

        if (sameNameSongs != null) {
            Song remove = sameNameSongs.get(artist.toLowerCase());

            if (remove != null) {
                topSongs.remove(remove);
                sameNameSongs.remove(artist.toLowerCase());
                return;
            }
        }

        throw new SongNotFoundException(String.format(SONG_NOT_FOUND, name));
    }

    public void updateSong(String name, String artist) {
        ParameterValidator.checkNull(name, NAME_PARAM);
        ParameterValidator.checkNull(artist, ARTIST_PARAM);

        Map<String, Song> sameNameSongs = songs.getOrDefault(name.toLowerCase(), null);

        if (sameNameSongs != null) {
            Song song = sameNameSongs.get(artist.toLowerCase());

            if (song != null) {
                topSongs.remove(song);
                song.incrementStreams();
                topSongs.add(song);
                return;
            }
        }

        throw new SongNotFoundException(String.format(SONG_NOT_FOUND, name));
    }

    public List<Song> getTopNStreamedSongs(int n) {
        ParameterValidator.checkPositive(n, "Top N songs");

        return topSongs.stream()
                .limit(n)
                .toList();
    }

    public List<Song> search(String... keyWords) {
        ParameterValidator.checkNull(keyWords, "keyWords");

        Stream<Song> stream = songs.values().stream()
                .flatMap(map -> map.values().stream());

        for (String keyWord : keyWords) {
            ParameterValidator.checkNull(keyWord, "one keyWord");
            stream = stream
                    .filter(song -> song.name().toLowerCase().contains(keyWord.toLowerCase()) ||
                            song.artist().toLowerCase().contains(keyWord.toLowerCase()));
        }

        return stream.toList();
    }

    public void createPlaylist(String playlistName) {
        ParameterValidator.checkNull(playlistName, PLAYLIST_NAME_PARAM);

        if (playlists.containsKey(playlistName)) {
            throw new UserErrorException(String.format("The playlist: %s, already exists.", playlistName));
        }

        createPlayListFile(playlistName);

        playlists.put(playlistName, new LinkedHashSet<>());
    }

    public void addSongToPlaylist(String playlistName, Song song) {
        ParameterValidator.checkNull(playlistName, PLAYLIST_NAME_PARAM);
        ParameterValidator.checkNull(song, SONG_PARAM);

        if (!playlists.containsKey(playlistName)) {
            throw new UserErrorException(String.format(PLAYLIST_NOT_FOUND, playlistName));
        }

        Set<Song> playlist = playlists.get(playlistName);

        if (playlist.contains(song)) {
            throw new UserErrorException(String.format("Song %s by %s is already in the playlist.",
                    song.name(), song.artist()));
        }

        try (var bufferedWriter = Files.newBufferedWriter(
                PLAYLIST_FOLDER.resolve(playlistName + PLAYLIST_FORMAT), StandardOpenOption.APPEND)) {
            bufferedWriter.write(String.format("\"%s\" \"%s\"%n", song.name(), song.artist()));
        } catch (IOException e) {
            throw new SongNotAddedToPlaylistException(String.format(
                    "Error while adding song to: %s. Please try again.", playlistName), e);
        }

        playlists.get(playlistName).add(song);
    }

    public List<Song> getPlaylist(String playlistName) {
        ParameterValidator.checkNull(playlistName, PLAYLIST_NAME_PARAM);

        if (!playlists.containsKey(playlistName)) {
            throw new UserErrorException(String.format(PLAYLIST_NOT_FOUND, playlistName));
        }

        return List.copyOf(playlists.get(playlistName));
    }

    public void loadSongs() {
        try (BufferedReader bufferedReader = Files.newBufferedReader(SONGS_FILE)) {
            List<Song> list = readSongs(bufferedReader);

            for (Song song : list) {
                addSong(song);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void loadPlaylists() {
        if (!Files.exists(PLAYLIST_FOLDER)) {
            return;
        }

        try {
            Files.walk(PLAYLIST_FOLDER)
                    .filter(path -> path.toString().endsWith(PLAYLIST_FORMAT))
                    .forEach(path -> {
                        try {
                            Set<Song> playlist = Files.lines(path)
                                    .map(line -> {
                                        String[] songData = ParsingUtil.multipleWordArgsSplit(line);
                                        return getSong(songData[SONG_NAME_INDEX], songData[ARTIST_NAME_INDEX]);
                                    })
                                    .collect(Collectors.toCollection(LinkedHashSet::new));

                            playlists.put(path.getFileName().toString()
                                    .replace(PLAYLIST_FORMAT, ""), playlist);
                        } catch (IOException e) {
                            throw new UncheckedIOException("Playlist reading error", e);
                        }
                    });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void saveSongs() {
        try {
            Path path = Files.createTempFile(SONGS_FILE.getParent(), "songs-copy", ".txt");
            Files.copy(SONGS_FILE, path, StandardCopyOption.REPLACE_EXISTING);

            try (var bufferedWriter = Files.newBufferedWriter(
                    SONGS_FILE, StandardOpenOption.TRUNCATE_EXISTING)) {
                for (Song song : topSongs) {
                    bufferedWriter.write(gson.toJson(song) + System.lineSeparator());
                }
            }

            Files.delete(path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private List<Song> readSongs(BufferedReader bufferedReader) {
        ParameterValidator.checkNull(bufferedReader, "bufferedReader");

        return bufferedReader.lines()
                .map(line -> {
                    Song song = gson.fromJson(line, Song.class);
                    if (!Files.exists(song.path()) || !song.path().getFileName().toString().endsWith(".wav")) {
                        throw new IllegalArgumentException(String.format(
                                "Song: %s by %s, path error. Check the path %s.",
                                song.name(), song.artist(), song.path().toString()));
                    }

                    return song;
                })
                .toList();
    }

    private void createPlayListFile(String playlistName) {
        ParameterValidator.checkNull(playlistName, "playlistName");
        ParameterValidator.checkEmpty(playlistName, "playlistName");
        ParameterValidator.checkBlank(playlistName, "playlistName");

        try {
            if (!Files.exists(PLAYLIST_FOLDER)) {
                Files.createDirectories(PLAYLIST_FOLDER);
            }

            Path playlistPath = PLAYLIST_FOLDER.resolve(playlistName + PLAYLIST_FORMAT);
            if (!Files.exists(playlistPath)) {
                Files.createFile(playlistPath);
            }
        } catch (IOException e) {
            throw new PlayLIstCreationException(String.format(
                    "Error while creating the playlist: %s. Please try again.", playlistName), e);
        }
    }

    static class PathConverter implements JsonDeserializer<Path>, JsonSerializer<Path> {
        @Override
        public Path deserialize(JsonElement jsonElement, Type type,
                                JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return Path.of(jsonElement.getAsString());
        }

        @Override
        public JsonElement serialize(Path path, Type type, JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive(path.toString());
        }
    }
}
