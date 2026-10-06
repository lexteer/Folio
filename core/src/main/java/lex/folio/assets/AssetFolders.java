package lex.folio.assets;

import com.badlogic.gdx.Gdx;
import lex.folio.model.AssetFolderPath;
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

    /**
     * Renames the asset's file and gives the asset the file name as its id. The name has no extension.
     * Returns why it could not be renamed, or null when it was.
     */
    public String renameAsset(ImageAsset asset, String name) {
        String problem = findNameProblem(asset, name);
        if (problem != null) return problem;

        String newPath = AssetFolderPath.join(asset.getFolder(), name + ImageFiles.PNG_EXTENSION);
        Path source = toDiskPath(asset.getPath());
        Path target = toDiskPath(newPath);

        try {
            if (Files.exists(target) && !Files.isSameFile(source, target)) {
                return "There is already a file called \"" + target.getFileName() + "\".";
            }
            Files.move(source, target);
        } catch (IOException e) {
            Gdx.app.error(TAG, "Could not rename " + source + " to " + target, e);
            return "Could not rename the file: " + e.getMessage();
        }
        asset.setPath(newPath);
        project.renameAsset(asset, name);
        return null;
    }

    private String findNameProblem(ImageAsset asset, String name) {
        if (name.isEmpty()) return "An asset needs a name.";
        if (name.matches(".*[\\\\/:*?\"<>|].*")) return "An asset name cannot contain any of \\ / : * ? \" < > |";
        if (name.endsWith(".") || name.endsWith(" ")) return "An asset name cannot end with a dot or a space.";

        ImageAsset owner = project.findAsset(name);
        if (owner != null && owner != asset) return "There is already an asset called \"" + name + "\".";
        return null;
    }

    private Path toDiskPath(String assetsRelativePath) {
        return project.getAssetsFolder().resolve(assetsRelativePath);
    }
}
