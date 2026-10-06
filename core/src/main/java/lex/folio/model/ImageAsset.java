package lex.folio.model;

import java.util.Objects;

public final class ImageAsset {
    private String id;
    private String path;

    public ImageAsset(String id, String path) {
        this.id = requireValidId(id);
        setPath(path);
    }

    public String getId() {
        return id;
    }

    /** Only {@link Project#renameAsset} changes the id, because the project looks assets up by it. */
    void setId(String id) {
        this.id = requireValidId(id);
    }

    public String getPath() {
        return path;
    }

    private static String requireValidId(String id) {
        Objects.requireNonNull(id, "id");
        if (id.isBlank()) throw new IllegalArgumentException("Asset id must not be blank");
        return id;
    }

    public String getFolder() {
        return AssetFolderPath.getParent(path);
    }

    public void setPath(String path) {
        this.path = Objects.requireNonNull(path, "path");
    }

    public String getFileName() {
        return AssetFolderPath.getName(path);
    }
}
