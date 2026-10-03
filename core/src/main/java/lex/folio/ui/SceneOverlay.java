package lex.folio.ui;

import com.badlogic.gdx.math.Vector2;
import imgui.ImDrawList;
import imgui.ImGui;
import lex.folio.assets.AssetLibrary;
import lex.folio.model.Layer;
import lex.folio.model.Room;
import lex.folio.model.RoomObject;
import lex.folio.model.Sprite;
import lex.folio.model.SpriteLayer;
import lex.folio.scene.SceneCamera;
import lex.folio.scene.Selection;
import lex.folio.scene.SpriteGeometry;

public class SceneOverlay {
    private static final float LINE_THICKNESS = 1.5f;
    private static final float MARKER_SIZE = 0.25f;

    private final SceneCamera camera;
    private final SpriteGeometry geometry;
    private final AssetLibrary assetLibrary;
    private final int placeholderColor = ImGui.colorConvertFloat4ToU32(0.95f, 0.45f, 0.85f, 1f);
    private final int selectionColor = ImGui.colorConvertFloat4ToU32(0.35f, 0.65f, 1f, 1f);

    private float imageX;
    private float imageY;

    public SceneOverlay(SceneCamera camera, SpriteGeometry geometry, AssetLibrary assetLibrary) {
        this.camera = camera;
        this.geometry = geometry;
        this.assetLibrary = assetLibrary;
    }

    public void draw(Room room, Selection selection, float imageX, float imageY) {
        this.imageX = imageX;
        this.imageY = imageY;

        ImDrawList drawList = ImGui.getWindowDrawList();
        drawPlaceholders(drawList, room);
        drawSelectionOutlines(drawList, selection);
    }

    private void drawPlaceholders(ImDrawList drawList, Room room) {
        for (Layer<?> layer : room.getLayers()) {
            if (layer.isVisible() && layer instanceof SpriteLayer spriteLayer) {
                drawPlaceholders(drawList, spriteLayer);
            }
        }
    }

    private void drawPlaceholders(ImDrawList drawList, SpriteLayer layer) {
        for (Sprite sprite : layer.getItems()) {
            if (assetLibrary.findRegion(sprite.getAssetId()) == null) {
                drawOutline(drawList, sprite, placeholderColor);
                drawOrientationMarker(drawList, sprite);
            }
        }
    }

    private void drawSelectionOutlines(ImDrawList drawList, Selection selection) {
        for (RoomObject object : selection.getObjects()) {
            if (object instanceof Sprite sprite) {
                drawOutline(drawList, sprite, selectionColor);
            }
        }
    }

    private void drawOutline(ImDrawList drawList, Sprite sprite, int color) {
        float halfWidth = geometry.getWidth(sprite) / 2f;
        float halfHeight = geometry.getHeight(sprite) / 2f;

        Vector2 bottomLeft = toScreen(sprite, -halfWidth, -halfHeight);
        Vector2 bottomRight = toScreen(sprite, halfWidth, -halfHeight);
        Vector2 topRight = toScreen(sprite, halfWidth, halfHeight);
        Vector2 topLeft = toScreen(sprite, -halfWidth, halfHeight);

        drawList.addQuad(
            bottomLeft.x, bottomLeft.y,
            bottomRight.x, bottomRight.y,
            topRight.x, topRight.y,
            topLeft.x, topLeft.y,
            color, LINE_THICKNESS
        );
    }

    private void drawOrientationMarker(ImDrawList drawList, Sprite sprite) {
        float halfWidth = geometry.getWidth(sprite) / 2f;
        float halfHeight = geometry.getHeight(sprite) / 2f;

        Vector2 corner = toScreen(sprite, halfWidth, halfHeight);
        Vector2 left = toScreen(sprite, halfWidth - MARKER_SIZE, halfHeight);
        Vector2 below = toScreen(sprite, halfWidth, halfHeight - MARKER_SIZE);

        drawList.addTriangle(
            corner.x, corner.y,
            left.x, left.y,
            below.x, below.y,
            placeholderColor, LINE_THICKNESS
        );
    }

    private Vector2 toScreen(Sprite sprite, float localX, float localY) {
        Vector2 world = geometry.toWorld(sprite, localX, localY);
        return camera.worldToScreen(world.x, world.y).add(imageX, imageY);
    }
}
