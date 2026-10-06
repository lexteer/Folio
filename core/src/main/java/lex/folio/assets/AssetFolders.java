package lex.folio.assets;

import com.badlogic.gdx.Gdx;
import lex.folio.model.ImageAsset;
import lex.folio.model.Project;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class AssetFolders {
    public static final String ROOT = "";
    private static final String TAG = "AssetFolders";
    private static final char SEPARATOR = '/';

    private final Project project;

    public AssetFolders(Project project) {
        this.project = project;
    }

    public List<String> listSubfolders(String folder) {
        Path directory = toDiskPath(folder);

        try (Stream<Path> entries = Files.list(directory)) {
            return entries.filter(Files::isDirectory)
                .map(entry -> join(folder, entry.getFileName().toString()))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
        } catch (IOException e) {
            Gdx.app.error(TAG, "Could not list " + directory, e);
            return List.of();
        }
    }

    public Path toDiskPath(String folder) {
        return project.getAssetsFolder().resolve(folder);
    }

    public static String join(String folder, String name) {
        return folder.equals(ROOT) ? name : folder + SEPARATOR + name;
    }

    public static String getName(String folder) {
        return folder.substring(folder.lastIndexOf(SEPARATOR) + 1);
    }

    public static List<String> getPathFromRoot(String folder) {
        List<String> result = new ArrayList<>();
        if (folder.equals(ROOT)) return result;

        int end = folder.indexOf(SEPARATOR);
        while (end >= 0) {
            result.add(folder.substring(0, end));
            end = folder.indexOf(SEPARATOR, end + 1);
        }
        result.add(folder);
        return result;
    }

    public void moveAsset(ImageAsset asset, String folder) {
        if (asset.getFolder().equals(folder)) return;

        String newPath = join(folder, asset.getFileName());
        Path source = project.getAssetsFolder().resolve(asset.getPath());
        Path target = project.getAssetsFolder().resolve(newPath);

        try {
            Files.move(source, target);
        } catch (IOException e) {
            Gdx.app.error(TAG, "Could not move " + source + " to " + target, e);
            return;
        }
        asset.setPath(newPath);
    }
}
