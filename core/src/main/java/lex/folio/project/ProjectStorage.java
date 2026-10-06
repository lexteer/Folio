package lex.folio.project;

import lex.folio.assets.AssetFolderScanner;
import lex.folio.model.Project;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Creates projects on disk and opens them again. A project is a folder holding a marker file and its assets. */
public final class ProjectStorage {
    public static final String FILE_NAME = "folio.project";
    private static final String PIXELS_PER_METER_KEY = "pixelsPerMeter";
    private static final float DEFAULT_PIXELS_PER_METER = 100f;

    private ProjectStorage() {
    }

    /** Turns {@code folder} (created if missing) into a new, empty project. */
    public static Project create(Path folder) throws IOException {
        Path file = folder.resolve(FILE_NAME);
        if (Files.exists(file)) {
            throw new IOException("This folder already contains a project. Use Open Project instead.");
        }

        Project project = new Project(folder, DEFAULT_PIXELS_PER_METER);
        Files.createDirectories(project.getAssetsFolder());
        write(file, project);

        AssetFolderScanner.addAssetsTo(project);
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
