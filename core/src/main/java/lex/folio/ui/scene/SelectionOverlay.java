package lex.folio.ui.scene;

import lex.folio.model.RoomObject;
import lex.folio.model.Sprite;
import lex.folio.scene.Selection;
import lex.folio.ui.common.UiColors;

/** Outlines the selected objects. */
class SelectionOverlay {
    private final SpriteOutlineDrawer outlines;
    private final Selection selection;

    SelectionOverlay(SpriteOutlineDrawer outlines, Selection selection) {
        this.outlines = outlines;
        this.selection = selection;
    }

    void draw() {
        for (RoomObject object : selection.getObjects()) {
            if (object instanceof Sprite sprite) {
                outlines.drawOutline(sprite, UiColors.ACCENT);
            }
        }
    }
}
