package lex.folio.command;

import lex.folio.model.Layer;
import lex.folio.model.RoomObject;

public final class AddObjectCommand<T extends RoomObject> implements Command {
    private final Layer<T> layer;
    private final T object;

    public AddObjectCommand(Layer<T> layer, T object) {
        this.layer = layer;
        this.object = object;
    }

    @Override
    public void execute() {
        layer.add(object);
    }

    @Override
    public void undo() {
        layer.remove(object);
    }
}
