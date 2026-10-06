package lex.folio.ui.scene;

import com.badlogic.gdx.math.Vector2;
import imgui.ImGui;
import lex.folio.scene.camera.SceneCamera;

/** Where the scene image sits on screen, and conversions between screen, image and world positions. */
public class SceneViewport {
    private SceneCamera camera;
    private float imageX;
    private float imageY;
    private float width;

    /** The viewport follows whichever room's camera is being shown. */
    public void setCamera(SceneCamera camera) {
        this.camera = camera;
    }

    /** Records the screen position of the scene image, to be called after drawing it. */
    public void updateFromLastItem() {
        imageX = ImGui.getItemRectMinX();
        imageY = ImGui.getItemRectMinY();
        width = ImGui.getItemRectSizeX();
    }

    public SceneCamera getCamera() {
        return camera;
    }

    public float getImageX() {
        return imageX;
    }

    public float getImageY() {
        return imageY;
    }

    public float getWidth() {
        return width;
    }

    /** The mouse position relative to the top left of the scene image. */
    public float getMouseX() {
        return ImGui.getMousePosX() - imageX;
    }

    public float getMouseY() {
        return ImGui.getMousePosY() - imageY;
    }

    public Vector2 getMouseWorld() {
        return camera.screenToWorld(getMouseX(), getMouseY());
    }

    public Vector2 worldToScreen(float worldX, float worldY) {
        return camera.worldToScreen(worldX, worldY).add(imageX, imageY);
    }
}
