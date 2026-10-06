package lex.folio.ui.inspector;

import com.badlogic.gdx.math.Vector2;
import lex.folio.command.CommandStack;
import lex.folio.command.SetValueCommand;
import lex.folio.model.Sprite;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/** Edits the properties of a single sprite. Every committed edit is an undoable command. */
public class SpriteInspector {
    private final CommandStack commandStack;
    private final PropertyRow row = new PropertyRow();

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
        row.begin("Position");
        row.drawAxisField("X", sprite.getX(), x ->
            commitPair(sprite::setPosition, sprite.getX(), sprite.getY(), x, sprite.getY()));
        row.sameLine();
        row.drawAxisField("Y", sprite.getY(), y ->
            commitPair(sprite::setPosition, sprite.getX(), sprite.getY(), sprite.getX(), y));
        row.end();
    }

    private void drawRotation(Sprite sprite) {
        row.begin("Rotation");
        row.drawWideField("rotation", sprite.getRotationDegrees(), degrees ->
            commit(sprite::setRotationDegrees, sprite.getRotationDegrees(), degrees));
        row.end();
    }

    private void drawScale(Sprite sprite) {
        row.begin("Scale");
        row.drawAxisField("X", sprite.getScaleX(), x ->
            commitPair(sprite::setScale, sprite.getScaleX(), sprite.getScaleY(), x, sprite.getScaleY()));
        row.sameLine();
        row.drawAxisField("Y", sprite.getScaleY(), y ->
            commitPair(sprite::setScale, sprite.getScaleX(), sprite.getScaleY(), sprite.getScaleX(), y));
        row.end();
    }

    private void drawFlip(Sprite sprite) {
        row.begin("Flip");
        row.drawAxisCheckbox("X", sprite.isFlipX(), flip -> commit(sprite::setFlipX, sprite.isFlipX(), flip));
        row.sameLine();
        row.drawAxisCheckbox("Y", sprite.isFlipY(), flip -> commit(sprite::setFlipY, sprite.isFlipY(), flip));
        row.end();
    }

    private void commitPair(BiConsumer<Float, Float> setter, float oldX, float oldY, float newX, float newY) {
        commit(v -> setter.accept(v.x, v.y), new Vector2(oldX, oldY), new Vector2(newX, newY));
    }

    private <T> void commit(Consumer<T> setter, T oldValue, T newValue) {
        if (Objects.equals(oldValue, newValue)) return;
        commandStack.execute(new SetValueCommand<>(setter, oldValue, newValue));
    }
}
