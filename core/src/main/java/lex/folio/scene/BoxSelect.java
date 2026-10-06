package lex.folio.scene;

/** The rectangle, in world space, that the user is dragging out to select objects. */
public class BoxSelect {
    private boolean active;
    private float startX;
    private float startY;
    private float endX;
    private float endY;

    public boolean isActive() {
        return active;
    }

    public void start(float worldX, float worldY) {
        active = true;
        startX = endX = worldX;
        startY = endY = worldY;
    }

    public void moveTo(float worldX, float worldY) {
        endX = worldX;
        endY = worldY;
    }

    public void finish() {
        active = false;
    }

    public float getMinX() {
        return Math.min(startX, endX);
    }

    public float getMinY() {
        return Math.min(startY, endY);
    }

    public float getMaxX() {
        return Math.max(startX, endX);
    }

    public float getMaxY() {
        return Math.max(startY, endY);
    }
}
