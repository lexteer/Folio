package lex.folio.ui.assets;

import imgui.ImGui;
import lex.folio.ui.common.UiColors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

final class BoxSelection {
    private record TileBounds(BrowserItem item, float minX, float minY, float maxX, float maxY) {
    }

    private static final int FILL_COLOR = UiColors.withAlpha(UiColors.ACCENT, 0.15f);
    private static final int BORDER_COLOR = UiColors.withAlpha(UiColors.ACCENT, 0.8f);

    private final List<TileBounds> tiles = new ArrayList<>();
    private final Set<BrowserItem> keptItems = new LinkedHashSet<>();
    private boolean active;
    private float startX;
    private float startY;

    void clearTiles() {
        tiles.clear();
    }

    void addLastItemAsTile(BrowserItem item) {
        tiles.add(new TileBounds(item,
            toContentX(ImGui.getItemRectMinX()), toContentY(ImGui.getItemRectMinY()),
            toContentX(ImGui.getItemRectMaxX()), toContentY(ImGui.getItemRectMaxY())));
    }

    boolean isActive() {
        return active;
    }

    void start(Collection<BrowserItem> itemsToKeep) {
        active = true;
        keptItems.clear();
        keptItems.addAll(itemsToKeep);
        startX = toContentX(ImGui.getMousePosX());
        startY = toContentY(ImGui.getMousePosY());
    }

    Set<BrowserItem> getSelectedItems() {
        Set<BrowserItem> result = new LinkedHashSet<>(keptItems);
        for (TileBounds tile : tiles) {
            if (overlapsBox(tile)) {
                result.add(tile.item());
            }
        }
        return result;
    }

    private boolean overlapsBox(TileBounds tile) {
        float mouseX = toContentX(ImGui.getMousePosX());
        float mouseY = toContentY(ImGui.getMousePosY());
        return tile.minX() <= Math.max(startX, mouseX) && tile.maxX() >= Math.min(startX, mouseX)
            && tile.minY() <= Math.max(startY, mouseY) && tile.maxY() >= Math.min(startY, mouseY);
    }

    void draw() {
        float x1 = toScreenX(startX);
        float y1 = toScreenY(startY);
        float x2 = ImGui.getMousePosX();
        float y2 = ImGui.getMousePosY();
        float minX = Math.min(x1, x2);
        float minY = Math.min(y1, y2);
        float maxX = Math.max(x1, x2);
        float maxY = Math.max(y1, y2);
        ImGui.getWindowDrawList().addRectFilled(minX, minY, maxX, maxY, FILL_COLOR);
        ImGui.getWindowDrawList().addRect(minX, minY, maxX, maxY, BORDER_COLOR);
    }

    void finish() {
        active = false;
        keptItems.clear();
    }

    private static float toContentX(float screenX) {
        return screenX - ImGui.getWindowPosX() + ImGui.getScrollX();
    }

    private static float toContentY(float screenY) {
        return screenY - ImGui.getWindowPosY() + ImGui.getScrollY();
    }

    private static float toScreenX(float contentX) {
        return contentX - ImGui.getScrollX() + ImGui.getWindowPosX();
    }

    private static float toScreenY(float contentY) {
        return contentY - ImGui.getScrollY() + ImGui.getWindowPosY();
    }
}
