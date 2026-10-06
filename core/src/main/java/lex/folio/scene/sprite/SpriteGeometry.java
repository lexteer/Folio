package lex.folio.scene.sprite;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import lex.folio.assets.AssetLibrary;
import lex.folio.model.Sprite;

public class SpriteGeometry {
    private static final float PLACEHOLDER_SIZE = 1f;

    private final AssetLibrary assetLibrary;
    private final float pixelsPerMeter;

    public SpriteGeometry(AssetLibrary assetLibrary, float pixelsPerMeter) {
        this.assetLibrary = assetLibrary;
        this.pixelsPerMeter = pixelsPerMeter;
    }

    public float getWidth(Sprite sprite) {
        TextureRegion region = assetLibrary.findRegion(sprite.getAssetId());
        return region == null ? PLACEHOLDER_SIZE : toMeters(region.getRegionWidth());
    }

    public float getHeight(Sprite sprite) {
        TextureRegion region = assetLibrary.findRegion(sprite.getAssetId());
        return region == null ? PLACEHOLDER_SIZE : toMeters(region.getRegionHeight());
    }

    /** Whether the sprite has no texture and is drawn as a placeholder instead. */
    public boolean isPlaceholder(Sprite sprite) {
        return assetLibrary.findRegion(sprite.getAssetId()) == null;
    }

    private float toMeters(int pixels) {
        return pixels / pixelsPerMeter;
    }

    public Vector2 toWorld(Sprite sprite, float localX, float localY) {
        float x = sprite.isFlipX() ? -localX : localX;
        float y = sprite.isFlipY() ? -localY : localY;

        Vector2 point = new Vector2(x * sprite.getScaleX(), y * sprite.getScaleY());
        point.rotateDeg(sprite.getRotationDegrees());
        return point.add(sprite.getX(), sprite.getY());
    }

    /** Whether the sprite's outline touches the axis aligned world rectangle. */
    public boolean overlaps(Sprite sprite, float minX, float minY, float maxX, float maxY) {
        float halfWidth = getWidth(sprite) / 2f;
        float halfHeight = getHeight(sprite) / 2f;
        Vector2[] corners = {
            toWorld(sprite, -halfWidth, -halfHeight), toWorld(sprite, halfWidth, -halfHeight),
            toWorld(sprite, halfWidth, halfHeight), toWorld(sprite, -halfWidth, halfHeight)
        };

        // Separating axis test: the rectangle's two axes, then the sprite's two edge directions.
        float spriteMinX = Float.MAX_VALUE, spriteMinY = Float.MAX_VALUE;
        float spriteMaxX = -Float.MAX_VALUE, spriteMaxY = -Float.MAX_VALUE;
        for (Vector2 corner : corners) {
            spriteMinX = Math.min(spriteMinX, corner.x);
            spriteMaxX = Math.max(spriteMaxX, corner.x);
            spriteMinY = Math.min(spriteMinY, corner.y);
            spriteMaxY = Math.max(spriteMaxY, corner.y);
        }
        if (spriteMaxX < minX || spriteMinX > maxX || spriteMaxY < minY || spriteMinY > maxY) return false;

        float[] rectX = {minX, maxX, maxX, minX};
        float[] rectY = {minY, minY, maxY, maxY};
        for (int i = 0; i < 2; i++) {
            Vector2 edge = corners[i + 1].cpy().sub(corners[i]);
            if (edge.isZero()) continue;

            float axisX = -edge.y;
            float axisY = edge.x;
            float spriteLow = Float.MAX_VALUE, spriteHigh = -Float.MAX_VALUE;
            for (Vector2 corner : corners) {
                float projection = corner.x * axisX + corner.y * axisY;
                spriteLow = Math.min(spriteLow, projection);
                spriteHigh = Math.max(spriteHigh, projection);
            }
            float rectLow = Float.MAX_VALUE, rectHigh = -Float.MAX_VALUE;
            for (int j = 0; j < 4; j++) {
                float projection = rectX[j] * axisX + rectY[j] * axisY;
                rectLow = Math.min(rectLow, projection);
                rectHigh = Math.max(rectHigh, projection);
            }
            if (spriteHigh < rectLow || spriteLow > rectHigh) return false;
        }
        return true;
    }

    public boolean contains(Sprite sprite, float worldX, float worldY) {
        if (sprite.getScaleX() == 0 || sprite.getScaleY() == 0) return false;

        Vector2 local = new Vector2(worldX - sprite.getX(), worldY - sprite.getY());
        local.rotateDeg(-sprite.getRotationDegrees());

        float unscaledX = local.x / sprite.getScaleX();
        float unscaledY = local.y / sprite.getScaleY();
        return Math.abs(unscaledX) <= getWidth(sprite) / 2f && Math.abs(unscaledY) <= getHeight(sprite) / 2f;
    }
}
