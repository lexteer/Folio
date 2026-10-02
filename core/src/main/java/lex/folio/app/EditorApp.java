package lex.folio.app;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.utils.ScreenUtils;
import imgui.ImGui;
import lex.folio.scene.SceneRenderer;
import lex.folio.ui.ScenePanel;

public class EditorApp extends ApplicationAdapter {
    private ImGuiBackend imGui;
    private SceneRenderer sceneRenderer;
    private ScenePanel scenePanel;

    @Override
    public void create() {
        imGui = new ImGuiBackend();
        sceneRenderer = new SceneRenderer();
        scenePanel = new ScenePanel(sceneRenderer);
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.08f, 0.08f, 0.09f, 1f);
        imGui.beginFrame();
        ImGui.dockSpaceOverViewport(0, ImGui.getMainViewport());
        scenePanel.draw();
        imGui.endFrame();
    }

    @Override
    public void dispose() {
        sceneRenderer.dispose();
        imGui.dispose();
    }
}
