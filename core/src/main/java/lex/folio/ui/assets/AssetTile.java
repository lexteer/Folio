package lex.folio.ui.assets;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec4;
import imgui.flag.ImGuiCol;
import lex.folio.ui.common.UiColors;

final class AssetTile {
    private static final String ELLIPSIS = "...";
    private static final float ROUNDING = 4f;
    private static final float NAME_GAP_IN_FONT_SIZES = 0.5f;
    private static final float CARD_ROUNDING = 2f;
    private static final float CARD_PADDING = 0.08f;
    private static final int SHADOW_LAYERS = 4;
    static final float SHADOW_SIZE = SHADOW_LAYERS;

    // Folder icon, as fractions of the tile size
    private static final float FOLDER_LEFT = 0.1f;
    private static final float FOLDER_RIGHT = 0.9f;
    private static final float FOLDER_TAB_TOP = 0.2f;
    private static final float FOLDER_BODY_TOP = 0.3f;
    private static final float FOLDER_BOTTOM = 0.85f;
    private static final float FOLDER_TAB_WIDTH = 0.35f;

    private static final float HIGHLIGHT_GROW = 1.5f;
    private static final float HIGHLIGHT_THICKNESS = 2f;

    private static final float BADGE_PADDING = 2f;

    private static final int HIGHLIGHT_COLOR = UiColors.ACCENT;
    private static final int SELECTED_COLOR = UiColors.withAlpha(UiColors.ACCENT, 0.25f);
    private static final int FOLDER_COLOR = UiColors.pack(0.85f, 0.70f, 0.35f, 1f);
    private static final int SHADOW_COLOR = UiColors.pack(0f, 0f, 0f, 0.08f);
    private static final int BADGE_COLOR = UiColors.pack(0f, 0f, 0f, 0.6f);

    private ImDrawList drawList;
    private float minX;
    private float minY;
    private float size;
    private float nameGap;

    boolean drawImage(String id, String name, TextureRegion region, float size, boolean selected, boolean armed) {
        boolean clicked = beginTile(id, size);
        drawCard();
        if (selected) {
            drawSelectedBackground();
        }
        if (region != null) {
            drawFittedImage(region);
        }
        if (armed) {
            drawHighlight();
        }
        drawName(name);
        return clicked;
    }

    private void drawHighlight() {
        drawList.addRect(minX - HIGHLIGHT_GROW, minY - HIGHLIGHT_GROW,
            minX + size + HIGHLIGHT_GROW, minY + size + HIGHLIGHT_GROW,
            HIGHLIGHT_COLOR, CARD_ROUNDING + HIGHLIGHT_GROW, HIGHLIGHT_THICKNESS);
    }

    private void drawCard() {
        float maxX = minX + size;
        float maxY = minY + size;
        drawCardShadow(maxX, maxY);
        drawList.addRectFilled(minX, minY, maxX, maxY, getOpaqueWindowBackground(), CARD_ROUNDING);
    }

    private static int getOpaqueWindowBackground() {
        ImVec4 background = ImGui.getStyle().getColor(ImGuiCol.WindowBg);
        return ImGui.colorConvertFloat4ToU32(background.x, background.y, background.z, 1f);
    }

    private void drawCardShadow(float maxX, float maxY) {
        for (int layer = 1; layer <= SHADOW_LAYERS; layer++) {
            drawList.addRectFilled(minX - layer, minY - layer, maxX + layer, maxY + layer,
                SHADOW_COLOR, CARD_ROUNDING + layer);
        }
    }

    boolean drawFolder(String id, String name, float size, boolean selected) {
        boolean clicked = beginTile(id, size);
        if (selected) {
            drawSelectedBackground();
        }
        drawFolderIcon();
        drawName(name);
        return clicked;
    }

    private boolean beginTile(String id, float size) {
        this.size = size;
        nameGap = ImGui.getFontSize() * NAME_GAP_IN_FONT_SIZES;

        boolean clicked = ImGui.invisibleButton(id, size, size + nameGap + ImGui.getTextLineHeight());
        drawList = ImGui.getWindowDrawList();
        minX = ImGui.getItemRectMinX();
        minY = ImGui.getItemRectMinY();
        return clicked;
    }

    private void drawFittedImage(TextureRegion region) {
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

    private void drawFolderIcon() {
        float left = minX + size * FOLDER_LEFT;
        float right = minX + size * FOLDER_RIGHT;
        float tabRight = left + (right - left) * FOLDER_TAB_WIDTH;
        float tabTop = minY + size * FOLDER_TAB_TOP;
        float bodyTop = minY + size * FOLDER_BODY_TOP;
        float bottom = minY + size * FOLDER_BOTTOM;

        drawList.addRectFilled(left, tabTop, tabRight, bodyTop + ROUNDING, FOLDER_COLOR, ROUNDING);
        drawList.addRectFilled(left, bodyTop, right, bottom, FOLDER_COLOR, ROUNDING);
    }

    private void drawName(String name) {
        String shownName = fitToWidth(name, size);
        float x = minX + (size - ImGui.calcTextSizeX(shownName)) / 2f;
        drawList.addText(x, minY + size + nameGap, ImGui.getColorU32(ImGuiCol.Text), shownName);

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

    void drawPreview(TextureRegion region, float size, int itemCount) {
        this.size = size;
        drawList = ImGui.getWindowDrawList();
        minX = ImGui.getCursorScreenPosX() + SHADOW_SIZE;
        minY = ImGui.getCursorScreenPosY() + SHADOW_SIZE;
        ImGui.dummy(size + 2f * SHADOW_SIZE, size + 2f * SHADOW_SIZE);

        drawCard();
        if (region != null) {
            drawFittedImage(region);
        }

        if (itemCount > 1) {
            drawCountBadge("+" + (itemCount - 1));
        }
    }

    private void drawCountBadge(String text) {
        float padding = size * CARD_PADDING;
        float textWidth = ImGui.calcTextSizeX(text);
        float x = minX + size - padding - textWidth;
        float y = minY + padding;
        drawList.addRectFilled(x - BADGE_PADDING, y - BADGE_PADDING,
            x + textWidth + BADGE_PADDING, y + ImGui.getTextLineHeight() + BADGE_PADDING,
            BADGE_COLOR, CARD_ROUNDING);
        drawList.addText(x, y, ImGui.getColorU32(ImGuiCol.Text), text);
    }

    private void drawSelectedBackground() {
        drawList.addRectFilled(minX, minY, minX + size, minY + size, SELECTED_COLOR, CARD_ROUNDING);
    }
}
