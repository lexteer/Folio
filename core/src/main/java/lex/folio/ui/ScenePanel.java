package lex.folio.ui;

import com.badlogic.gdx.math.Vector2;
import imgui.ImGui;
import imgui.flag.ImGuiMouseButton;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import lex.folio.model.ImageAsset;
import lex.folio.model.Room;
import lex.folio.scene.*;
import lex.folio.ui.assets.AssetDragDrop;

public class ScenePanel {
    public static final String TITLE = "Scene";
    private static final int WINDOW_FLAGS = ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoScrollWithMouse;
    private static final float ZOOM_STEP = 1.15f;

    private final SceneRenderer sceneRenderer;
    private final SceneCamera camera;
    private final SceneOverlay sceneOverlay;
    private final Room room;
    private final SceneToolbar toolbar;
    private final SceneTools tools;

    private boolean panning;
    private float imageX;
    private float imageY;

    public ScenePanel(SceneRenderer sceneRenderer, SceneCamera camera, SceneOverlay sceneOverlay,
                      Room room, SceneTools tools) {
        this.sceneRenderer = sceneRenderer;
        this.camera = camera;
        this.sceneOverlay = sceneOverlay;
        this.room = room;
        this.tools = tools;
        this.toolbar = new SceneToolbar(tools.getToolState());
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
        imageX = ImGui.getItemRectMinX();
        imageY = ImGui.getItemRectMinY();
        boolean imageHovered = ImGui.isItemHovered();
        handleAssetDrop();

        sceneOverlay.draw(room, imageX, imageY);
        toolbar.draw(imageX, imageY, width);

        boolean hovered = imageHovered && !toolbar.isHovered();
        handleToolInput(hovered);
        handlePanning(hovered);
        handleZooming(hovered);
    }

    private void handleAssetDrop() {
        ImageAsset dropped = AssetDragDrop.acceptDropOnLastItemWithoutOutline();
        if (dropped == null) return;

        Vector2 world = getMouseWorld();
        tools.dropAsset(room, dropped, world.x, world.y);
    }

    private void handleToolInput(boolean hovered) {
        Vector2 world = getMouseWorld();
        if (hovered && ImGui.isMouseClicked(ImGuiMouseButton.Left)) {
            tools.press(room, world.x, world.y);
        }
        if (!tools.isDragging()) return;

        if (ImGui.isMouseDown(ImGuiMouseButton.Left)) {
            tools.drag(world.x, world.y);
        } else {
            tools.release();
        }
    }

    private Vector2 getMouseWorld() {
        return camera.screenToWorld(getMouseX(), getMouseY());
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
        return ImGui.getMousePosX() - imageX;
    }

    private float getMouseY() {
        return ImGui.getMousePosY() - imageY;
    }

    private boolean isPanButtonClicked() {
        return ImGui.isMouseClicked(ImGuiMouseButton.Middle);
    }

    private boolean isPanButtonDown() {
        return ImGui.isMouseDown(ImGuiMouseButton.Middle);
    }
}
