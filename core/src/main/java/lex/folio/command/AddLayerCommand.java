package lex.folio.command;

import lex.folio.model.Layer;
import lex.folio.model.Room;

public final class AddLayerCommand implements Command {
    private final Room room;
    private final Layer<?> layer;
    private final int index;

    /** @param index where in the draw order the layer goes, 0 being the bottom */
    public AddLayerCommand(Room room, Layer<?> layer, int index) {
        this.room = room;
        this.layer = layer;
        this.index = index;
    }

    @Override
    public void execute() {
        room.addLayer(index, layer);
    }

    @Override
    public void undo() {
        room.removeLayer(layer);
    }
}
