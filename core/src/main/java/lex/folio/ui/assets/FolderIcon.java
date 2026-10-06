package lex.folio.ui.assets;

import imgui.ImDrawList;
import lex.folio.ui.common.UiColors;

/** A flat folder drawn from two rounded rectangles. */
final class FolderIcon {
    private static final int COLOR = UiColors.pack(0.85f, 0.70f, 0.35f, 1f);
    private static final float ROUNDING = 4f;

    // As fractions of the tile size
    private static final float LEFT = 0.1f;
    private static final float RIGHT = 0.9f;
    private static final float TAB_TOP = 0.2f;
    private static final float BODY_TOP = 0.3f;
    private static final float BOTTOM = 0.85f;
    private static final float TAB_WIDTH = 0.35f;

    private FolderIcon() {
    }

    static void draw(ImDrawList drawList, float minX, float minY, float size) {
        float left = minX + size * LEFT;
        float right = minX + size * RIGHT;
        float tabRight = left + (right - left) * TAB_WIDTH;
        float tabTop = minY + size * TAB_TOP;
        float bodyTop = minY + size * BODY_TOP;
        float bottom = minY + size * BOTTOM;

        drawList.addRectFilled(left, tabTop, tabRight, bodyTop + ROUNDING, COLOR, ROUNDING);
        drawList.addRectFilled(left, bodyTop, right, bottom, COLOR, ROUNDING);
    }
}
