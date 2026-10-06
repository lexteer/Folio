package lex.folio.app;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.nio.file.Path;

/** Remembers which project was open last, in the .folio folder, so the editor can reopen it on start. */
final class LastProject {
    private static final String TAG = "LastProject";

    private final FileHandle file = Gdx.files.external(".folio/last-project.txt");

    /** The folder of the last open project, or null if there is none. */
    Path find() {
        if (!file.exists()) return null;

        try {
            String text = file.readString("UTF-8").trim();
            return text.isEmpty() ? null : Path.of(text);
        } catch (RuntimeException e) {
            Gdx.app.error(TAG, "Could not read " + file, e);
            return null;
        }
    }

    void save(Path projectFolder) {
        try {
            file.parent().mkdirs();
            file.writeString(projectFolder.toAbsolutePath().toString(), false, "UTF-8");
        } catch (RuntimeException e) {
            Gdx.app.error(TAG, "Could not write " + file, e);
        }
    }

    void clear() {
        file.delete();
    }
}
