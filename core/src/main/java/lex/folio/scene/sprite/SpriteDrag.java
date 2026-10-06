package lex.folio.scene.sprite;

import com.badlogic.gdx.math.Vector2;
import lex.folio.command.Command;
import lex.folio.command.CommandGroup;
import lex.folio.command.CommandStack;
import lex.folio.command.SetValueCommand;
import lex.folio.model.Sprite;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Moves one or more sprites together with the mouse. A finished drag is a single undo step. */
public class SpriteDrag {
    private final CommandStack commandStack;
    private final Map<Sprite, Vector2> startPositions = new LinkedHashMap<>();
    private float grabX;
    private float grabY;

    public SpriteDrag(CommandStack commandStack) {
        this.commandStack = commandStack;
    }

    public boolean isActive() {
        return !startPositions.isEmpty();
    }

    public void start(Collection<Sprite> sprites, float worldX, float worldY) {
        startPositions.clear();
        for (Sprite sprite : sprites) {
            startPositions.put(sprite, new Vector2(sprite.getX(), sprite.getY()));
        }
        grabX = worldX;
        grabY = worldY;
    }

    public void moveTo(float worldX, float worldY) {
        float deltaX = worldX - grabX;
        float deltaY = worldY - grabY;
        startPositions.forEach((sprite, start) -> sprite.setPosition(start.x + deltaX, start.y + deltaY));
    }

    /** Returns whether the sprites ended up somewhere else than they started. */
    public boolean finish() {
        List<Command> moves = new ArrayList<>();
        startPositions.forEach((sprite, start) -> {
            Vector2 end = new Vector2(sprite.getX(), sprite.getY());
            if (!end.equals(start)) {
                moves.add(new SetValueCommand<>(v -> sprite.setPosition(v.x, v.y), start, end));
            }
        });
        startPositions.clear();

        if (moves.isEmpty()) return false;

        commandStack.execute(new CommandGroup(moves));
        return true;
    }

    public void cancel() {
        startPositions.forEach((sprite, start) -> sprite.setPosition(start.x, start.y));
        startPositions.clear();
    }
}
