package lex.folio.app;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.utils.ScreenUtils;

import java.nio.file.Path;
import java.util.Arrays;

/** The application's lifecycle: starts the editor on top of ImGui, runs a frame at a time, shuts down. */
public class EditorApp extends ApplicationAdapter {
    private ImGuiBackend imGui;
    private Dockspace dockspace;
    private Editor editor;

    @Override
    public void create() {
        imGui = new ImGuiBackend();
        dockspace = new Dockspace(!imGui.hadSavedLayout());
        editor = new Editor();
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.08f, 0.08f, 0.09f, 1f);

        imGui.beginFrame();
        dockspace.show();
        editor.draw();
        imGui.endFrame();
    }

    @Override
    public void dispose() {
        editor.dispose();
        imGui.dispose();
    }

    /** Whether the window may close right now. Unsaved rooms are asked about first, and the app exits afterwards. */
    public boolean requestClose() {
        return editor.requestClose();
    }

    public void filesDropped(String[] files) {
        editor.filesDropped(Arrays.stream(files).map(Path::of).toList());
    }
}
