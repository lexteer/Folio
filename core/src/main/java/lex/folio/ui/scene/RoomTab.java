package lex.folio.ui.scene;

import lex.folio.model.Room;
import lex.folio.scene.camera.SceneCamera;

/** A room shown as a tab of the scene panel, which remembers where its own camera was left. */
final class RoomTab {
    private static int nextId = 1;

    private final Room room;
    private final SceneCamera camera;
    private final int id = nextId++;

    RoomTab(Room room, SceneCamera camera) {
        this.room = room;
        this.camera = camera;
    }

    Room getRoom() {
        return room;
    }

    SceneCamera getCamera() {
        return camera;
    }

    /** The label ImGui shows for the tab. The part after ### is the identity, so renaming a room keeps the tab. */
    String getLabel() {
        return room.getName() + "###room" + id;
    }
}
