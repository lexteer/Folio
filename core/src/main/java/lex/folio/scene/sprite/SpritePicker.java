package lex.folio.scene.sprite;

import lex.folio.model.Room;
import lex.folio.model.Sprite;
import lex.folio.model.SpriteLayer;

import java.util.ArrayList;
import java.util.List;

/** Finds the topmost sprite under a world position. */
public class SpritePicker {
    private final SpriteGeometry geometry;

    public SpritePicker(SpriteGeometry geometry) {
        this.geometry = geometry;
    }

    public Sprite findSpriteAt(Room room, float worldX, float worldY) {
        for (SpriteLayer layer : room.getSpriteLayers().reversed()) {
            if (!layer.isEditable()) continue;

            Sprite sprite = findSpriteAt(layer, worldX, worldY);
            if (sprite != null) return sprite;
        }
        return null;
    }

    /** All sprites touching the world rectangle, on layers that can be edited. */
    public List<Sprite> findSpritesIn(Room room, float minX, float minY, float maxX, float maxY) {
        List<Sprite> found = new ArrayList<>();
        for (SpriteLayer layer : room.getSpriteLayers()) {
            if (!layer.isEditable()) continue;

            for (Sprite sprite : layer.getItems()) {
                if (geometry.overlaps(sprite, minX, minY, maxX, maxY)) found.add(sprite);
            }
        }
        return found;
    }

    private Sprite findSpriteAt(SpriteLayer layer, float worldX, float worldY) {
        List<Sprite> sprites = layer.getItems();

        for (int i = sprites.size() - 1; i >= 0; i--) {
            Sprite sprite = sprites.get(i);

            if (geometry.contains(sprite, worldX, worldY)) return sprite;
        }
        return null;
    }
}
