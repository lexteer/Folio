package lex.folio.command;

import lex.folio.model.Layer;
import lex.folio.model.Room;
import lex.folio.model.RoomObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/** Changes the draw order of objects within their layers. Items never move to another layer. */
public final class ReorderItemsCommand implements Command {
    private record Change<T extends RoomObject>(Layer<T> layer, List<T> before, List<T> after) {
        void use(boolean forward) {
            layer.setOrder(forward ? after : before);
        }
    }

    private final List<Change<?>> changes;

    /** Returns null if nothing on an editable layer would change, so there is nothing to do. */
    public static ReorderItemsCommand of(Room room, Collection<? extends RoomObject> objects, ItemOrder order) {
        Set<RoomObject> selected = Set.copyOf(objects);
        List<Change<?>> changes = new ArrayList<>();
        for (Layer<?> layer : room.getLayers()) {
            if (!layer.isEditable()) continue;

            addChange(changes, layer, selected, order);
        }
        return changes.isEmpty() ? null : new ReorderItemsCommand(changes);
    }

    private static <T extends RoomObject> void addChange(List<Change<?>> changes, Layer<T> layer,
                                                         Set<RoomObject> selected, ItemOrder order) {
        List<T> before = List.copyOf(layer.getItems());
        List<T> after = order.apply(before, selected);
        if (!before.equals(after)) changes.add(new Change<>(layer, before, after));
    }

    private ReorderItemsCommand(List<Change<?>> changes) {
        this.changes = changes;
    }

    @Override
    public void execute() {
        for (Change<?> change : changes) {
            change.use(true);
        }
    }

    @Override
    public void undo() {
        for (Change<?> change : changes) {
            change.use(false);
        }
    }
}
