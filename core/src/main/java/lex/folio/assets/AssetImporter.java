package lex.folio.assets;

import com.badlogic.gdx.Gdx;
import lex.folio.model.ImageAsset;
import lex.folio.model.Project;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public class AssetImporter {
    private static final String TAG = "AssetImporter";
    private static final String PNG_EXTENSION = ".png";

    private final Project project;
    private final AssetLibrary assetLibrary;

    public AssetImporter(Project project, AssetLibrary assetLibrary) {
        this.project = project;
        this.assetLibrary = assetLibrary;
    }

    public void importFiles(List<Path> paths, String folder) {
        for (Path path : paths) {
            importPath(path, folder);
        }
    }

    private void importPath(Path path, String folder) {
        if (Files.isDirectory(path)) {
            importFolder(path, folder);
        } else if (isPng(path)) {
            importPngFile(path, folder);
        } else {
            Gdx.app.log(TAG, "Skipping " + path + ": not a PNG file");
        }
    }

    private void importFolder(Path sourceFolder, String parentFolder) {
        String targetFolder = AssetFolders.join(parentFolder, sourceFolder.getFileName().toString());
        importFiles(listFolder(sourceFolder), targetFolder);
    }

    private List<Path> listFolder(Path folder) {
        try (Stream<Path> entries = Files.list(folder)) {
            return entries.sorted().toList();
        } catch (IOException e) {
            Gdx.app.error(TAG, "Could not list " + folder, e);
            return List.of();
        }
    }

    private void importPngFile(Path file, String folder) {
        String id = toAssetId(file);
        if (project.findAsset(id) != null) {
            Gdx.app.error(TAG, "Skipping " + file + ": asset id '" + id + "' is already used");
            return;
        }

        String assetPath = AssetFolders.join(folder, file.getFileName().toString());
        Path target = project.getAssetsFolder().resolve(assetPath);
        if (!copyFile(file, target)) return;

        ImageAsset asset = new ImageAsset(id, assetPath);
        project.addAsset(asset);
        assetLibrary.load(asset);
    }

    private boolean copyFile(Path source, Path target) {
        try {
            Files.createDirectories(target.getParent());
            Files.copy(source, target);
            return true;
        } catch (IOException e) {
            Gdx.app.error(TAG, "Could not copy " + source + " to " + target, e);
            return false;
        }
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
