package lex.folio.scene;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import lex.folio.assets.AssetLibrary;
import lex.folio.model.Layer;
import lex.folio.model.Room;
import lex.folio.model.Sprite;
import lex.folio.model.SpriteLayer;

import java.util.ArrayList;
import java.util.List;

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
        drawTexturedSprites(getVisibleSpriteLayers(room));
    }

    private List<SpriteLayer> getVisibleSpriteLayers(Room room) {
        List<SpriteLayer> result = new ArrayList<>();

        for (Layer<?> layer : room.getLayers()) {
            if (layer.isVisible() && layer instanceof SpriteLayer spriteLayer) {
                result.add(spriteLayer);
            }
        }
        return result;
    }

    private void drawTexturedSprites(List<SpriteLayer> layers) {
        spriteBatch.begin();

        for (SpriteLayer layer : layers) {
            for (Sprite sprite : layer.getItems()) {
                TextureRegion region = assetLibrary.findRegion(sprite.getAssetId());

                if (region != null) {
                    drawTexture(sprite, region, layer.getOpacity());
                }
            }
        }
        spriteBatch.end();
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
