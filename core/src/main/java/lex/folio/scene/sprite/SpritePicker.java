package lex.folio.scene.sprite;

import lex.folio.model.Sprite;
import lex.folio.model.SpriteLayer;

import java.util.ArrayList;
import java.util.List;

/** Finds sprites of a layer by world position. */
public class SpritePicker {
    private final SpriteGeometry geometry;

    public SpritePicker(SpriteGeometry geometry) {
        this.geometry = geometry;
    }

    /** The topmost sprite of the layer under the world position, or null. */
    public Sprite findSpriteAt(SpriteLayer layer, float worldX, float worldY) {
        List<Sprite> sprites = layer.getItems();

        for (int i = sprites.size() - 1; i >= 0; i--) {
            Sprite sprite = sprites.get(i);

            if (geometry.contains(sprite, worldX, worldY)) return sprite;
        }
        return null;
    }

    /** All sprites of the layer touching the world rectangle. */
    public List<Sprite> findSpritesIn(SpriteLayer layer, float minX, float minY, float maxX, float maxY) {
        List<Sprite> found = new ArrayList<>();
        for (Sprite sprite : layer.getItems()) {
            if (geometry.overlaps(sprite, minX, minY, maxX, maxY)) found.add(sprite);
        }
        return found;
    }
}
