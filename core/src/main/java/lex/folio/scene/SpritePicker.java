package lex.folio.scene;

import lex.folio.model.Layer;
import lex.folio.model.Room;
import lex.folio.model.Sprite;
import lex.folio.model.SpriteLayer;

import java.util.List;

public class SpritePicker {
    private final SpriteGeometry geometry;

    public SpritePicker(SpriteGeometry geometry) {
        this.geometry = geometry;
    }

    public Sprite findSpriteAt(Room room, float worldX, float worldY) {
        List<Layer<?>> layers = room.getLayers();

        for (int i = layers.size() - 1; i >= 0; i--) {
            if (layers.get(i) instanceof SpriteLayer layer && isPickable(layer)) {
                Sprite sprite = findSpriteAt(layer, worldX, worldY);

                if (sprite != null) return sprite;
            }
        }
        return null;
    }

    private Sprite findSpriteAt(SpriteLayer layer, float worldX, float worldY) {
        List<Sprite> sprites = layer.getItems();

        for (int i = sprites.size() - 1; i >= 0; i--) {
            Sprite sprite = sprites.get(i);

            if (geometry.contains(sprite, worldX, worldY)) return sprite;
        }
        return null;
    }

    private static boolean isPickable(Layer<?> layer) {
        return layer.isVisible() && !layer.isLocked();
    }
}
