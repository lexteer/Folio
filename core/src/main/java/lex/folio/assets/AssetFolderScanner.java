package lex.folio.assets;

import com.badlogic.gdx.Gdx;
import lex.folio.model.ImageAsset;
import lex.folio.model.Project;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * Temporary: registers every PNG in the project's assets folder as an asset.
 * Goes away once assets come from importing and from project.json.
 */
public final class AssetFolderScanner {
    private static final String TAG = "AssetFolderScanner";
    private static final String PNG_EXTENSION = ".png";

    private AssetFolderScanner() {
    }

    public static void addAssetsTo(Project project) {
        Path assetsFolder = project.getAssetsFolder();
        if (!Files.isDirectory(assetsFolder)) {
            Gdx.app.error(TAG, "Assets folder not found: " + assetsFolder);
            return;
        }

        try (Stream<Path> files = Files.walk(assetsFolder)) {
            files.filter(AssetFolderScanner::isPng)
                .sorted()
                .forEach(file -> addAsset(project, file));
        } catch (IOException e) {
            Gdx.app.error(TAG, "Could not scan " + assetsFolder, e);
        }
    }

    private static void addAsset(Project project, Path file) {
        String id = toAssetId(file);
        if (project.findAsset(id) != null) {
            Gdx.app.error(TAG, "Skipping " + file + ": asset id '" + id + "' is already used");
            return;
        }

        String path = project.getAssetsFolder().relativize(file).toString().replace('\\', '/');
        project.addAsset(new ImageAsset(id, path));
    }

    private static String toAssetId(Path file) {
        String fileName = file.getFileName().toString();
        return fileName.substring(0, fileName.length() - PNG_EXTENSION.length());
    }

    private static boolean isPng(Path file) {
        String fileName = file.getFileName().toString().toLowerCase(Locale.ROOT);
        return Files.isRegularFile(file) && fileName.endsWith(PNG_EXTENSION);
    }
}
