package lex.folio.ui.inspector;

import imgui.ImGui;
import imgui.type.ImFloat;

import java.util.function.Consumer;

/** Lays out one labelled row of inspector fields, and reports a field's value once its edit is finished. */
final class PropertyRow {
    private static final String DECIMAL_FORMAT = "%.3f";
    private static final float LABEL_WIDTH_IN_FONT_SIZES = 6f;

    private final ImFloat fieldValue = new ImFloat();
    private float pendingValue;
    private float fieldsWidth;

    void begin(String label) {
        ImGui.pushID(label);
        ImGui.alignTextToFramePadding();
        ImGui.text(label);
        ImGui.sameLine(ImGui.getFontSize() * LABEL_WIDTH_IN_FONT_SIZES);
        fieldsWidth = ImGui.getContentRegionAvailX();
    }

    void end() {
        ImGui.popID();
    }

    /** Two fields side by side, the second one starting on the same line as the first. */
    void drawAxisField(String axis, float value, Consumer<Float> onCommit) {
        drawAxisLabel(axis);
        ImGui.setNextItemWidth(getAxisFieldWidth(axis));
        drawFloatInput("##" + axis, value, onCommit);
    }

    /** A field that takes the whole row, lined up with the axis fields of the other rows. */
    void drawWideField(String id, float value, Consumer<Float> onCommit) {
        ImGui.setCursorPosX(ImGui.getCursorPosX() + ImGui.calcTextSizeX("X") + getSpacing());
        ImGui.setNextItemWidth(ImGui.getContentRegionAvailX());
        drawFloatInput("##" + id, value, onCommit);
    }

    void drawAxisCheckbox(String axis, boolean value, Consumer<Boolean> onToggle) {
        drawAxisLabel(axis);
        if (ImGui.checkbox("##" + axis, value)) {
            onToggle.accept(!value);
        }
    }

    void sameLine() {
        ImGui.sameLine();
    }

    private void drawAxisLabel(String axis) {
        ImGui.text(axis);
        ImGui.sameLine();
    }

    private float getAxisFieldWidth(String axis) {
        return (fieldsWidth - getSpacing()) / 2f - ImGui.calcTextSizeX(axis) - getSpacing();
    }

    private float getSpacing() {
        return ImGui.getStyle().getItemSpacingX();
    }

    private void drawFloatInput(String id, float value, Consumer<Float> onCommit) {
        fieldValue.set(value);
        if (ImGui.inputFloat(id, fieldValue, 0, 0, DECIMAL_FORMAT)) {
            pendingValue = fieldValue.get();
        }

        if (ImGui.isItemDeactivatedAfterEdit()) {
            onCommit.accept(pendingValue);
        }
    }
}
