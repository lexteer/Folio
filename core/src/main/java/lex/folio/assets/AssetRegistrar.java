package lex.folio.assets;

import com.badlogic.gdx.Gdx;
import lex.folio.model.ImageAsset;
import lex.folio.model.Project;

import java.nio.file.Path;

/** Adds image files to the project as assets, refusing ids the project already uses. */
final class AssetRegistrar {
    private static final String TAG = "AssetRegistrar";

    private final Project project;

    AssetRegistrar(Project project) {
        this.project = project;
    }

    /** The id the file would get as an asset, or null (after logging) if the project already uses it. */
    String findFreeId(Path file) {
        String id = ImageFiles.toAssetId(file);
        if (project.findAsset(id) == null) return id;

        Gdx.app.error(TAG, "Skipping " + file + ": asset id '" + id + "' is already used");
        return null;
    }

    ImageAsset add(String id, String assetPath) {
        ImageAsset asset = new ImageAsset(id, assetPath);
        project.addAsset(asset);
        return asset;
    }
}
