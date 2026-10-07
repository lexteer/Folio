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

    /** The collision layers in draw order, bottom first. */
    public List<CollisionLayer> getCollisionLayers() {
        List<CollisionLayer> result = new ArrayList<>();
        for (Layer<?> layer : layers) {
            if (layer instanceof CollisionLayer collisionLayer) {
                result.add(collisionLayer);
            }
        }
        return result;
    }

    /** The layer that has the object, or null if no layer has it. */
    public Layer<?> findLayerOf(RoomObject object) {
        for (Layer<?> layer : layers) {
            if (layer.contains(object)) return layer;
        }
        return null;
    }

    /** Points the sprites that use the asset id at the new id. Returns whether there were any. */
    public boolean replaceAssetId(String oldId, String newId) {
        boolean replaced = false;
        for (SpriteLayer layer : getSpriteLayers()) {
            for (Sprite sprite : layer.getItems()) {
                if (sprite.getAssetId().equalsIgnoreCase(oldId)) {
                    sprite.setAssetId(newId);
                    replaced = true;
                }
            }
        }
        return replaced;
    }

    /** Gives the shapes that have the tag another one. Returns whether there were any. */
    public boolean replaceTag(String oldName, String newName) {
        boolean replaced = false;
        for (CollisionLayer layer : getCollisionLayers()) {
            for (CollisionShape shape : layer.getItems()) {
                if (shape.getTag().equalsIgnoreCase(oldName)) {
                    shape.setTag(newName);
                    replaced = true;
                }
            }
        }
        return replaced;
    }

    public void addLayer(Layer<?> layer) {
        layers.add(Objects.requireNonNull(layer, "layer"));
    }

    /** Inserts the layer so that it ends up at the index, which is where it is in the draw order. */
    public void addLayer(int index, Layer<?> layer) {
        layers.add(index, Objects.requireNonNull(layer, "layer"));
    }

    /** Moves the layer at one index to another, which is its index once it is out of its old place. */
    public void moveLayer(int fromIndex, int toIndex) {
        layers.add(toIndex, layers.remove(fromIndex));
    }

    public void removeLayer(Layer<?> layer) {
        layers.remove(layer);
    }
}
