package lex.folio.ui;

import imgui.ImGui;
import imgui.flag.ImGuiStyleVar;
import lex.folio.scene.SceneRenderer;

public class ScenePanel {
    private static final String TITLE = "Scene";

    private final SceneRenderer sceneRenderer;

    public ScenePanel(SceneRenderer sceneRenderer) {
        this.sceneRenderer = sceneRenderer;
    }

    public void draw() {
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 0, 0);
        boolean visible = ImGui.begin(TITLE);
        ImGui.popStyleVar();
        if (visible) {
            drawContent();
        }
        ImGui.end();
    }

    private void drawContent() {
        int width = (int) ImGui.getContentRegionAvailX();
        int height = (int) ImGui.getContentRegionAvailY();
        if (width <= 0 || height <= 0) {
            return;
        }
        sceneRenderer.render(width, height);
        ImGui.image(sceneRenderer.getTextureHandle(), width, height, 0, 1, 1, 0);
    }
}
