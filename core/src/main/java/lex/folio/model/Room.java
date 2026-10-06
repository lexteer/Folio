package lex.folio.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Room {
    private String name;
    private float anchorX;
    private float anchorY;
    private int nextId = 1;
    private final List<Layer<?>> layers = new ArrayList<>();
    private final List<Layer<?>> readOnlyLayers = Collections.unmodifiableList(layers);

    public Room(String name) {
        setName(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNull(name, "name");
    }

    public float getAnchorX() {
        return anchorX;
    }

    public float getAnchorY() {
        return anchorY;
    }

    public void setAnchor(float anchorX, float anchorY) {
        this.anchorX = anchorX;
        this.anchorY = anchorY;
    }

    public int getNextId() {
        return nextId;
    }

    public void setNextId(int nextId) {
        if (nextId < 1) throw new IllegalArgumentException("nextId must be positive, was " + nextId);
        this.nextId = nextId;
    }

    public int createId() {
        return nextId++;
    }

    public List<Layer<?>> getLayers() {
        return readOnlyLayers;
    }

    /** The sprite layers in draw order, bottom first. */
    public List<SpriteLayer> getSpriteLayers() {
        List<SpriteLayer> result = new ArrayList<>();
        for (Layer<?> layer : layers) {
            if (layer instanceof SpriteLayer spriteLayer) {
                result.add(spriteLayer);
            }
        }
        return result;
    }

    public void addLayer(Layer<?> layer) {
        layers.add(Objects.requireNonNull(layer, "layer"));
    }

    public void removeLayer(Layer<?> layer) {
        layers.remove(layer);
    }
}
