package lex.folio.command;

import java.util.Objects;
import java.util.function.Consumer;

public final class SetValueCommand<T> implements Command {
    private final Consumer<T> setter;
    private final T oldValue;
    private final T newValue;

    public SetValueCommand(Consumer<T> setter, T oldValue, T newValue) {
        this.setter = Objects.requireNonNull(setter, "setter");
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    @Override
    public void execute() {
        setter.accept(newValue);
    }

    @Override
    public void undo() {
        setter.accept(oldValue);
    }
}
