package lex.folio.command;

import lex.folio.model.Room;

public final class MoveLayerCommand implements Command {
    private final Room room;
    private final int fromIndex;
    private final int toIndex;

    /** @param toIndex the index the layer has once it is out of its old place, see {@link Room#moveLayer} */
    public MoveLayerCommand(Room room, int fromIndex, int toIndex) {
        this.room = room;
        this.fromIndex = fromIndex;
        this.toIndex = toIndex;
    }

    @Override
    public void execute() {
        room.moveLayer(fromIndex, toIndex);
    }

    @Override
    public void undo() {
        room.moveLayer(toIndex, fromIndex);
    }
}
