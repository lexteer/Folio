package lex.folio.ui.scene;

import imgui.ImGui;
import imgui.flag.ImGuiMouseButton;

/** Pans with the middle mouse button and zooms with the wheel. */
class SceneCameraControls {
    private static final float ZOOM_STEP = 1.15f;

    private final SceneViewport viewport;
    private boolean panning;

    SceneCameraControls(SceneViewport viewport) {
        this.viewport = viewport;
    }

    void handle(boolean hovered) {
        handlePanning(hovered);
        handleZooming(hovered);
    }

    private void handlePanning(boolean hovered) {
        if (hovered && ImGui.isMouseClicked(ImGuiMouseButton.Middle)) panning = true;
        if (!ImGui.isMouseDown(ImGuiMouseButton.Middle)) panning = false;

        if (panning) {
            viewport.getCamera().panByScreenPixels(ImGui.getIO().getMouseDeltaX(), ImGui.getIO().getMouseDeltaY());
        }
    }

    private void handleZooming(boolean hovered) {
        float wheel = ImGui.getIO().getMouseWheel();
        if (!hovered || wheel == 0) return;

        viewport.getCamera().zoomAt(viewport.getMouseX(), viewport.getMouseY(), (float) Math.pow(ZOOM_STEP, -wheel));
    }
}
