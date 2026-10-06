package lex.folio.ui.assets;

import imgui.ImGui;

/** Flows tiles left to right, wrapping to a new row when the window is full. */
final class TileLayout {
    private static final float TILE_SIZE_IN_FONT_SIZES = 5f;
    private static final float TILE_GAP_IN_FONT_SIZES = 0.8f;

    private float tileSize;
    private float tileGap;
    private int columns;
    private int tileIndex;

    /** Call before placing the first tile of a frame, once the cursor is where the first tile goes. */
    void begin() {
        tileSize = ImGui.getFontSize() * TILE_SIZE_IN_FONT_SIZES;
        tileGap = ImGui.getFontSize() * TILE_GAP_IN_FONT_SIZES;

        float available = ImGui.getContentRegionAvailX() - AssetTile.SHADOW_SIZE;
        columns = Math.max(1, (int) ((available + tileGap) / (tileSize + tileGap)));
        tileIndex = 0;
    }

    float getTileSize() {
        return tileSize;
    }

    /** Moves the cursor to where the next tile goes. */
    void placeNextTile() {
        if (tileIndex % columns != 0) ImGui.sameLine(0, tileGap);
        tileIndex++;
    }
}
