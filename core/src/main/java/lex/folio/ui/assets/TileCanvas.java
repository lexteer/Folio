package lex.folio.ui.assets;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec4;
import imgui.flag.ImGuiCol;
import lex.folio.ui.common.UiColors;

/** The square a tile is drawn in, with the pieces tiles are made of. */
final class TileCanvas {
    static final float SHADOW_SIZE = 4f;

    private static final int SHADOW_LAYERS = (int) SHADOW_SIZE;
    private static final float CARD_ROUNDING = 2f;
    private static final float CARD_PADDING = 0.08f;
    private static final float HIGHLIGHT_GROW = 1.5f;
    private static final float HIGHLIGHT_THICKNESS = 2f;
    private static final float BADGE_PADDING = 2f;
    private static final String ELLIPSIS = "...";

    private static final int HIGHLIGHT_COLOR = UiColors.ACCENT;
    private static final int SELECTED_COLOR = UiColors.withAlpha(UiColors.ACCENT, 0.25f);
    private static final int SHADOW_COLOR = UiColors.pack(0f, 0f, 0f, 0.08f);
    private static final int BADGE_COLOR = UiColors.pack(0f, 0f, 0f, 0.6f);

    private ImDrawList drawList;
    private float minX;
    private float minY;
    private float size;

    /** Positions the canvas at the screen position of the last item. */
    void placeOnLastItem(float size) {
        place(ImGui.getItemRectMinX(), ImGui.getItemRectMinY(), size);
    }

    void place(float minX, float minY, float size) {
        this.drawList = ImGui.getWindowDrawList();
        this.minX = minX;
        this.minY = minY;
        this.size = size;
    }

    ImDrawList getDrawList() {
        return drawList;
    }

    float getMinX() {
        return minX;
    }

    float getMinY() {
        return minY;
    }

    float getSize() {
        return size;
    }

    void drawCard() {
        float maxX = minX + size;
        float maxY = minY + size;
        for (int layer = 1; layer <= SHADOW_LAYERS; layer++) {
            drawList.addRectFilled(minX - layer, minY - layer, maxX + layer, maxY + layer,
                SHADOW_COLOR, CARD_ROUNDING + layer);
        }
        drawList.addRectFilled(minX, minY, maxX, maxY, getOpaqueWindowBackground(), CARD_ROUNDING);
    }

    void drawSelectedBackground() {
        drawList.addRectFilled(minX, minY, minX + size, minY + size, SELECTED_COLOR, CARD_ROUNDING);
    }

    void drawHighlight() {
        drawList.addRect(minX - HIGHLIGHT_GROW, minY - HIGHLIGHT_GROW,
            minX + size + HIGHLIGHT_GROW, minY + size + HIGHLIGHT_GROW,
            HIGHLIGHT_COLOR, CARD_ROUNDING + HIGHLIGHT_GROW, HIGHLIGHT_THICKNESS);
    }

    /** Draws the image as large as fits inside the card, keeping its proportions. */
    void drawFittedImage(TextureRegion region) {
        float padding = size * CARD_PADDING;
        float area = size - 2f * padding;
        float scale = area / Math.max(region.getRegionWidth(), region.getRegionHeight());
        float width = region.getRegionWidth() * scale;
        float height = region.getRegionHeight() * scale;
        float x = minX + padding + (area - width) / 2f;
        float y = minY + padding + (area - height) / 2f;

        drawList.addImage(region.getTexture().getTextureObjectHandle(), x, y, x + width, y + height,
            region.getU(), region.getV(), region.getU2(), region.getV2());
    }

    /** Draws a label in the card's top right corner. */
    void drawCornerBadge(String text) {
        float padding = size * CARD_PADDING;
        float textWidth = ImGui.calcTextSizeX(text);
        float x = minX + size - padding - textWidth;
        float y = minY + padding;
        drawList.addRectFilled(x - BADGE_PADDING, y - BADGE_PADDING,
            x + textWidth + BADGE_PADDING, y + ImGui.getTextLineHeight() + BADGE_PADDING,
            BADGE_COLOR, CARD_ROUNDING);
        drawList.addText(x, y, ImGui.getColorU32(ImGuiCol.Text), text);
    }

    /** Draws the name centered at the given distance below the card, shortened with "..." if it is too wide. */
    void drawName(String name, float gap) {
        String shownName = fitToWidth(name, size);
        float x = minX + (size - ImGui.calcTextSizeX(shownName)) / 2f;
        drawList.addText(x, minY + size + gap, ImGui.getColorU32(ImGuiCol.Text), shownName);

        if (!shownName.equals(name) && ImGui.isItemHovered()) {
            ImGui.setTooltip(name);
        }
    }

    private static String fitToWidth(String text, float maxWidth) {
        if (ImGui.calcTextSizeX(text) <= maxWidth) return text;

        String shortened = text;
        while (!shortened.isEmpty() && ImGui.calcTextSizeX(shortened + ELLIPSIS) > maxWidth) {
            shortened = shortened.substring(0, shortened.length() - 1);
        }
        return shortened + ELLIPSIS;
    }

    private static int getOpaqueWindowBackground() {
        ImVec4 background = ImGui.getStyle().getColor(ImGuiCol.WindowBg);
        return ImGui.colorConvertFloat4ToU32(background.x, background.y, background.z, 1f);
    }
}
