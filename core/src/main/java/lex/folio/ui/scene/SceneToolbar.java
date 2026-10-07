package lex.folio.ui.scene;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import lex.folio.ui.common.Icons;
import lex.folio.ui.common.UiColors;
import lex.folio.scene.tool.Tool;
import lex.folio.scene.tool.ToolState;

final class SceneToolbar {
    private static final float TOP_MARGIN_IN_FONT_SIZES = 0.5f;
    private static final float PADDING = 4f;
    private static final float ROUNDING = 6f;
    /** How much taller than a frame the buttons are, which keeps the wider icons clear of their corners. */
    private static final float BUTTON_EXTRA = 8f;
    /** The icons are drawn a little lower than centered text, which puts the middle of the shape in the middle. */

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
        drawToolButton(Icons.SELECT, "Select", Tool.SELECT);
        ImGui.sameLine();
        drawToolButton(Icons.PAINT, "Paint", Tool.PAINT);
    }

    boolean isHovered() {
        return ImGui.isMouseHoveringRect(minX, minY, maxX, maxY);
    }

    private void computeBounds() {
        float width = getButtonsWidth() + 2f * PADDING;
        float height = getButtonSize() + 2f * PADDING;

        minX = viewport.getImageX() + (viewport.getWidth() - width) / 2f;
        minY = viewport.getImageY() + ImGui.getFontSize() * TOP_MARGIN_IN_FONT_SIZES;
        maxX = minX + width;
        maxY = minY + height;
    }

    private static float getButtonSize() {
        return ImGui.getFrameHeight() + BUTTON_EXTRA;
    }

    private static float getButtonsWidth() {
        return 2f * getButtonSize() + ImGui.getStyle().getItemSpacingX();
    }

    private void drawToolButton(String icon, String name, Tool tool) {
        boolean active = toolState.getTool() == tool;
        if (active) ImGui.pushStyleColor(ImGuiCol.Button, ImGui.getColorU32(ImGuiCol.ButtonActive));
        ImGui.pushStyleVar(ImGuiStyleVar.FrameRounding, ROUNDING);

        float size = getButtonSize();
        if (ImGui.button("##" + name, size, size)) {
            toolState.setTool(tool);
        }
        drawIconCentered(icon);
        if (ImGui.isItemHovered()) ImGui.setTooltip(name);

        ImGui.popStyleVar();
        if (active) ImGui.popStyleColor();
    }

    private static void drawIconCentered(String icon) {
        ImVec2 box = ImGui.calcTextSize(icon);
        float x = Math.round((ImGui.getItemRectMinX() + ImGui.getItemRectMaxX() - box.x) / 2f);
        float y = Math.round((ImGui.getItemRectMinY() + ImGui.getItemRectMaxY() - box.y) / 2f);
        ImGui.getWindowDrawList().addText(ImGui.getFont(), Math.round(Icons.FONT_SIZE), x, y, ImGui.getColorU32(ImGuiCol.Text), icon);
    }
}
