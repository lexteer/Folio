package lex.folio.model;

import java.nio.file.Path;
import java.util.*;

public class Project {
    private static final String ASSETS_FOLDER_NAME = "assets";
    private static final String ROOMS_FOLDER_NAME = "rooms";
    private final Map<String, ImageAsset> assetsByKey = new LinkedHashMap<>();
    private final Collection<ImageAsset> readOnlyAssets = Collections.unmodifiableCollection(assetsByKey.values());

    private final Path rootFolder;
    private float pixelsPerMeter;

    public Project(Path rootFolder, float pixelsPerMeter) {
        this.rootFolder = Objects.requireNonNull(rootFolder, "rootFolder");
        setPixelsPerMeter(pixelsPerMeter);
    }

    public Path getRootFolder() {
        return rootFolder;
    }

    public Collection<ImageAsset> getAssets() {
        return readOnlyAssets;
    }

    public ImageAsset findAsset(String id) {
        return assetsByKey.get(toLookupKey(id));
    }

    public void addAsset(ImageAsset asset) {
        String key = toLookupKey(asset.getId());

        if (assetsByKey.containsKey(key)) {
            throw new IllegalArgumentException("Asset id already exists: " + asset.getId());
        }

        assetsByKey.put(key, asset);
    }

    /** Gives the asset a new id, keeping its place in the order of assets. The new id must not be used by another asset. */
    public void renameAsset(ImageAsset asset, String newId) {
        ImageAsset owner = findAsset(newId);
        if (owner != null && owner != asset) {
            throw new IllegalArgumentException("Asset id already exists: " + newId);
        }

        asset.setId(newId);
        List<ImageAsset> assets = new ArrayList<>(assetsByKey.values());
        assetsByKey.clear();
        for (ImageAsset each : assets) {
            assetsByKey.put(toLookupKey(each.getId()), each);
        }
    }

    private static String toLookupKey(String id) {
        return id.toLowerCase(Locale.ROOT);
    }

    public Path getAssetsFolder() {
        return rootFolder.resolve(ASSETS_FOLDER_NAME);
    }

    public Path getRoomsFolder() {
        return rootFolder.resolve(ROOMS_FOLDER_NAME);
    }

    public float getPixelsPerMeter() {
        return pixelsPerMeter;
    }

    public void setPixelsPerMeter(float pixelsPerMeter) {
        if (pixelsPerMeter <= 0 || Float.isNaN(pixelsPerMeter)) {
            throw new IllegalArgumentException("pixelsPerMeter must be positive, was " + pixelsPerMeter);
        }
        this.pixelsPerMeter = pixelsPerMeter;
    }
}
