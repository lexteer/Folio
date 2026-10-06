package lex.folio.ui;

import com.badlogic.gdx.math.Vector2;
import imgui.ImGui;
import imgui.type.ImFloat;
import lex.folio.command.CommandStack;
import lex.folio.command.SetValueCommand;
import lex.folio.model.Sprite;

import java.util.Objects;
import java.util.function.Consumer;

public class SpriteInspector {
    private static final String DECIMAL_FORMAT = "%.3f";
    private static final float LABEL_WIDTH_IN_FONT_SIZES = 6f;

    private final CommandStack commandStack;
    private final ImFloat fieldValue = new ImFloat();
    private float pendingValue;
    private float rowFieldsStartX;
    private float rowFieldsWidth;

    public SpriteInspector(CommandStack commandStack) {
        this.commandStack = commandStack;
    }

    public void draw(Sprite sprite) {
        drawPosition(sprite);
        drawRotation(sprite);
        drawScale(sprite);
        drawFlip(sprite);
    }

    private void drawPosition(Sprite sprite) {
        beginRow("Position");
        drawAxisField("X", sprite.getX(), x -> commitPosition(sprite, x, sprite.getY()));
        ImGui.sameLine();
        drawAxisField("Y", sprite.getY(), y -> commitPosition(sprite, sprite.getX(), y));
        endRow();
    }

    private void drawRotation(Sprite sprite) {
        beginRow("Rotation");
        skipAxisLabel();
        ImGui.setNextItemWidth(ImGui.getContentRegionAvailX());
        drawFloatInput("##rotation", sprite.getRotationDegrees(),
            degrees -> commit(sprite::setRotationDegrees, sprite.getRotationDegrees(), degrees));
        endRow();
    }

    private void drawScale(Sprite sprite) {
        beginRow("Scale");
        drawAxisField("X", sprite.getScaleX(), x -> commitScale(sprite, x, sprite.getScaleY()));
        ImGui.sameLine();
        drawAxisField("Y", sprite.getScaleY(), y -> commitScale(sprite, sprite.getScaleX(), y));
        endRow();
    }

    private void drawFlip(Sprite sprite) {
        beginRow("Flip");
        drawAxisLabel("X");
        boolean flipX = sprite.isFlipX();
        if (ImGui.checkbox("##X", flipX)) {
            commit(sprite::setFlipX, flipX, !flipX);
        }
        ImGui.sameLine();
        drawAxisLabel("Y");
        boolean flipY = sprite.isFlipY();
        if (ImGui.checkbox("##Y", flipY)) {
            commit(sprite::setFlipY, flipY, !flipY);
        }
        endRow();
    }

    private void beginRow(String label) {
        ImGui.pushID(label);
        ImGui.alignTextToFramePadding();
        ImGui.text(label);
        ImGui.sameLine(ImGui.getFontSize() * LABEL_WIDTH_IN_FONT_SIZES);
        rowFieldsWidth = ImGui.getContentRegionAvailX();
    }

    private void moveToSecondHalf() {
        ImGui.sameLine(rowFieldsStartX + getHalfRowWidth() + getSpacing());
    }

    private void drawAxisLabel(String axis) {
        ImGui.text(axis);
        ImGui.sameLine();
    }

    private void skipAxisLabel() {
        ImGui.setCursorPosX(ImGui.getCursorPosX() + ImGui.calcTextSizeX("X") + getSpacing());
    }

    private void drawAxisField(String axis, float value, Consumer<Float> onCommit) {
        drawAxisLabel(axis);
        ImGui.setNextItemWidth(getAxisFieldWidth(axis));
        drawFloatInput("##" + axis, value, onCommit);
    }

    private float getAxisFieldWidth(String axis) {
        return getHalfRowWidth() - ImGui.calcTextSizeX(axis) - getSpacing();
    }

    private float getHalfRowWidth() {
        return (rowFieldsWidth - getSpacing()) / 2f;
    }

    private float getSpacing() {
        return ImGui.getStyle().getItemSpacingX();
    }

    private void endRow() {
        ImGui.popID();
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

    private void commitPosition(Sprite sprite, float x, float y) {
        Vector2 oldPosition = new Vector2(sprite.getX(), sprite.getY());
        commit(v -> sprite.setPosition(v.x, v.y), oldPosition, new Vector2(x, y));
    }

    private void commitScale(Sprite sprite, float x, float y) {
        Vector2 oldScale = new Vector2(sprite.getScaleX(), sprite.getScaleY());
        commit(v -> sprite.setScale(v.x, v.y), oldScale, new Vector2(x, y));
    }

    private <T> void commit(Consumer<T> setter, T oldValue, T newValue) {
        if (Objects.equals(oldValue, newValue)) return;
        commandStack.execute(new SetValueCommand<>(setter, oldValue, newValue));
    }
}
