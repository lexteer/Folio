package lex.folio.ui.common;

import com.badlogic.gdx.Gdx;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public final class NativeFileDialog {
    private static final String TAG = "NativeFileDialog";
    private static final String MULTIPLE_FILES_SEPARATOR = "|";

    private boolean open; // only read and written on the render thread

    public void choosePngFiles(Consumer<List<Path>> onChosen) {
        runInBackground(NativeFileDialog::showPngDialog, onChosen);
    }

    /** Calls {@code onChosen} with the folder, or not at all if the dialog was cancelled. */
    public void chooseFolder(String title, Consumer<Path> onChosen) {
        runInBackground(() -> showFolderDialog(title), folder -> {
            if (folder != null) onChosen.accept(folder);
        });
    }

    /** Calls {@code onChosen} with the file, or not at all if the dialog was cancelled. */
    public void chooseFile(String title, Path startFolder, String filter, String description, Consumer<Path> onChosen) {
        runInBackground(() -> showFileDialog(title, startFolder, filter, description), file -> {
            if (file != null) onChosen.accept(file);
        });
    }

    private <T> void runInBackground(Supplier<T> dialog, Consumer<T> onChosen) {
        if (open) return;
        open = true;

        Thread thread = new Thread(() -> {
            T result = dialog.get();
            Gdx.app.postRunnable(() -> {
                open = false;
                onChosen.accept(result);
            });
        }, "File dialog");
        thread.setDaemon(true);
        thread.start();
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

    private static Path showFileDialog(String title, Path startFolder, String filter, String description) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer filters = stack.mallocPointer(1);
            filters.put(stack.UTF8(filter));
            filters.flip();

            // A trailing separator makes the dialog start inside the folder instead of selecting it.
            String start = startFolder == null ? null : startFolder + java.io.File.separator;
            String result = TinyFileDialogs.tinyfd_openFileDialog(title, start, filters, description, false);
            return result == null ? null : Path.of(result);
        } catch (RuntimeException e) {
            Gdx.app.error(TAG, "File dialog failed", e);
            return null;
        }
    }

    private static Path showFolderDialog(String title) {
        try {
            String result = TinyFileDialogs.tinyfd_selectFolderDialog(title, null);
            return result == null ? null : Path.of(result);
        } catch (RuntimeException e) {
            Gdx.app.error(TAG, "Folder dialog failed", e);
            return null;
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
