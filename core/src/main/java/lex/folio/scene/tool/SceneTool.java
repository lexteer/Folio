package lex.folio.scene.tool;

import lex.folio.model.Room;

/** A tool the user can pick in the scene toolbar. Input arrives as world positions. */
public interface SceneTool {
    /** Additive means the user holds shift or ctrl, to add to what is selected instead of replacing it. */
    void press(Room room, float worldX, float worldY, boolean additive);

    default boolean isDragging() {
        return false;
    }

    /** Whether the tool is part way through something that takes several clicks, such as a polygon. */
    default boolean isBuilding() {
        return false;
    }

    /** The mouse is over the scene, with no button involved. */
    default void hover(float worldX, float worldY) {
    }

    default void doubleClick(Room room, float worldX, float worldY) {
    }

    /** Completes what {@link #isBuilding} says is in progress, if it can be completed. */
    default void finish() {
    }

    default void drag(float worldX, float worldY) {
    }

    default void release() {
    }

    /** Abandons the interaction in progress, whether {@link #isDragging} or {@link #isBuilding}. */
    default void cancel() {
    }
}
