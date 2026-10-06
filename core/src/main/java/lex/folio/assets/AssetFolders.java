package lex.folio.assets;

import com.badlogic.gdx.Gdx;
import lex.folio.model.ImageAsset;
import lex.folio.model.Project;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/** Reads and changes the asset folders on disk. */
public class AssetFolders {
    private static final String TAG = "AssetFolders";

    private final Project project;

    public AssetFolders(Project project) {
        this.project = project;
    }

    public List<String> listSubfolders(String folder) {
        Path directory = toDiskPath(folder);

        try (Stream<Path> entries = Files.list(directory)) {
            return entries.filter(Files::isDirectory)
                .map(entry -> AssetFolderPath.join(folder, entry.getFileName().toString()))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
        } catch (IOException e) {
            Gdx.app.error(TAG, "Could not list " + directory, e);
            return List.of();
        }
    }

    public void moveAsset(ImageAsset asset, String folder) {
        if (asset.getFolder().equals(folder)) return;

        String newPath = AssetFolderPath.join(folder, asset.getFileName());
        Path source = toDiskPath(asset.getPath());
        Path target = toDiskPath(newPath);

        try {
            Files.move(source, target);
        } catch (IOException e) {
            Gdx.app.error(TAG, "Could not move " + source + " to " + target, e);
            return;
        }
        asset.setPath(newPath);
    }

    private Path toDiskPath(String assetsRelativePath) {
        return project.getAssetsFolder().resolve(assetsRelativePath);
    }
}
