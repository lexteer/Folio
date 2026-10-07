package lex.folio.model;

/** A rectangle centered on its position. */
public final class RectShape extends CollisionShape {
    private float width;
    private float height;

    public RectShape(int id, float x, float y, float width, float height, String tag) {
        super(id, x, y, tag);
        setSize(width, height);
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public void setSize(float width, float height) {
        if (!(width > 0 && height > 0)) {
            throw new IllegalArgumentException("size must be positive, was " + width + " x " + height);
        }
        this.width = width;
        this.height = height;
    }
}
