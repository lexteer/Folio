package lex.folio.scene;

import lex.folio.model.Layer;
import lex.folio.model.Room;

import java.util.Map;
import java.util.WeakHashMap;

/** The layer of each room that new objects go to. It is only remembered while the project is open. */
public class ActiveLayers {
    private final Map<Room, Layer<?>> layers = new WeakHashMap<>();

    /** The layer new objects are put on: the one that was chosen, or else the topmost. Null if the room has none. */
    public Layer<?> get(Room room) {
        Layer<?> chosen = layers.get(room);
        if (chosen != null && room.getLayers().contains(chosen)) return chosen;

        return room.getLayers().isEmpty() ? null : room.getLayers().getLast();
    }

    public void set(Room room, Layer<?> layer) {
        layers.put(room, layer);
    }
}
