package lex.folio.app;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Disposable;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.ImFontAtlas;
import imgui.ImFontConfig;
import imgui.ImGuiStyle;
import imgui.ImVec4;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiConfigFlags;
import imgui.flag.ImGuiDir;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import lex.folio.ui.common.Icons;
import org.lwjgl.glfw.GLFW;

/** Starts ImGui, wraps each frame, and shuts it down. */
public class ImGuiBackend implements Disposable {
    private final ImGuiImplGlfw platform = new ImGuiImplGlfw();
    private final ImGuiImplGl3 renderer = new ImGuiImplGl3();

    private final FileHandle layoutFile = Gdx.files.external(".folio/layout.ini");
    private final boolean hadSavedLayout = layoutFile.exists();

    /** Must be created after the GL context exists, i.e. in ApplicationListener.create(). */
    public ImGuiBackend() {
        ImGui.createContext();
        configureIo();
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

    private void configureIo() {
        ImGuiIO io = ImGui.getIO();
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable);
        loadFonts(io);

        layoutFile.parent().mkdirs();
        io.setIniFilename(layoutFile.file().getAbsolutePath());
        io.setConfigWindowsMoveFromTitleBarOnly(true);

        // No dimming behind modal popups.
        ImGuiStyle style = ImGui.getStyle();
        style.setColor(ImGuiCol.ModalWindowDimBg, 0f, 0f, 0f, 0f);

        // No collapse arrow on the tab bar row.
        style.setWindowMenuButtonPosition(ImGuiDir.None);

        // Tab bar row keeps the same background whether or not the panel is focused.
        ImVec4 titleBg = new ImVec4();
        style.getColor(ImGuiCol.TitleBg, titleBg);
        style.setColor(ImGuiCol.TitleBgActive, titleBg.x, titleBg.y, titleBg.z, titleBg.w);
    }

    /** The default font, with the icon font merged in so that icons can be written like any other text. */
    private void loadFonts(ImGuiIO io) {
        ImFontAtlas fonts = io.getFonts();
        fonts.addFontDefault();

        ImFontConfig config = new ImFontConfig();
        config.setMergeMode(true);
        // The icons sit on the same baseline as the text, which puts them too high next to the small default font.
        config.setGlyphOffset(0f, 3f);
        fonts.addFontFromMemoryTTF(Gdx.files.internal(Icons.FONT_FILE).readBytes(), Icons.FONT_SIZE, config,
            Icons.GLYPH_RANGES);
        config.destroy();
    }

    public boolean hadSavedLayout() {
        return hadSavedLayout;
    }
}
