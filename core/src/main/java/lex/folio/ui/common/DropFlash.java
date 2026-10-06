package lex.folio.ui.common;

import imgui.ImGui;
import imgui.flag.ImGuiCol;

public final class DropFlash {
    private static final double DURATION_SECONDS = 0.5;
    private static final float OUTLINE_OFFSET = 3.5f;
    private static final float OUTLINE_THICKNESS = 2f;

    private String folder;
    private double startTime;

    public void start(String folder) {
        this.folder = folder;
        startTime = ImGui.getTime();
    }

    public void drawOnLastItem(String folder) {
        if (!folder.equals(this.folder)) return;

        float alpha = getRemainingFraction();
        if (alpha <= 0f) return;

        ImGui.getWindowDrawList().addRect(
            ImGui.getItemRectMinX() - OUTLINE_OFFSET, ImGui.getItemRectMinY() - OUTLINE_OFFSET,
            ImGui.getItemRectMaxX() + OUTLINE_OFFSET, ImGui.getItemRectMaxY() + OUTLINE_OFFSET,
            ImGui.getColorU32(ImGuiCol.DragDropTarget, alpha), 0f, OUTLINE_THICKNESS);
    }

    private float getRemainingFraction() {
        double elapsed = ImGui.getTime() - startTime;
        return (float) (1.0 - elapsed / DURATION_SECONDS);
    }
}
