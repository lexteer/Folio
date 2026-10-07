package lex.folio.project;

import lex.folio.assets.AssetFolderScanner;
import lex.folio.model.CollisionTag;
import lex.folio.model.CollisionTags;
import lex.folio.model.Project;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/** Creates projects on disk and opens them again. A project is a folder holding a marker file and its assets. */
public final class ProjectStorage {
    public static final String FILE_NAME = "folio.project";
    private static final String PIXELS_PER_METER_KEY = "pixelsPerMeter";
    private static final String OPEN_ROOMS_KEY = "openRooms";
    /** Room names cannot contain this, see the rename checks. */
    private static final String OPEN_ROOMS_SEPARATOR = "|";
    private static final String OPEN_ROOMS_SEPARATOR_REGEX = "\\|";
    private static final String COLLISION_TAGS_KEY = "collisionTags";
    /** Between the tags, and between the name and the color of a tag. Tag names cannot contain either. */
    private static final String TAG_SEPARATOR = "|";
    private static final String TAG_SEPARATOR_REGEX = "\\|";
    private static final String TAG_COLOR_SEPARATOR = "=";
    public static final float DEFAULT_PIXELS_PER_METER = 100f;

    private ProjectStorage() {
    }

    /** Creates a new project in a new folder called {@code name} inside {@code parentFolder}. */
    public static Project create(Path parentFolder, String name, float pixelsPerMeter) throws IOException {
        Path folder = parentFolder.resolve(name);
        if (Files.exists(folder)) {
            throw new IOException("\"" + name + "\" already exists in " + parentFolder + ".");
        }

        Project project;
        try {
            project = new Project(folder, pixelsPerMeter);
        } catch (IllegalArgumentException e) {
            throw new IOException(e.getMessage());
        }
        Files.createDirectories(project.getAssetsFolder());
        write(folder.resolve(FILE_NAME), project);
        return project;
    }

    public static Project open(Path folder) throws IOException {
        Path file = folder.resolve(FILE_NAME);
        if (!Files.isRegularFile(file)) {
            throw new IOException("This folder is not a Folio project (no " + FILE_NAME + " found).");
        }

        Project project = new Project(folder, read(file));
        AssetFolderScanner.addAssetsTo(project);
        project.getCollisionTags().load(readCollisionTags(file));
        return project;
    }

    /**
     * The names of the rooms that were open when the project was last used, or null if that was never stored.
     * A project without that setting has all of its rooms open.
     */
    public static List<String> readOpenRooms(Path folder) throws IOException {
        String value = readProperties(folder.resolve(FILE_NAME)).getProperty(OPEN_ROOMS_KEY);
        if (value == null) return null;
        return value.isEmpty() ? List.of() : List.of(value.split(OPEN_ROOMS_SEPARATOR_REGEX));
    }

    /** Remembers which rooms are open, keeping the other settings of the project file. */
    public static void writeOpenRooms(Path folder, List<String> roomNames) throws IOException {
        Path file = folder.resolve(FILE_NAME);
        Properties properties = readProperties(file);
        properties.setProperty(OPEN_ROOMS_KEY, String.join(OPEN_ROOMS_SEPARATOR, roomNames));
        try (Writer writer = Files.newBufferedWriter(file)) {
            properties.store(writer, "Folio project");
        }
    }

    /** Remembers the collision tags, keeping the other settings of the project file. */
    public static void writeCollisionTags(Path folder, CollisionTags tags) throws IOException {
        Path file = folder.resolve(FILE_NAME);
        Properties properties = readProperties(file);
        List<String> entries = new ArrayList<>();
        for (CollisionTag tag : tags.getAll()) {
            entries.add(tag.name() + TAG_COLOR_SEPARATOR + String.format("%06X", tag.rgb()));
        }
        properties.setProperty(COLLISION_TAGS_KEY, String.join(TAG_SEPARATOR, entries));
        try (Writer writer = Files.newBufferedWriter(file)) {
            properties.store(writer, "Folio project");
        }
    }

    /** The stored tags. Entries that cannot be understood are left out. */
    private static List<CollisionTag> readCollisionTags(Path file) throws IOException {
        String value = readProperties(file).getProperty(COLLISION_TAGS_KEY);
        List<CollisionTag> tags = new ArrayList<>();
        if (value == null || value.isEmpty()) return tags;

        for (String entry : value.split(TAG_SEPARATOR_REGEX)) {
            int split = entry.lastIndexOf(TAG_COLOR_SEPARATOR);
            if (split <= 0) continue;
            try {
                int rgb = Integer.parseInt(entry.substring(split + 1).trim(), 16) & 0xFFFFFF;
                tags.add(new CollisionTag(entry.substring(0, split), rgb));
            } catch (NumberFormatException e) {
                // A damaged entry is not worth failing to open the project for.
            }
        }
        return tags;
    }

    private static Properties readProperties(Path file) throws IOException {
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(file)) {
            properties.load(reader);
        }
        return properties;
    }

    private static void write(Path file, Project project) throws IOException {
        Properties properties = new Properties();
        properties.setProperty(PIXELS_PER_METER_KEY, Float.toString(project.getPixelsPerMeter()));
        try (Writer writer = Files.newBufferedWriter(file)) {
            properties.store(writer, "Folio project");
        }
    }

    private static float read(Path file) throws IOException {
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(file)) {
            properties.load(reader);
        }

        String value = properties.getProperty(PIXELS_PER_METER_KEY);
        if (value == null) return DEFAULT_PIXELS_PER_METER;

        try {
            float pixelsPerMeter = Float.parseFloat(value.trim());
            if (pixelsPerMeter <= 0 || Float.isNaN(pixelsPerMeter)) throw new NumberFormatException(value);
            return pixelsPerMeter;
        } catch (NumberFormatException e) {
            throw new IOException("Invalid " + PIXELS_PER_METER_KEY + " in " + FILE_NAME + ": " + value);
        }
    }
}
