package lex.folio.project;

import lex.folio.model.Project;
import lex.folio.model.Room;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/** Saves the rooms of a project as one file each in the project's rooms folder, and loads them again. */
public final class RoomStorage {
    private static final String EXTENSION = ".room.json";

    private final Path folder;

    public RoomStorage(Project project) {
        this.folder = project.getRoomsFolder();
    }

    /** Loads every saved room. Returns an empty list if nothing was saved yet. */
    public List<Room> loadAll() throws IOException {
        if (!Files.isDirectory(folder)) return List.of();

        List<Path> files;
        try (Stream<Path> children = Files.list(folder)) {
            files = children.filter(file -> file.getFileName().toString().endsWith(EXTENSION)).sorted().toList();
        }

        List<Room> rooms = new ArrayList<>();
        for (Path file : files) {
            rooms.add(load(file));
        }
        return rooms;
    }

    /** Points the sprites of every saved room that use the asset id at the new id, and saves those rooms again. */
    public void replaceAssetId(String oldId, String newId) throws IOException {
        if (!Files.isDirectory(folder)) return;

        List<Path> files;
        try (Stream<Path> children = Files.list(folder)) {
            files = children.filter(file -> file.getFileName().toString().endsWith(EXTENSION)).sorted().toList();
        }
        for (Path file : files) {
            Room room = load(file);
            if (room.replaceAssetId(oldId, newId)) save(room, null);
        }
    }

    /** Gives the shapes of every saved room that have the tag another one, and saves those rooms again. */
    public void replaceTag(String oldName, String newName) throws IOException {
        if (!Files.isDirectory(folder)) return;

        List<Path> files;
        try (Stream<Path> children = Files.list(folder)) {
            files = children.filter(file -> file.getFileName().toString().endsWith(EXTENSION)).sorted().toList();
        }
        for (Path file : files) {
            Room room = load(file);
            if (room.replaceTag(oldName, newName)) save(room, null);
        }
    }

    public Path getFolder() {
        return folder;
    }

    /** Loads one room file. */
    public Room load(Path file) throws IOException {
        try {
            return RoomJson.read(Files.readString(file, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IOException("Could not read room " + file.getFileName() + ": " + e.getMessage(), e);
        }
    }

    /** Whether the file is one of the rooms of this project. */
    public boolean contains(Path file) throws IOException {
        Path parent = file.toAbsolutePath().getParent();
        return parent != null && Files.isDirectory(folder) && Files.isSameFile(parent, folder);
    }

    /** The folder of the project that has this room file in its rooms folder, or null if there is none. */
    public static Path findProjectOf(Path roomFile) {
        Path roomsFolder = roomFile.toAbsolutePath().getParent();
        Path projectFolder = roomsFolder == null ? null : roomsFolder.getParent();
        if (projectFolder == null || !Files.isRegularFile(projectFolder.resolve(ProjectStorage.FILE_NAME))) return null;
        return projectFolder;
    }

    /**
     * Writes the room to its file, replacing what was saved before. Returns what was written.
     *
     * @param previousName the name the room was last saved under, or null; its file goes away if the name changed
     */
    public String save(Room room, String previousName) throws IOException {
        String text = snapshot(room);
        Files.createDirectories(folder);

        Path file = fileOf(room.getName());
        Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
        Files.writeString(temporary, text, StandardCharsets.UTF_8);
        Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);

        if (previousName != null && !previousName.equals(room.getName())) {
            Path old = fileOf(previousName);
            // On a case-insensitive file system a renamed-by-case room has the same file.
            if (Files.exists(old) && !Files.isSameFile(old, file)) Files.delete(old);
        }
        return text;
    }

    /** The text {@link #save} would write now, for telling whether a room changed since it was saved. */
    public String snapshot(Room room) {
        return RoomJson.write(room);
    }

    public boolean exists(String roomName) {
        return Files.exists(fileOf(roomName));
    }

    private Path fileOf(String roomName) {
        return folder.resolve(roomName.replaceAll("[\\\\/:*?\"<>|]", "_") + EXTENSION);
    }
}
