package lex.folio.scene.tool;

import lex.folio.model.Room;

/** A tool the user can pick in the scene toolbar. Input arrives as world positions. */
public interface SceneTool {
    /** Additive means the user holds shift or ctrl, to add to what is selected instead of replacing it. */
    void press(Room room, float worldX, float worldY, boolean additive);

    default boolean isDragging() {
        return false;
    }

    default void drag(float worldX, float worldY) {
    }

    default void release() {
    }

    /** Abandons the interaction in progress, undoing any preview of it. */
    default void cancel() {
    }
}
