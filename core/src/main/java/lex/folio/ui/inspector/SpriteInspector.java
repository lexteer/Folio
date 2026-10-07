package lex.folio.ui.inspector;

import com.badlogic.gdx.math.Vector2;
import lex.folio.command.CommandStack;
import lex.folio.model.Sprite;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * Edits the properties of the selected sprites. The first one's values are shown, and an edit is applied to all of
 * them as one undoable command.
 */
public class SpriteInspector {
    private final CommandStack commandStack;
    private final PropertyRow row = new PropertyRow();

    public SpriteInspector(CommandStack commandStack) {
        this.commandStack = commandStack;
    }

    public void draw(List<Sprite> sprites) {
        List<Sprite> targets = List.copyOf(sprites);
        Sprite shown = targets.getFirst();
        drawPosition(targets, shown);
        drawRotation(targets, shown);
        drawScale(targets, shown);
        drawFlip(targets, shown);
    }

    private void drawPosition(List<Sprite> targets, Sprite shown) {
        row.begin("Position");
        row.drawAxisField("X", shown.getX(), x ->
            commit(targets, SpriteInspector::getPosition, SpriteInspector::setPosition,
                v -> new Vector2(x, v.y)));
        row.sameLine();
        row.drawAxisField("Y", shown.getY(), y ->
            commit(targets, SpriteInspector::getPosition, SpriteInspector::setPosition,
                v -> new Vector2(v.x, y)));
        row.end();
    }

    private void drawRotation(List<Sprite> targets, Sprite shown) {
        row.begin("Rotation");
        row.drawWideField("rotation", shown.getRotationDegrees(), degrees ->
            commit(targets, Sprite::getRotationDegrees, Sprite::setRotationDegrees, old -> degrees));
        row.end();
    }

    private void drawScale(List<Sprite> targets, Sprite shown) {
        row.begin("Scale");
        row.drawAxisField("X", shown.getScaleX(), x ->
            commit(targets, SpriteInspector::getScale, SpriteInspector::setScale, v -> new Vector2(x, v.y)));
        row.sameLine();
        row.drawAxisField("Y", shown.getScaleY(), y ->
            commit(targets, SpriteInspector::getScale, SpriteInspector::setScale, v -> new Vector2(v.x, y)));
        row.end();
    }

    private void drawFlip(List<Sprite> targets, Sprite shown) {
        row.begin("Flip");
        row.drawAxisCheckbox("X", shown.isFlipX(), flip ->
            commit(targets, Sprite::isFlipX, Sprite::setFlipX, old -> flip));
        row.sameLine();
        row.drawAxisCheckbox("Y", shown.isFlipY(), flip ->
            commit(targets, Sprite::isFlipY, Sprite::setFlipY, old -> flip));
        row.end();
    }

    private static Vector2 getPosition(Sprite sprite) {
        return new Vector2(sprite.getX(), sprite.getY());
    }

    private static void setPosition(Sprite sprite, Vector2 position) {
        sprite.setPosition(position.x, position.y);
    }

    private static Vector2 getScale(Sprite sprite) {
        return new Vector2(sprite.getScaleX(), sprite.getScaleY());
    }

    private static void setScale(Sprite sprite, Vector2 scale) {
        sprite.setScale(scale.x, scale.y);
    }

    private <T> void commit(List<Sprite> sprites, Function<Sprite, T> read, BiConsumer<Sprite, T> write,
                            UnaryOperator<T> change) {
        ObjectEdits.commit(commandStack, sprites, read, write, change);
    }
}
