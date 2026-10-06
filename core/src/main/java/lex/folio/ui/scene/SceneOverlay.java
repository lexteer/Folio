package lex.folio.ui.scene;

import lex.folio.model.Room;
import lex.folio.scene.BoxSelect;
import lex.folio.scene.Selection;
import lex.folio.scene.sprite.SpriteGeometry;
import lex.folio.scene.tool.ToolState;

/** Everything drawn on top of the rendered scene image. */
public class SceneOverlay {
    private final PlaceholderOverlay placeholders;
    private final SelectionOverlay selectionOutlines;
    private final SceneToolbar toolbar;

    public SceneOverlay(SceneViewport viewport, SpriteGeometry geometry, Selection selection,
                        BoxSelect boxSelect, ToolState toolState) {
        SpriteOutlineDrawer outlines = new SpriteOutlineDrawer(viewport, geometry);
        this.placeholders = new PlaceholderOverlay(outlines, geometry);
        this.selectionOutlines = new SelectionOverlay(viewport, outlines, selection, boxSelect);
        this.toolbar = new SceneToolbar(viewport, toolState);
    }

    void draw(Room room) {
        placeholders.draw(room);
        selectionOutlines.draw();
        toolbar.draw();
    }

    /** Whether the mouse is over a widget of the overlay, which then gets the input instead of the scene. */
    boolean isHovered() {
        return toolbar.isHovered();
    }
}
