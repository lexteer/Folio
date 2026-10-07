package lex.folio.command;

import lex.folio.model.Layer;
import lex.folio.model.Room;
import lex.folio.model.RoomObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** Removes objects from the layers of a room, and puts them back at the same places in the draw order on undo. */
public final class DeleteObjectsCommand implements Command {
    private record Removal<T extends RoomObject>(Layer<T> layer, T item, int index) {
        void remove() {
            layer.remove(item);
        }

        void putBack() {
            layer.add(index, item);
        }
    }

    private final List<Removal<?>> removals = new ArrayList<>();

    /** Returns null if none of the objects is on an editable layer of the room, so there is nothing to delete. */
    public static DeleteObjectsCommand of(Room room, Collection<? extends RoomObject> objects) {
        DeleteObjectsCommand command = new DeleteObjectsCommand();
        for (Layer<?> layer : room.getLayers()) {
            if (layer.isEditable()) command.addRemovals(layer, objects);
        }
        return command.removals.isEmpty() ? null : command;
    }

    private <T extends RoomObject> void addRemovals(Layer<T> layer, Collection<? extends RoomObject> objects) {
        List<T> items = layer.getItems();
        for (int index = 0; index < items.size(); index++) {
            if (objects.contains(items.get(index))) {
                removals.add(new Removal<>(layer, items.get(index), index));
            }
        }
    }

    private DeleteObjectsCommand() {
    }

    @Override
    public void execute() {
        for (Removal<?> removal : removals) {
            removal.remove();
        }
    }

    @Override
    public void undo() {
        // Putting back from the lowest index up restores every original index.
        for (Removal<?> removal : removals) {
            removal.putBack();
        }
    }
}
