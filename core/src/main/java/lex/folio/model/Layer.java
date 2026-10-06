package lex.folio.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public abstract sealed class Layer<T extends RoomObject> permits SpriteLayer {
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

    public void remove(T item) {
        items.remove(item);
    }
}
