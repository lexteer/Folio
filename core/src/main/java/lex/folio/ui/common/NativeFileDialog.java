package lex.folio.ui.common;

import com.badlogic.gdx.Gdx;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public final class NativeFileDialog {
    private static final String TAG = "NativeFileDialog";
    private static final String MULTIPLE_FILES_SEPARATOR = "|";

    private boolean open; // only read and written on the render thread

    public void choosePngFiles(Consumer<List<Path>> onChosen) {
        if (open) return;
        open = true;

        Thread thread = new Thread(() -> runDialog(onChosen), "File dialog");
        thread.setDaemon(true);
        thread.start();
    }

    // Runs on the background thread
    private void runDialog(Consumer<List<Path>> onChosen) {
        List<Path> files = showPngDialog();

        Gdx.app.postRunnable(() -> {
            open = false;
            onChosen.accept(files);
        });
    }

    private static List<Path> showPngDialog() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer filters = stack.mallocPointer(1);
            filters.put(stack.UTF8("*.png"));
            filters.flip();

            String result = TinyFileDialogs.tinyfd_openFileDialog(
                "Import images", null, filters, "PNG images", true);

            return toPaths(result);
        } catch (RuntimeException e) {
            Gdx.app.error(TAG, "File dialog failed", e);
            return List.of();
        }
    }

    private static List<Path> toPaths(String dialogResult) {
        List<Path> paths = new ArrayList<>();
        if (dialogResult == null) return paths;

        for (String part : dialogResult.split(Pattern.quote(MULTIPLE_FILES_SEPARATOR))) {
            paths.add(Path.of(part));
        }
        return paths;
    }

    public boolean isOpen() {
        return open;
    }
}
