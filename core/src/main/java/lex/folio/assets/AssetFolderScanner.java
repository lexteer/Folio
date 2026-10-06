package lex.folio.assets;

import com.badlogic.gdx.Gdx;
import lex.folio.model.Project;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Temporary: registers every PNG in the project's assets folder as an asset.
 * Goes away once assets come from importing and from the project file.
 */
public final class AssetFolderScanner {
    private static final String TAG = "AssetFolderScanner";

    private AssetFolderScanner() {
    }

    public static void addAssetsTo(Project project) {
        Path assetsFolder = project.getAssetsFolder();
        if (!Files.isDirectory(assetsFolder)) {
            Gdx.app.error(TAG, "Assets folder not found: " + assetsFolder);
            return;
        }

        AssetRegistrar registrar = new AssetRegistrar(project);
        try (Stream<Path> files = Files.walk(assetsFolder)) {
            files.filter(ImageFiles::isPng)
                .sorted()
                .forEach(file -> addAsset(project, registrar, file));
        } catch (IOException e) {
            Gdx.app.error(TAG, "Could not scan " + assetsFolder, e);
        }
    }

    private static void addAsset(Project project, AssetRegistrar registrar, Path file) {
        String id = registrar.findFreeId(file);
        if (id == null) return;

        String path = project.getAssetsFolder().relativize(file).toString().replace('\\', '/');
        registrar.add(id, path);
    }
}
