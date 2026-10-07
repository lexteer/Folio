package lex.folio.scene;

import com.badlogic.gdx.math.Vector2;
import lex.folio.command.Command;
import lex.folio.command.CommandGroup;
import lex.folio.command.CommandStack;
import lex.folio.command.SetValueCommand;
import lex.folio.model.RoomObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Moves one or more objects together with the mouse. A finished drag is a single undo step. */
public class ObjectDrag {
    private final CommandStack commandStack;
    private final Map<RoomObject, Vector2> startPositions = new LinkedHashMap<>();
    private float grabX;
    private float grabY;

    public ObjectDrag(CommandStack commandStack) {
        this.commandStack = commandStack;
    }

    public boolean isActive() {
        return !startPositions.isEmpty();
    }

    public void start(Collection<RoomObject> objects, float worldX, float worldY) {
        startPositions.clear();
        for (RoomObject object : objects) {
            startPositions.put(object, new Vector2(object.getX(), object.getY()));
        }
        grabX = worldX;
        grabY = worldY;
    }

    public void moveTo(float worldX, float worldY) {
        float deltaX = worldX - grabX;
        float deltaY = worldY - grabY;
        startPositions.forEach((object, start) -> object.setPosition(start.x + deltaX, start.y + deltaY));
    }

    /** Returns whether the objects ended up somewhere else than they started. */
    public boolean finish() {
        List<Command> moves = new ArrayList<>();
        startPositions.forEach((object, start) -> {
            Vector2 end = new Vector2(object.getX(), object.getY());
            if (!end.equals(start)) {
                moves.add(new SetValueCommand<>(v -> object.setPosition(v.x, v.y), start, end));
            }
        });
        startPositions.clear();

        if (moves.isEmpty()) return false;

        commandStack.execute(new CommandGroup(moves));
        return true;
    }

    public void cancel() {
        startPositions.forEach((object, start) -> object.setPosition(start.x, start.y));
        startPositions.clear();
    }
}
