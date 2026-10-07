package lex.folio.ui.scene;

import imgui.ImGui;
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
    private static final float CURSOR_SIZE = 12f;

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
        drawToolButton(null, "Select", Tool.SELECT);
        ImGui.sameLine();
        drawToolButton(Icons.PAINT, "Paint", Tool.PAINT);
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

    /** The buttons are square, one frame high. */
    private static float getButtonsWidth() {
        return 2f * ImGui.getFrameHeight() + ImGui.getStyle().getItemSpacingX();
    }

    /** Draws an arrow cursor, pointing up and left, over the button just submitted. The icon font only has one that points up. */
    private static void drawCursor() {
        float x = Math.round((ImGui.getItemRectMinX() + ImGui.getItemRectMaxX()) / 2f - CURSOR_SIZE * 0.3f);
        float y = Math.round((ImGui.getItemRectMinY() + ImGui.getItemRectMaxY()) / 2f - CURSOR_SIZE * 0.5f);
        float s = CURSOR_SIZE;
        int color = ImGui.getColorU32(ImGuiCol.Text);

        var draw = ImGui.getWindowDrawList();
        draw.addTriangleFilled(x, y, x, y + s * 0.85f, x + s * 0.62f, y + s * 0.6f, color);
        draw.addQuadFilled(x + s * 0.2f, y + s * 0.7f, x + s * 0.34f, y + s, x + s * 0.5f, y + s * 0.93f,
            x + s * 0.36f, y + s * 0.6f, color);
    }

    private void drawToolButton(String icon, String name, Tool tool) {
        boolean active = toolState.getTool() == tool;
        if (active) ImGui.pushStyleColor(ImGuiCol.Button, ImGui.getColorU32(ImGuiCol.ButtonActive));
        ImGui.pushStyleVar(ImGuiStyleVar.FrameRounding, ROUNDING);

        float size = ImGui.getFrameHeight();
        if (ImGui.button((icon == null ? "" : icon) + "##" + name, size, size)) {
            toolState.setTool(tool);
        }
        if (icon == null) drawCursor();
        if (ImGui.isItemHovered()) ImGui.setTooltip(name);

        ImGui.popStyleVar();
        if (active) ImGui.popStyleColor();
    }
}
