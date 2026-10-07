package lex.folio.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public abstract sealed class Layer<T extends RoomObject> permits SpriteLayer, CollisionLayer {
    private final int id;
    private String name;
    private boolean visible = true;
    private boolean locked;
    private final List<T> items = new ArrayList<>();
    private final List<T> readOnlyItems = Collections.unmodifiableList(items);

    protected Layer(int id, String name) {
        this.id = id;
        setName(name);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNull(name, "name");
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    /** Whether tools may pick or place objects on this layer. */
    public boolean isEditable() {
        return visible && !locked;
    }

    public List<T> getItems() {
        return readOnlyItems;
    }

    public void add(T item) {
        items.add(Objects.requireNonNull(item, "item"));
    }

    /** Inserts the item so that it ends up at the index, which is where it is in the draw order. */
    public void add(int index, T item) {
        items.add(index, Objects.requireNonNull(item, "item"));
    }

    public void remove(T item) {
        items.remove(item);
    }

    public boolean contains(RoomObject object) {
        return items.contains(object);
    }

    /** Replaces the draw order, which must hold the same items as now. */
    public void setOrder(List<T> order) {
        if (order.size() != items.size() || !items.containsAll(order)) {
            throw new IllegalArgumentException("The new order must have the same items");
        }
        List<T> copy = List.copyOf(order);
        items.clear();
        items.addAll(copy);
    }
}
