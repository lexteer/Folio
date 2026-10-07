package lex.folio.ui.scene;

import lex.folio.model.CollisionLayer;
import lex.folio.model.CollisionShape;
import lex.folio.model.CollisionTag;
import lex.folio.model.CollisionTags;
import lex.folio.model.Room;
import lex.folio.ui.common.UiColors;

/** Shows the collision shapes of the visible collision layers, in the color of their tag. */
class CollisionOverlay {
    private static final float FILL_ALPHA = 0.25f;
    private static final float LINE_ALPHA = 0.9f;
    private static final float LINE_THICKNESS = 1.5f;
    /** For shapes whose tag is gone. */
    private static final int MISSING_TAG_RGB = 0x909090;

    private final ShapeDrawer drawer;
    private final CollisionTags tags;

    CollisionOverlay(ShapeDrawer drawer, CollisionTags tags) {
        this.drawer = drawer;
        this.tags = tags;
    }

    void draw(Room room) {
        for (CollisionLayer layer : room.getCollisionLayers()) {
            if (!layer.isVisible()) continue;

            for (CollisionShape shape : layer.getItems()) {
                int rgb = colorOf(shape);
                drawer.draw(shape, color(rgb, LINE_ALPHA), color(rgb, FILL_ALPHA), LINE_THICKNESS, false);
            }
        }
    }

    int colorOf(CollisionShape shape) {
        CollisionTag tag = tags.find(shape.getTag());
        return tag == null ? MISSING_TAG_RGB : tag.rgb();
    }

    static int color(int rgb, float alpha) {
        return UiColors.pack((rgb >> 16 & 0xFF) / 255f, (rgb >> 8 & 0xFF) / 255f, (rgb & 0xFF) / 255f, alpha);
    }
}
