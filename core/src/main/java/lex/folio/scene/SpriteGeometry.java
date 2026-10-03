package lex.folio.scene;

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
        return region == null ? PLACEHOLDER_SIZE : region.getRegionWidth() / pixelsPerMeter;
    }

    public float getHeight(Sprite sprite) {
        TextureRegion region = assetLibrary.findRegion(sprite.getAssetId());
        return region == null ? PLACEHOLDER_SIZE : region.getRegionHeight() / pixelsPerMeter;
    }

    public Vector2 toWorld(Sprite sprite, float localX, float localY) {
        float x = sprite.isFlipX() ? -localX : localX;
        float y = sprite.isFlipY() ? -localY : localY;

        Vector2 point = new Vector2(x * sprite.getScaleX(), y * sprite.getScaleY());
        point.rotateDeg(sprite.getRotationDegrees());
        return point.add(sprite.getX(), sprite.getY());
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
