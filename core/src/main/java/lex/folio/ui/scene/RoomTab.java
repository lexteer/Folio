package lex.folio.ui.scene;

import lex.folio.model.Room;
import lex.folio.scene.camera.SceneCamera;

/** A room shown as a tab of the scene panel, which remembers where its own camera was left. */
final class RoomTab {
    private static int nextId = 1;

    private final Room room;
    private final SceneCamera camera;
    private final int id = nextId++;
    /** What the room looked like when it was last saved or loaded; null while it was never saved. */
    private String savedSnapshot;
    private boolean unsaved = true;
    /** The name the room had when it was saved, which names its file; null while it was never saved. */
    private String savedName;

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

    boolean isUnsaved() {
        return unsaved;
    }

    String getSavedName() {
        return savedName;
    }

    void markSaved(String snapshot) {
        savedName = room.getName();
        savedSnapshot = snapshot;
        unsaved = false;
    }

    /** Compares against the saved state, so undoing back to it makes the tab clean again. */
    void refreshUnsaved(String currentSnapshot) {
        unsaved = !currentSnapshot.equals(savedSnapshot);
    }

    /**
     * The label ImGui shows for the tab. The part after ### is the identity, so renaming a room keeps the tab.
     *
     * @param padding trailing space that keeps the name clear of the close button
     */
    String getLabel(String padding) {
        return room.getName() + padding + "###room" + id;
    }
}
