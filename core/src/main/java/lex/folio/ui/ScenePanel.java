package lex.folio.ui;

import imgui.ImGui;
import imgui.flag.ImGuiMouseButton;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import lex.folio.scene.SceneCamera;
import lex.folio.scene.SceneRenderer;

public class ScenePanel {
    private static final String TITLE = "Scene";
    private static final int WINDOW_FLAGS = ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoScrollWithMouse;
    private static final float ZOOM_STEP = 1.15f;

    private final SceneRenderer sceneRenderer;
    private final SceneCamera camera;
    private boolean panning;

    public ScenePanel(SceneRenderer sceneRenderer, SceneCamera camera) {
        this.sceneRenderer = sceneRenderer;
        this.camera = camera;
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

        sceneRenderer.render(width, height);
        ImGui.image(sceneRenderer.getTextureHandle(), width, height, 0, 1, 1, 0);

        boolean hovered = ImGui.isItemHovered();
        handlePanning(hovered);
        handleZooming(hovered);
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
        float mouseX = ImGui.getMousePosX() - ImGui.getItemRectMinX();
        float mouseY = ImGui.getMousePosY() - ImGui.getItemRectMinY();
        camera.zoomAt(mouseX, mouseY, (float) Math.pow(ZOOM_STEP, -wheel));
    }

    private boolean isPanButtonClicked() {
        return ImGui.isMouseClicked(ImGuiMouseButton.Middle);
    }

    private boolean isPanButtonDown() {
        return ImGui.isMouseDown(ImGuiMouseButton.Middle);
    }
}
