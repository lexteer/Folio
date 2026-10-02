package lex.folio.scene;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import lex.folio.model.Layer;
import lex.folio.model.Room;
import lex.folio.model.Sprite;
import lex.folio.model.SpriteLayer;

public class RoomRenderer {
    private static final float PLACEHOLDER_SIZE = 1f;
    private static final float MARKER_SIZE = 0.25f;
    private static final Color PLACEHOLDER_COLOR = new Color(0.95f, 0.45f, 0.85f, 1f);

    private final ShapeRenderer shapeRenderer;

    public RoomRenderer(ShapeRenderer shapeRenderer) {
        this.shapeRenderer = shapeRenderer;
    }

    /** Expects the shape renderer's projection matrix to already be set */
    public void render(Room room) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(PLACEHOLDER_COLOR);

        for (Layer<?> layer : room.getLayers()) {
            if (layer.isVisible() && layer instanceof SpriteLayer spriteLayer) {
                drawSpriteLayer(spriteLayer);
            }
        }
        shapeRenderer.end();
    }

    private void drawSpriteLayer(SpriteLayer layer) {
        for (Sprite sprite : layer.getItems()) {
            drawPlaceholder(sprite);
        }
    }

    private void drawPlaceholder(Sprite sprite) {
        float half = PLACEHOLDER_SIZE / 2f;
        drawOutline(sprite, half);
        drawOrientationMarker(sprite, half);
    }

    private void drawOutline(Sprite sprite, float half) {
        Vector2 bottomLeft = toWorld(sprite, -half, -half);
        Vector2 bottomRight = toWorld(sprite, half, -half);
        Vector2 topRight = toWorld(sprite, half, half);
        Vector2 topLeft = toWorld(sprite, -half, half);

        shapeRenderer.line(bottomLeft, bottomRight);
        shapeRenderer.line(bottomRight, topRight);
        shapeRenderer.line(topRight, topLeft);
        shapeRenderer.line(topLeft, bottomLeft);
    }

    private void drawOrientationMarker(Sprite sprite, float half) {
        Vector2 corner = toWorld(sprite, half, half);
        Vector2 left = toWorld(sprite, half - MARKER_SIZE, half);
        Vector2 below = toWorld(sprite, half, half - MARKER_SIZE);

        shapeRenderer.triangle(corner.x, corner.y, left.x, left.y, below.x, below.y);
    }

    private Vector2 toWorld(Sprite sprite, float localX, float localY) {
        float x = sprite.isFlipX() ? -localX : localX;
        float y = sprite.isFlipY() ? -localY : localY;

        Vector2 point = new Vector2(x * sprite.getScaleX(), y * sprite.getScaleY());
        point.rotateDeg(sprite.getRotationDegrees());
        return point.add(sprite.getX(), sprite.getY());
    }
}
