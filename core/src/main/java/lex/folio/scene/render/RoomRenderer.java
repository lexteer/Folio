package lex.folio.scene.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import lex.folio.assets.AssetLibrary;
import lex.folio.model.Room;
import lex.folio.model.Sprite;
import lex.folio.model.SpriteLayer;

import lex.folio.scene.sprite.SpriteGeometry;

public class RoomRenderer {
    private final SpriteBatch spriteBatch;
    private final AssetLibrary assetLibrary;
    private final SpriteGeometry geometry;
    private final Color drawColor = new Color();

    public RoomRenderer(SpriteBatch spriteBatch, AssetLibrary assetLibrary, SpriteGeometry geometry) {
        this.spriteBatch = spriteBatch;
        this.assetLibrary = assetLibrary;
        this.geometry = geometry;
    }

    /** Expects the sprite batch's projection matrix to already be set. */
    public void render(Room room) {
        spriteBatch.begin();
        for (SpriteLayer layer : room.getSpriteLayers()) {
            if (layer.isVisible()) {
                drawLayer(layer);
            }
        }
        spriteBatch.end();
    }

    private void drawLayer(SpriteLayer layer) {
        for (Sprite sprite : layer.getItems()) {
            TextureRegion region = assetLibrary.findRegion(sprite.getAssetId());

            if (region != null) {
                drawTexture(sprite, region, layer.getOpacity());
            }
        }
    }

    private void drawTexture(Sprite sprite, TextureRegion region, float layerOpacity) {
        float width = geometry.getWidth(sprite);
        float height = geometry.getHeight(sprite);
        float flipX = sprite.isFlipX() ? -1f : 1f;
        float flipY = sprite.isFlipY() ? -1f : 1f;

        Color.rgba8888ToColor(drawColor, sprite.getTint());
        drawColor.a *= layerOpacity;
        spriteBatch.setColor(drawColor);

        spriteBatch.draw(region,
            sprite.getX() - width / 2f, sprite.getY() - height / 2f,
            width / 2f, height / 2f,
            width, height,
            sprite.getScaleX() * flipX, sprite.getScaleY() * flipY,
            sprite.getRotationDegrees()
        );
    }
}
