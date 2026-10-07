package lex.folio.scene;

import lex.folio.model.CollisionLayer;
import lex.folio.model.CollisionShape;
import lex.folio.model.Layer;
import lex.folio.model.Room;
import lex.folio.model.RoomObject;
import lex.folio.model.Sprite;
import lex.folio.model.SpriteLayer;
import lex.folio.scene.shape.ShapeGeometry;
import lex.folio.scene.sprite.SpritePicker;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleSupplier;

/** Finds objects of any kind by world position, on the layers that can be edited. */
public class ObjectPicker {
    private final SpritePicker spritePicker;
    private final DoubleSupplier tolerance;

    /** @param tolerance how far from a line in the world still counts as on it, which depends on the zoom */
    public ObjectPicker(SpritePicker spritePicker, DoubleSupplier tolerance) {
        this.spritePicker = spritePicker;
        this.tolerance = tolerance;
    }

    /** The topmost object under the world position, or null. */
    public RoomObject findAt(Room room, float worldX, float worldY) {
        for (Layer<?> layer : room.getLayers().reversed()) {
            if (!layer.isEditable()) continue;

            RoomObject found = switch (layer) {
                case SpriteLayer sprites -> spritePicker.findSpriteAt(sprites, worldX, worldY);
                case CollisionLayer shapes -> findShapeAt(shapes, worldX, worldY);
            };
            if (found != null) return found;
        }
        return null;
    }

    /** All objects touching the world rectangle. */
    public List<RoomObject> findIn(Room room, float minX, float minY, float maxX, float maxY) {
        List<RoomObject> found = new ArrayList<>();
        for (Layer<?> layer : room.getLayers()) {
            if (!layer.isEditable()) continue;

            switch (layer) {
                case SpriteLayer sprites -> found.addAll(spritePicker.findSpritesIn(sprites, minX, minY, maxX, maxY));
                case CollisionLayer shapes -> {
                    for (CollisionShape shape : shapes.getItems()) {
                        if (ShapeGeometry.overlaps(shape, minX, minY, maxX, maxY)) found.add(shape);
                    }
                }
            }
        }
        return found;
    }

    private CollisionShape findShapeAt(CollisionLayer layer, float worldX, float worldY) {
        List<CollisionShape> shapes = layer.getItems();
        for (int i = shapes.size() - 1; i >= 0; i--) {
            if (ShapeGeometry.contains(shapes.get(i), worldX, worldY, (float) tolerance.getAsDouble())) {
                return shapes.get(i);
            }
        }
        return null;
    }
}
