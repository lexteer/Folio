package lex.folio.app;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.utils.ScreenUtils;
import imgui.ImGui;

public class EditorApp extends ApplicationAdapter {
    private ImGuiBackend imGui;

    @Override
    public void create() {
        imGui = new ImGuiBackend();
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.08f, 0.08f, 0.09f, 1f);
        imGui.beginFrame();
        ImGui.dockSpaceOverViewport();
        ImGui.showDemoWindow();
        imGui.endFrame();
    }

    @Override
    public void dispose() {
        imGui.dispose();
    }
}
