package lex.folio.command;

import lex.folio.model.Layer;
import lex.folio.model.Room;

/** Removes a layer with everything on it, and puts it back in the same place on undo. */
public final class RemoveLayerCommand implements Command {
    private final Room room;
    private final Layer<?> layer;
    private final int index;

    public RemoveLayerCommand(Room room, Layer<?> layer) {
        this.room = room;
        this.layer = layer;
        this.index = room.getLayers().indexOf(layer);
    }

    @Override
    public void execute() {
        room.removeLayer(layer);
    }

    @Override
    public void undo() {
        room.addLayer(index, layer);
    }
}
