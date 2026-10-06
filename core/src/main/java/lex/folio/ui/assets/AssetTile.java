package lex.folio.ui.assets;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import imgui.ImGui;

/** Draws the tiles of the assets grid: an image card or a folder, with a name below. */
final class AssetTile {
    static final float SHADOW_SIZE = TileCanvas.SHADOW_SIZE;

    private static final float NAME_GAP_IN_FONT_SIZES = 0.5f;

    private final TileCanvas canvas = new TileCanvas();

    /** Returns whether the tile was clicked. */
    boolean drawImage(String id, String name, TextureRegion region, float size, boolean selected, boolean armed) {
        boolean clicked = beginTile(id, size);
        canvas.drawCard();
        if (selected) {
            canvas.drawSelectedBackground();
        }
        if (region != null) {
            canvas.drawFittedImage(region);
        }
        if (armed) {
            canvas.drawHighlight();
        }
        canvas.drawName(name, getNameGap());
        return clicked;
    }

    /** Returns whether the tile was clicked. */
    boolean drawFolder(String id, String name, float size, boolean selected) {
        boolean clicked = beginTile(id, size);
        if (selected) {
            canvas.drawSelectedBackground();
        }
        FolderIcon.draw(canvas.getDrawList(), canvas.getMinX(), canvas.getMinY(), canvas.getSize());
        canvas.drawName(name, getNameGap());
        return clicked;
    }

    /** The screen Y of the name of the tile drawn last. */
    float getLastNameY() {
        return canvas.getMinY() + canvas.getSize() + getNameGap();
    }

    /** Draws what follows the mouse while dragging an asset; itemCount is how many items are being dragged. */
    void drawPreview(TextureRegion region, float size, int itemCount) {
        canvas.place(ImGui.getCursorScreenPosX() + SHADOW_SIZE, ImGui.getCursorScreenPosY() + SHADOW_SIZE, size);
        ImGui.dummy(size + 2f * SHADOW_SIZE, size + 2f * SHADOW_SIZE);

        canvas.drawCard();
        if (region != null) {
            canvas.drawFittedImage(region);
        }
        if (itemCount > 1) {
            canvas.drawCornerBadge("+" + (itemCount - 1));
        }
    }

    private boolean beginTile(String id, float size) {
        boolean clicked = ImGui.invisibleButton(id, size, size + getNameGap() + ImGui.getTextLineHeight());
        canvas.placeOnLastItem(size);
        return clicked;
    }

    private static float getNameGap() {
        return ImGui.getFontSize() * NAME_GAP_IN_FONT_SIZES;
    }
}
