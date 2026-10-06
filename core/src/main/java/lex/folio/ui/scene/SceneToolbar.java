package lex.folio.ui.scene;

import imgui.ImGui;
import imgui.flag.ImGuiCol;
import lex.folio.ui.common.UiColors;
import lex.folio.scene.tool.Tool;
import lex.folio.scene.tool.ToolState;

final class SceneToolbar {
    private static final String SELECT_LABEL = "Select";
    private static final String PAINT_LABEL = "Paint";
    private static final float TOP_MARGIN_IN_FONT_SIZES = 0.5f;
    private static final float PADDING = 4f;
    private static final float ROUNDING = 6f;

    private static final int BACKGROUND_COLOR = UiColors.pack(0.1f, 0.1f, 0.12f, 0.9f);

    private final SceneViewport viewport;
    private final ToolState toolState;

    private float minX;
    private float minY;
    private float maxX;
    private float maxY;

    SceneToolbar(SceneViewport viewport, ToolState toolState) {
        this.viewport = viewport;
        this.toolState = toolState;
    }

    void draw() {
        computeBounds();
        ImGui.getWindowDrawList().addRectFilled(minX, minY, maxX, maxY, BACKGROUND_COLOR, ROUNDING);

        ImGui.setCursorScreenPos(minX + PADDING, minY + PADDING);
        drawToolButton(SELECT_LABEL, Tool.SELECT);
        ImGui.sameLine();
        drawToolButton(PAINT_LABEL, Tool.PAINT);
    }

    boolean isHovered() {
        return ImGui.isMouseHoveringRect(minX, minY, maxX, maxY);
    }

    private void computeBounds() {
        float width = getButtonsWidth() + 2f * PADDING;
        float height = ImGui.getFrameHeight() + 2f * PADDING;

        minX = viewport.getImageX() + (viewport.getWidth() - width) / 2f;
        minY = viewport.getImageY() + ImGui.getFontSize() * TOP_MARGIN_IN_FONT_SIZES;
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
