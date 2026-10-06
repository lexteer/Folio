package lex.folio.scene;

import lex.folio.model.RoomObject;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/** The objects currently selected in the scene. */
public class Selection {
    private final Set<RoomObject> objects = new LinkedHashSet<>();
    private final Set<RoomObject> readOnlyObjects = Collections.unmodifiableSet(objects);

    public Set<RoomObject> getObjects() {
        return readOnlyObjects;
    }

    public boolean contains(RoomObject object) {
        return objects.contains(object);
    }

    public boolean isEmpty() {
        return objects.isEmpty();
    }

    public void selectOnly(RoomObject object) {
        objects.clear();
        objects.add(Objects.requireNonNull(object, "object"));
    }

    /** Adds the object if it is not selected, removes it if it is. */
    public void toggle(RoomObject object) {
        if (!objects.remove(object)) {
            objects.add(Objects.requireNonNull(object, "object"));
        }
    }

    public void replaceWith(Collection<? extends RoomObject> newObjects) {
        objects.clear();
        objects.addAll(newObjects);
    }

    public void clear() {
        objects.clear();
    }
}
