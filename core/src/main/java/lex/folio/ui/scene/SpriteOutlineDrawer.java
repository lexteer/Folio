package lex.folio.ui.scene;

import com.badlogic.gdx.math.Vector2;
import imgui.ImDrawList;
import imgui.ImGui;
import lex.folio.model.Sprite;
import lex.folio.scene.sprite.SpriteGeometry;

/** Draws outlines and markers around sprites, in screen space. */
class SpriteOutlineDrawer {
    private static final float LINE_THICKNESS = 1.5f;
    private static final float MARKER_SIZE = 0.25f;

    private final SceneViewport viewport;
    private final SpriteGeometry geometry;

    SpriteOutlineDrawer(SceneViewport viewport, SpriteGeometry geometry) {
        this.viewport = viewport;
        this.geometry = geometry;
    }

    void drawOutline(Sprite sprite, int color) {
        float halfWidth = geometry.getWidth(sprite) / 2f;
        float halfHeight = geometry.getHeight(sprite) / 2f;

        Vector2 bottomLeft = toScreen(sprite, -halfWidth, -halfHeight);
        Vector2 bottomRight = toScreen(sprite, halfWidth, -halfHeight);
        Vector2 topRight = toScreen(sprite, halfWidth, halfHeight);
        Vector2 topLeft = toScreen(sprite, -halfWidth, halfHeight);

        ImDrawList drawList = ImGui.getWindowDrawList();
        drawList.addQuad(
            bottomLeft.x, bottomLeft.y,
            bottomRight.x, bottomRight.y,
            topRight.x, topRight.y,
            topLeft.x, topLeft.y,
            color, LINE_THICKNESS
        );
    }

    /** A small triangle in the top right corner, so a placeholder's flip and rotation can be read. */
    void drawOrientationMarker(Sprite sprite, int color) {
        float halfWidth = geometry.getWidth(sprite) / 2f;
        float halfHeight = geometry.getHeight(sprite) / 2f;

        Vector2 corner = toScreen(sprite, halfWidth, halfHeight);
        Vector2 left = toScreen(sprite, halfWidth - MARKER_SIZE, halfHeight);
        Vector2 below = toScreen(sprite, halfWidth, halfHeight - MARKER_SIZE);

        ImGui.getWindowDrawList().addTriangle(
            corner.x, corner.y,
            left.x, left.y,
            below.x, below.y,
            color, LINE_THICKNESS
        );
    }

    private Vector2 toScreen(Sprite sprite, float localX, float localY) {
        Vector2 world = geometry.toWorld(sprite, localX, localY);
        return viewport.worldToScreen(world.x, world.y);
    }
}
