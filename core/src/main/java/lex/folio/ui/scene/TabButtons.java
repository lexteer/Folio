package lex.folio.ui.scene;

import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiMouseButton;
import imgui.flag.ImGuiTabItemFlags;
import lex.folio.ui.common.Icons;

/** The round buttons of the room tab bar: the close button on each tab and the one that adds a room. */
final class TabButtons {
    private static final float CLOSE_SIZE_PER_FONT_SIZE = 1f;
    private static final String ADD_LABEL = "   ";
    private static final float HOVER_ALPHA = 0.2f;
    /** The icons are drawn smaller than the circles around them. */
    private static final float ICON_SIZE_PER_FONT_SIZE = 0.8f;

    private TabButtons() {
    }

    private static float closeDiameter() {
        return ImGui.getFontSize() * CLOSE_SIZE_PER_FONT_SIZE;
    }

    /** Trailing spaces for a tab label, which keep the label's text clear of the close button. */
    static String closeButtonPadding() {
        int spaces = (int) Math.ceil((closeDiameter() + 4f) / ImGui.calcTextSize(" ").x);
        return " ".repeat(spaces);
    }

    /**
     * Draws the close button on the tab that was just submitted, in place of the one ImGui would draw. A room with
     * unsaved changes shows a dot until the mouse is over the button. Returns whether it was clicked.
     */
    static boolean drawClose(boolean unsaved) {
        if (!ImGui.isItemVisible()) return false;

        float radius = closeDiameter() / 2f;
        float x = Math.round(ImGui.getItemRectMaxX() - ImGui.getStyle().getFramePaddingX() - radius);
        float y = textCenterY();
        boolean hovered = ImGui.isItemHovered() && isMouseWithin(x, y, radius);

        ImDrawList draw = ImGui.getWindowDrawList();
        if (hovered) {
            draw.addCircleFilled(x, y, radius, ImGui.getColorU32(1f, 1f, 1f, HOVER_ALPHA));
        }
        if (unsaved && !hovered) {
            draw.addCircleFilled(x, y, radius * 0.4f, ImGui.getColorU32(ImGuiCol.Text));
        } else {
            drawIconCentered(draw, Icons.CLOSE, x, y);
        }
        return hovered && ImGui.isMouseClicked(ImGuiMouseButton.Left);
    }

    /** Draws the add button as a circle, which lights up under the mouse. Returns whether it was clicked. */
    static boolean drawAdd() {
        int resting = ImGui.getColorU32(ImGuiCol.Tab);
        int hovering = ImGui.getColorU32(1f, 1f, 1f, HOVER_ALPHA);

        // The tab bar still lays the button out and handles the click; it is only drawn invisibly.
        ImGui.pushStyleColor(ImGuiCol.Tab, 0);
        ImGui.pushStyleColor(ImGuiCol.TabHovered, 0);
        ImGui.pushStyleColor(ImGuiCol.TabSelected, 0);
        ImGui.pushStyleColor(ImGuiCol.Text, 0);
        boolean clicked = ImGui.tabItemButton(ADD_LABEL, ImGuiTabItemFlags.Trailing | ImGuiTabItemFlags.NoTooltip);
        ImGui.popStyleColor(4);

        float x = Math.round((ImGui.getItemRectMinX() + ImGui.getItemRectMaxX()) / 2f);
        float y = textCenterY();
        float radius = ImGui.getFontSize() / 2f + 1f;

        ImDrawList draw = ImGui.getWindowDrawList();
        draw.addCircleFilled(x, y, radius, resting);
        if (ImGui.isItemHovered()) draw.addCircleFilled(x, y, radius, hovering);
        drawIconCentered(draw, Icons.ADD, x, y);
        return clicked;
    }

    private static void drawIconCentered(ImDrawList draw, String icon, float x, float y) {
        int size = Math.round(ImGui.getFontSize() * ICON_SIZE_PER_FONT_SIZE);
        float scale = size / Icons.FONT_SIZE;
        ImVec2 box = ImGui.calcTextSize(icon);
        draw.addText(ImGui.getFont(), size, Math.round(x - box.x * scale / 2f), Math.round(y - box.y * scale / 2f),
            ImGui.getColorU32(ImGuiCol.Text), icon);
    }

    /** The vertical middle of the text of the item just submitted, which is not the middle of its whole rectangle. */
    private static float textCenterY() {
        return ImGui.getItemRectMinY() + ImGui.getStyle().getFramePaddingY() + ImGui.getFontSize() / 2f;
    }

    private static boolean isMouseWithin(float x, float y, float radius) {
        float dx = ImGui.getMousePosX() - x;
        float dy = ImGui.getMousePosY() - y;
        return dx * dx + dy * dy <= radius * radius;
    }
}
