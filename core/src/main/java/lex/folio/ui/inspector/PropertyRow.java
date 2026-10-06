package lex.folio.ui.inspector;

import imgui.ImGui;
import imgui.flag.ImGuiInputTextFlags;
import imgui.type.ImString;

import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;

/** Lays out one labelled row of inspector fields, and reports a field's value once its edit is finished. */
final class PropertyRow {
    private static final String DECIMAL_FORMAT = "%.3f";
    private static final float LABEL_WIDTH_IN_FONT_SIZES = 6f;

    private final ImString fieldText = new ImString(32);
    private String label = "";
    private String editedField;
    private float fieldsWidth;

    void begin(String label) {
        this.label = label;
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

    /**
     * A text field that is parsed here instead of by ImGui's number input, so a decimal point or comma always
     * works. The text is only replaced by the value while the field is not being edited.
     */
    private void drawFloatInput(String id, float value, Consumer<Float> onCommit) {
        String key = label + id;
        if (!key.equals(editedField)) {
            fieldText.set(format(value));
        }

        ImGui.inputText(id, fieldText, ImGuiInputTextFlags.AutoSelectAll);
        if (ImGui.isItemActivated()) {
            editedField = key;
        }
        if (ImGui.isItemDeactivated()) {
            editedField = null;
            if (ImGui.isItemDeactivatedAfterEdit()) {
                parse(fieldText.get()).ifPresent(onCommit);
            }
        }
    }

    private static String format(float value) {
        String text = String.format(Locale.ROOT, DECIMAL_FORMAT, value);
        return text.contains(".") ? text.replaceAll("\\.?0+$", "") : text;
    }

    private static Optional<Float> parse(String text) {
        try {
            float value = Float.parseFloat(text.trim().replace(',', '.'));
            return Float.isFinite(value) ? Optional.of(value) : Optional.empty();
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
