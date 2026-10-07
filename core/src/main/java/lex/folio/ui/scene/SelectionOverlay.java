package lex.folio.ui.scene;

import com.badlogic.gdx.math.Vector2;
import imgui.ImGui;
import lex.folio.model.CollisionShape;
import lex.folio.model.RoomObject;
import lex.folio.model.Sprite;
import lex.folio.scene.BoxSelect;
import lex.folio.scene.Selection;
import lex.folio.ui.common.UiColors;

/** Outlines the selected objects, and the box being dragged out to select more. */
class SelectionOverlay {
    private static final int BOX_FILL_COLOR = UiColors.withAlpha(UiColors.ACCENT, 0.15f);
    private static final int BOX_BORDER_COLOR = UiColors.withAlpha(UiColors.ACCENT, 0.8f);

    private static final float OUTLINE_THICKNESS = 2f;

    private final SceneViewport viewport;
    private final SpriteOutlineDrawer outlines;
    private final ShapeDrawer shapes;
    private final Selection selection;
    private final BoxSelect boxSelect;

    SelectionOverlay(SceneViewport viewport, SpriteOutlineDrawer outlines, ShapeDrawer shapes, Selection selection,
                     BoxSelect boxSelect) {
        this.viewport = viewport;
        this.outlines = outlines;
        this.shapes = shapes;
        this.selection = selection;
        this.boxSelect = boxSelect;
    }

    void draw() {
        for (RoomObject object : selection.getObjects()) {
            if (object instanceof Sprite sprite) {
                outlines.drawOutline(sprite, UiColors.ACCENT);
            } else if (object instanceof CollisionShape shape) {
                shapes.draw(shape, UiColors.ACCENT, UiColors.withAlpha(UiColors.ACCENT, 0.12f), OUTLINE_THICKNESS, true);
            }
        }
        drawBox();
    }

    private void drawBox() {
        if (!boxSelect.isActive()) return;

        Vector2 a = viewport.worldToScreen(boxSelect.getMinX(), boxSelect.getMinY());
        Vector2 b = viewport.worldToScreen(boxSelect.getMaxX(), boxSelect.getMaxY());
        float minX = Math.min(a.x, b.x);
        float minY = Math.min(a.y, b.y);
        float maxX = Math.max(a.x, b.x);
        float maxY = Math.max(a.y, b.y);
        ImGui.getWindowDrawList().addRectFilled(minX, minY, maxX, maxY, BOX_FILL_COLOR);
        ImGui.getWindowDrawList().addRect(minX, minY, maxX, maxY, BOX_BORDER_COLOR);
    }
}
