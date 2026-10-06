package lex.folio.ui.scene;

import imgui.ImDrawList;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiMouseButton;
import imgui.flag.ImGuiTabItemFlags;

/** The round buttons of the room tab bar: the close button on each tab and the one that adds a room. */
final class TabButtons {
    private static final float CLOSE_SIZE_PER_FONT_SIZE = 0.75f;
    private static final String ADD_LABEL = "   ";

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

        float diameter = closeDiameter();
        float radius = diameter / 2f;
        float x = ImGui.getItemRectMaxX() - ImGui.getStyle().getFramePaddingX() - radius;
        float y = (ImGui.getItemRectMinY() + ImGui.getItemRectMaxY()) / 2f;
        boolean hovered = ImGui.isItemHovered() && isMouseWithin(x, y, radius);

        ImDrawList draw = ImGui.getWindowDrawList();
        int textColor = ImGui.getColorU32(ImGuiCol.Text);
        if (hovered) {
            draw.addCircleFilled(x, y, radius, ImGui.getColorU32(ImGuiCol.ButtonHovered));
        }
        if (unsaved && !hovered) {
            draw.addCircleFilled(x, y, radius * 0.4f, textColor);
        } else {
            float arm = radius * 0.45f;
            draw.addLine(x - arm, y - arm, x + arm, y + arm, textColor, 1.2f);
            draw.addLine(x - arm, y + arm, x + arm, y - arm, textColor, 1.2f);
        }
        return hovered && ImGui.isMouseClicked(ImGuiMouseButton.Left);
    }

    /** Draws the add button as a circle, which lights up under the mouse. Returns whether it was clicked. */
    static boolean drawAdd() {
        int resting = ImGui.getColorU32(ImGuiCol.Tab);
        int hovering = ImGui.getColorU32(ImGuiCol.TabHovered);
        int textColor = ImGui.getColorU32(ImGuiCol.Text);

        // The tab bar still lays the button out and handles the click; it is only drawn invisibly.
        ImGui.pushStyleColor(ImGuiCol.Tab, 0);
        ImGui.pushStyleColor(ImGuiCol.TabHovered, 0);
        ImGui.pushStyleColor(ImGuiCol.TabSelected, 0);
        ImGui.pushStyleColor(ImGuiCol.Text, 0);
        boolean clicked = ImGui.tabItemButton(ADD_LABEL, ImGuiTabItemFlags.Trailing | ImGuiTabItemFlags.NoTooltip);
        ImGui.popStyleColor(4);

        float width = ImGui.getItemRectMaxX() - ImGui.getItemRectMinX();
        float height = ImGui.getItemRectMaxY() - ImGui.getItemRectMinY();
        float x = (ImGui.getItemRectMinX() + ImGui.getItemRectMaxX()) / 2f;
        float y = (ImGui.getItemRectMinY() + ImGui.getItemRectMaxY()) / 2f;
        float radius = Math.min(width, height) / 2f - 1f;

        ImDrawList draw = ImGui.getWindowDrawList();
        draw.addCircleFilled(x, y, radius, ImGui.isItemHovered() ? hovering : resting);
        float arm = radius * 0.45f;
        draw.addLine(x - arm, y, x + arm, y, textColor, 1.2f);
        draw.addLine(x, y - arm, x, y + arm, textColor, 1.2f);
        return clicked;
    }

    private static boolean isMouseWithin(float x, float y, float radius) {
        float dx = ImGui.getMousePosX() - x;
        float dy = ImGui.getMousePosY() - y;
        return dx * dx + dy * dy <= radius * radius;
    }
}
