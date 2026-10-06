package lex.folio.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import lex.folio.model.ImageAsset;
import lex.folio.model.Project;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class AssetLibrary implements Disposable {
    private static final String TAG = "AssetLibrary";

    private final Project project;
    private final Map<ImageAsset, TextureRegion> regionsByAsset = new IdentityHashMap<>();
    private final List<Texture> textures = new ArrayList<>();

    public AssetLibrary(Project project) {
        this.project = project;
    }

    public void loadAll() {
        for (ImageAsset asset : project.getAssets()) {
            load(asset);
        }
    }

    public TextureRegion findRegion(String assetId) {
        ImageAsset asset = project.findAsset(assetId);
        if (asset == null) return null;

        return regionsByAsset.get(asset);
    }

    public void load(ImageAsset asset) {
        Path file = project.getAssetsFolder().resolve(asset.getPath());

        try {
            Texture texture = new Texture(Gdx.files.absolute(file.toString()));
            texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);

            textures.add(texture);
            regionsByAsset.put(asset, new TextureRegion(texture));
        } catch (GdxRuntimeException e) {
            Gdx.app.error(TAG, "Could not load " + file, e);
        }
    }

    @Override
    public void dispose() {
        for (Texture texture : textures) {
            texture.dispose();
        }
        textures.clear();
        regionsByAsset.clear();
    }
}
