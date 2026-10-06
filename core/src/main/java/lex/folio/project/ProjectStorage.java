package lex.folio.project;

import lex.folio.assets.AssetFolderScanner;
import lex.folio.model.Project;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
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
