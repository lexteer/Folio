package lex.folio.app;

import lex.folio.assets.AssetFolderScanner;
import lex.folio.model.Project;
import lex.folio.model.Room;
import lex.folio.model.Sprite;
import lex.folio.model.SpriteLayer;

import java.nio.file.Path;

/** Temporary: a hardcoded project and room to edit. Goes away once projects are loaded from disk. */
final class TestProject {
    private static final Path FOLDER = Path.of(System.getProperty("user.home"), "FolioTestProject");
    private static final float PIXELS_PER_METER = 100f;

    private TestProject() {
    }

    static Project load() {
        Project project = new Project(FOLDER, PIXELS_PER_METER);
        AssetFolderScanner.addAssetsTo(project);
        return project;
    }

    static Room createRoom() {
        Room room = new Room("test");
        SpriteLayer layer = new SpriteLayer(room.createId(), "Art");

        layer.add(new Sprite(room.createId(), "tile008", 2, 1));

        Sprite rotated = new Sprite(room.createId(), "tile16", 4, 1);
        layer.add(rotated);

        Sprite scaled = new Sprite(room.createId(), "tile021", 6, 1);
        layer.add(scaled);

        Sprite flippedX = new Sprite(room.createId(), "tile000", 8, 1);
        layer.add(flippedX);

        Sprite flippedY = new Sprite(room.createId(), "flipY", 10, 1);
        layer.add(flippedY);

        rotated.setRotationDegrees(30);
        scaled.setScale(2f, 0.5f);
        flippedX.setFlipX(true);
        flippedY.setFlipY(true);

        room.addLayer(layer);
        return room;
    }
}
