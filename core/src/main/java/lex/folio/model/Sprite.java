package lex.folio.model;

import java.util.Objects;

public final class Sprite extends RoomObject {
    public static final int NO_TINT = 0xFFFFFFFF;

    private String assetId;
    private float rotationDegrees;
    private float scaleX = 1f;
    private float scaleY = 1f;
    private boolean flipX;
    private boolean flipY;
    private int tint = NO_TINT;

    public Sprite(int id, String assetId, float x, float y) {
        super(id, x, y);
        setAssetId(assetId);
    }

    public String getAssetId() {
        return assetId;
    }

    public void setAssetId(String assetId) {
        this.assetId = Objects.requireNonNull(assetId, "assetId");
    }

    public float getRotationDegrees() {
        return rotationDegrees;
    }

    public void setRotationDegrees(float rotationDegrees) {
        this.rotationDegrees = rotationDegrees;
    }

    public float getScaleX() {
        return scaleX;
    }

    public float getScaleY() {
        return scaleY;
    }

    public void setScale(float scaleX, float scaleY) {
        this.scaleX = scaleX;
        this.scaleY = scaleY;
    }

    public boolean isFlipX() {
        return flipX;
    }

    public void setFlipX(boolean flipX) {
        this.flipX = flipX;
    }

    public boolean isFlipY() {
        return flipY;
    }

    public void setFlipY(boolean flipY) {
        this.flipY = flipY;
    }

    public int getTint() {
        return tint;
    }

    public void setTint(int tint) {
        this.tint = tint;
    }
}
