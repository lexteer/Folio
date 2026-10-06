package lex.folio.ui.scene;

import lex.folio.model.Room;
import lex.folio.model.Sprite;
import lex.folio.model.SpriteLayer;
import lex.folio.scene.sprite.SpriteGeometry;
import lex.folio.ui.common.UiColors;

/** Marks sprites whose image is missing. */
class PlaceholderOverlay {
    private static final int COLOR = UiColors.pack(0.95f, 0.45f, 0.85f, 1f);

    private final SpriteOutlineDrawer outlines;
    private final SpriteGeometry geometry;

    PlaceholderOverlay(SpriteOutlineDrawer outlines, SpriteGeometry geometry) {
        this.outlines = outlines;
        this.geometry = geometry;
    }

    void draw(Room room) {
        for (SpriteLayer layer : room.getSpriteLayers()) {
            if (layer.isVisible()) {
                drawLayer(layer);
            }
        }
    }

    private void drawLayer(SpriteLayer layer) {
        for (Sprite sprite : layer.getItems()) {
            if (geometry.isPlaceholder(sprite)) {
                outlines.drawOutline(sprite, COLOR);
                outlines.drawOrientationMarker(sprite, COLOR);
            }
        }
    }
}
