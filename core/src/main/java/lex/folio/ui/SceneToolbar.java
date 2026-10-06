package lex.folio.ui;

import imgui.ImGui;
import imgui.flag.ImGuiCol;
import lex.folio.scene.Tool;
import lex.folio.scene.ToolState;

final class SceneToolbar {
    private static final String SELECT_LABEL = "Select";
    private static final String PAINT_LABEL = "Paint";
    private static final float TOP_MARGIN_IN_FONT_SIZES = 0.5f;
    private static final float PADDING = 4f;
    private static final float ROUNDING = 6f;

    private final ToolState toolState;
    private final int backgroundColor = ImGui.colorConvertFloat4ToU32(0.1f, 0.1f, 0.12f, 0.9f);

    private float minX;
    private float minY;
    private float maxX;
    private float maxY;

    SceneToolbar(ToolState toolState) {
        this.toolState = toolState;
    }

    void draw(float sceneMinX, float sceneMinY, float sceneWidth) {
        computeBounds(sceneMinX, sceneMinY, sceneWidth);
        ImGui.getWindowDrawList().addRectFilled(minX, minY, maxX, maxY, backgroundColor, ROUNDING);

        ImGui.setCursorScreenPos(minX + PADDING, minY + PADDING);
        drawToolButton(SELECT_LABEL, Tool.SELECT);
        ImGui.sameLine();
        drawToolButton(PAINT_LABEL, Tool.PAINT);
    }

    boolean isHovered() {
        return ImGui.isMouseHoveringRect(minX, minY, maxX, maxY);
    }

    private void computeBounds(float sceneMinX, float sceneMinY, float sceneWidth) {
        float width = getButtonsWidth() + 2f * PADDING;
        float height = ImGui.getFrameHeight() + 2f * PADDING;

        minX = sceneMinX + (sceneWidth - width) / 2f;
        minY = sceneMinY + ImGui.getFontSize() * TOP_MARGIN_IN_FONT_SIZES;
        maxX = minX + width;
        maxY = minY + height;
    }

    private float getButtonsWidth() {
        return getButtonWidth(SELECT_LABEL) + ImGui.getStyle().getItemSpacingX() + getButtonWidth(PAINT_LABEL);
    }

    private static float getButtonWidth(String label) {
        return ImGui.calcTextSizeX(label) + 2f * ImGui.getStyle().getFramePaddingX();
    }

    private void drawToolButton(String label, Tool tool) {
        boolean active = toolState.getTool() == tool;
        if (active) ImGui.pushStyleColor(ImGuiCol.Button, ImGui.getColorU32(ImGuiCol.ButtonActive));

        if (ImGui.button(label)) {
            toolState.setTool(tool);
        }
        if (active) ImGui.popStyleColor();
    }
}
