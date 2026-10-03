package lex.folio.ui;

import com.badlogic.gdx.math.Vector2;
import imgui.ImGui;
import imgui.flag.ImGuiMouseButton;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import lex.folio.model.Room;
import lex.folio.model.Sprite;
import lex.folio.scene.SceneCamera;
import lex.folio.scene.SceneRenderer;
import lex.folio.scene.Selection;
import lex.folio.scene.SpritePicker;

public class ScenePanel {
    private static final String TITLE = "Scene";
    private static final int WINDOW_FLAGS = ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoScrollWithMouse;
    private static final float ZOOM_STEP = 1.15f;

    private final SceneRenderer sceneRenderer;
    private final SceneCamera camera;
    private boolean panning;

    private final Room room;
    private final SpritePicker spritePicker;
    private final Selection selection;
    private final SceneOverlay sceneOverlay;

    public ScenePanel(SceneRenderer sceneRenderer, SceneCamera camera, SpritePicker spritePicker,
                      SceneOverlay sceneOverlay, Selection selection, Room room) {
        this.sceneRenderer = sceneRenderer;
        this.camera = camera;
        this.spritePicker = spritePicker;
        this.selection = selection;
        this.room = room;
        this.sceneOverlay = sceneOverlay;
    }

    public void draw() {
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 0, 0);
        boolean visible = ImGui.begin(TITLE, WINDOW_FLAGS);
        ImGui.popStyleVar();

        if (visible) {
            drawContent();
        }
        ImGui.end();
    }

    private void drawContent() {
        int width = (int) ImGui.getContentRegionAvailX();
        int height = (int) ImGui.getContentRegionAvailY();
        if (width <= 0 || height <= 0) return;

        sceneRenderer.render(room, width, height);
        ImGui.image(sceneRenderer.getTextureHandle(), width, height, 0, 1, 1, 0);
        sceneOverlay.draw(room, selection, ImGui.getItemRectMinX(), ImGui.getItemRectMinY());

        boolean hovered = ImGui.isItemHovered();
        handleSelecting(hovered);
        handlePanning(hovered);
        handleZooming(hovered);
    }

    private void handleSelecting(boolean hovered) {
        if (!hovered || !ImGui.isMouseClicked(ImGuiMouseButton.Left)) {
            return;
        }
        Vector2 world = camera.screenToWorld(getMouseX(), getMouseY());
        Sprite sprite = spritePicker.findSpriteAt(room, world.x, world.y);

        if (sprite == null) {
            selection.clear();
        } else {
            selection.selectOnly(sprite);
        }
    }


    private void handlePanning(boolean hovered) {
        if (hovered && isPanButtonClicked()) panning = true;
        if (!isPanButtonDown()) panning = false;

        if (panning) {
            camera.panByScreenPixels(ImGui.getIO().getMouseDeltaX(), ImGui.getIO().getMouseDeltaY());
        }
    }

    private void handleZooming(boolean hovered) {
        float wheel = ImGui.getIO().getMouseWheel();
        if (!hovered || wheel == 0) {
            return;
        }
        camera.zoomAt(getMouseX(), getMouseY(), (float) Math.pow(ZOOM_STEP, -wheel));
    }

    private float getMouseX() {
        return ImGui.getMousePosX() - ImGui.getItemRectMinX();
    }

    private float getMouseY() {
        return ImGui.getMousePosY() - ImGui.getItemRectMinY();
    }

    private boolean isPanButtonClicked() {
        return ImGui.isMouseClicked(ImGuiMouseButton.Middle);
    }

    private boolean isPanButtonDown() {
        return ImGui.isMouseDown(ImGuiMouseButton.Middle);
    }
}
