package lex.folio.app;

import com.badlogic.gdx.utils.Disposable;
import imgui.ImGui;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import org.lwjgl.glfw.GLFW;

/** Starts ImGui, wraps each frame, and shuts it down. */
public class ImGuiBackend implements Disposable {
    private final ImGuiImplGlfw platform = new ImGuiImplGlfw();
    private final ImGuiImplGl3 renderer = new ImGuiImplGl3();

    /** Must be created after the GL context exists, i.e. in ApplicationListener.create(). */
    public ImGuiBackend() {
        ImGui.createContext();
        long windowHandle = GLFW.glfwGetCurrentContext();
        platform.init(windowHandle, true);
        renderer.init("#version 150");
    }

    public void beginFrame() {
        renderer.newFrame();
        platform.newFrame();
        ImGui.newFrame();
    }

    public void endFrame() {
        ImGui.render();
        renderer.renderDrawData(ImGui.getDrawData());
    }

    @Override
    public void dispose() {
        renderer.shutdown();
        platform.shutdown();
        ImGui.destroyContext();
    }
}
