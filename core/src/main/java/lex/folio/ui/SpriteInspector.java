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
    private static final String DECIMAL_FORMAT = "%.2f";

    private final CommandStack commandStack;
    private final float[] twoFloats = new float[2];
    private final ImFloat oneFloat = new ImFloat();

    private Vector2 pendingPosition;
    private float pendingRotation;
    private Vector2 pendingScale;

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
        twoFloats[0] = sprite.getX();
        twoFloats[1] = sprite.getY();

        if (ImGui.inputFloat2("Position", twoFloats, DECIMAL_FORMAT)) {
            pendingPosition = new Vector2(twoFloats[0], twoFloats[1]);
        }
        if (ImGui.isItemDeactivatedAfterEdit()) {
            Vector2 oldPosition = new Vector2(sprite.getX(), sprite.getY());
            commit(v -> sprite.setPosition(v.x, v.y), oldPosition, pendingPosition);
        }
    }

    private void drawRotation(Sprite sprite) {
        oneFloat.set(sprite.getRotationDegrees());

        if (ImGui.inputFloat("Rotation", oneFloat, 0, 0, DECIMAL_FORMAT)) {
            pendingRotation = oneFloat.get();
        }
        if (ImGui.isItemDeactivatedAfterEdit()) {
            commit(sprite::setRotationDegrees, sprite.getRotationDegrees(), pendingRotation);
        }
    }

    private void drawScale(Sprite sprite) {
        twoFloats[0] = sprite.getScaleX();
        twoFloats[1] = sprite.getScaleY();

        if (ImGui.inputFloat2("Scale", twoFloats, DECIMAL_FORMAT)) {
            pendingScale = new Vector2(twoFloats[0], twoFloats[1]);
        }
        if (ImGui.isItemDeactivatedAfterEdit()) {
            Vector2 oldScale = new Vector2(sprite.getScaleX(), sprite.getScaleY());
            commit(v -> sprite.setScale(v.x, v.y), oldScale, pendingScale);
        }
    }

    private void drawFlip(Sprite sprite) {
        boolean flipX = sprite.isFlipX();
        if (ImGui.checkbox("Flip X", flipX)) {
            commit(sprite::setFlipX, flipX, !flipX);
        }

        ImGui.sameLine();

        boolean flipY = sprite.isFlipY();
        if (ImGui.checkbox("Flip Y", flipY)) {
            commit(sprite::setFlipY, flipY, !flipY);
        }
    }

    private <T> void commit(Consumer<T> setter, T oldValue, T newValue) {
        if (Objects.equals(oldValue, newValue)) return;
        commandStack.execute(new SetValueCommand<>(setter, oldValue, newValue));
    }
}
