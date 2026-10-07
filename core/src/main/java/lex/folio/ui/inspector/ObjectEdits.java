package lex.folio.ui.inspector;

import lex.folio.command.Command;
import lex.folio.command.CommandGroup;
import lex.folio.command.CommandStack;
import lex.folio.command.SetValueCommand;
import lex.folio.model.RoomObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/** Changes a property of many objects at once. */
final class ObjectEdits {
    private ObjectEdits() {
    }

    /** Changes the property of every object, each from its own current value, as a single undo step. */
    static <O extends RoomObject, T> void commit(CommandStack commandStack, List<O> objects, Function<O, T> read,
                                                 BiConsumer<O, T> write, UnaryOperator<T> change) {
        List<Command> edits = new ArrayList<>();
        for (O object : objects) {
            T oldValue = read.apply(object);
            T newValue = change.apply(oldValue);
            if (!Objects.equals(oldValue, newValue)) {
                edits.add(new SetValueCommand<>(v -> write.accept(object, v), oldValue, newValue));
            }
        }
        if (edits.isEmpty()) return;

        commandStack.execute(new CommandGroup(edits));
    }
}
