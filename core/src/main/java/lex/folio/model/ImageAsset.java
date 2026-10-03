package lex.folio.model;

import java.util.Objects;

public final class ImageAsset {
    private final String id;
    private final String path;

    public ImageAsset(String id, String path) {
        this.id = requireValidId(id);
        this.path = Objects.requireNonNull(path, "path");
    }

    public String getId() {
        return id;
    }

    public String getPath() {
        return path;
    }

    private static String requireValidId(String id) {
        Objects.requireNonNull(id, "id");
        if (id.isBlank()) throw new IllegalArgumentException("Asset id must not be blank");
        return id;
    }
}
