package lex.folio.scene.sprite;

import com.badlogic.gdx.math.Vector2;
import lex.folio.command.CommandStack;
import lex.folio.command.SetValueCommand;
import lex.folio.model.Sprite;

public class SpriteDrag {
    private final CommandStack commandStack;
    private final Vector2 startPosition = new Vector2();
    private final Vector2 grabOffset = new Vector2();
    private Sprite sprite;

    public SpriteDrag(CommandStack commandStack) {
        this.commandStack = commandStack;
    }

    public boolean isActive() {
        return sprite != null;
    }

    public void start(Sprite sprite, float worldX, float worldY) {
        this.sprite = sprite;
        startPosition.set(sprite.getX(), sprite.getY());
        grabOffset.set(worldX - sprite.getX(), worldY - sprite.getY());
    }

    public void moveTo(float worldX, float worldY) {
        if (!isActive()) return;
        sprite.setPosition(worldX - grabOffset.x, worldY - grabOffset.y);
    }

    public void finish() {
        if (!isActive()) return;

        Vector2 endPosition = new Vector2(sprite.getX(), sprite.getY());
        if (!endPosition.equals(startPosition)) {
            commitMove(sprite, startPosition.cpy(), endPosition);
        }
        sprite = null;
    }

    private void commitMove(Sprite movedSprite, Vector2 from, Vector2 to) {
        commandStack.execute(new SetValueCommand<>(v -> movedSprite.setPosition(v.x, v.y), from, to));
    }

    public void cancel() {
        if (!isActive()) return;

        sprite.setPosition(startPosition.x, startPosition.y);
        sprite = null;
    }
}
