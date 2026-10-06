package lex.folio.command;

import lex.folio.model.Room;
import lex.folio.model.RoomObject;
import lex.folio.model.Sprite;
import lex.folio.model.SpriteLayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** Removes objects from the layers of a room, and puts them back at the same places in the draw order on undo. */
public final class DeleteObjectsCommand implements Command {
    private record Removal(SpriteLayer layer, Sprite sprite, int index) {
    }

    private final List<Removal> removals = new ArrayList<>();

    /** Returns null if none of the objects is on an editable layer of the room, so there is nothing to delete. */
    public static DeleteObjectsCommand of(Room room, Collection<? extends RoomObject> objects) {
        DeleteObjectsCommand command = new DeleteObjectsCommand();
        for (SpriteLayer layer : room.getSpriteLayers()) {
            if (!layer.isEditable()) continue;

            List<Sprite> sprites = layer.getItems();
            for (int index = 0; index < sprites.size(); index++) {
                if (objects.contains(sprites.get(index))) {
                    command.removals.add(new Removal(layer, sprites.get(index), index));
                }
            }
        }
        return command.removals.isEmpty() ? null : command;
    }

    private DeleteObjectsCommand() {
    }

    @Override
    public void execute() {
        for (Removal removal : removals) {
            removal.layer().remove(removal.sprite());
        }
    }

    @Override
    public void undo() {
        // Putting back from the lowest index up restores every original index.
        for (Removal removal : removals) {
            removal.layer().add(removal.index(), removal.sprite());
        }
    }
}
