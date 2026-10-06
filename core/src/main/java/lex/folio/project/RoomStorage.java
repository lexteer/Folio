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
            try {
                rooms.add(RoomJson.read(Files.readString(file, StandardCharsets.UTF_8)));
            } catch (IOException e) {
                throw new IOException("Could not read room " + file.getFileName() + ": " + e.getMessage(), e);
            }
        }
        return rooms;
    }

    /** Writes the room to its file, replacing what was saved before. Returns what was written. */
    public String save(Room room) throws IOException {
        String text = snapshot(room);
        Files.createDirectories(folder);

        Path file = fileOf(room.getName());
        Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
        Files.writeString(temporary, text, StandardCharsets.UTF_8);
        Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
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
