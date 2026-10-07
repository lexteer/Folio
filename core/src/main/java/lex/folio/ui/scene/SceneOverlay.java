package lex.folio.ui.scene;

import lex.folio.model.CollisionTags;
import lex.folio.model.Room;
import lex.folio.scene.BoxSelect;
import lex.folio.scene.Selection;
import lex.folio.scene.shape.ShapeDraft;
import lex.folio.scene.shape.ShapePlacer;
import lex.folio.scene.sprite.SpriteGeometry;
import lex.folio.scene.sprite.SpritePlacer;
import lex.folio.scene.tool.ToolState;

/** Everything drawn on top of the rendered scene image. */
public class SceneOverlay {
    private final PlaceholderOverlay placeholders;
    private final CollisionOverlay collision;
    private final SelectionOverlay selectionOutlines;
    private final DraftOverlay draft;
    private final SceneToolbar toolbar;
    private final ToolHintOverlay hint;

    public SceneOverlay(SceneViewport viewport, SpriteGeometry geometry, Selection selection, BoxSelect boxSelect,
                        ToolState toolState, CollisionTags tags, ShapeDraft shapeDraft, SpritePlacer spritePlacer,
                        ShapePlacer shapePlacer) {
        SpriteOutlineDrawer outlines = new SpriteOutlineDrawer(viewport, geometry);
        ShapeDrawer shapes = new ShapeDrawer(viewport);
        this.placeholders = new PlaceholderOverlay(outlines, geometry);
        this.collision = new CollisionOverlay(shapes, tags);
        this.selectionOutlines = new SelectionOverlay(viewport, outlines, shapes, selection, boxSelect);
        this.draft = new DraftOverlay(shapes, shapeDraft, tags);
        this.toolbar = new SceneToolbar(viewport, toolState);
        this.hint = new ToolHintOverlay(viewport, toolState, spritePlacer, shapePlacer);
    }

    void draw(Room room) {
        placeholders.draw(room);
        collision.draw(room);
        selectionOutlines.draw();
        draft.draw();
        toolbar.draw();
        hint.draw(room, toolbar.getBottom());
    }

    /** Whether the mouse is over a widget of the overlay, which then gets the input instead of the scene. */
    boolean isHovered() {
        return toolbar.isHovered();
    }
}
