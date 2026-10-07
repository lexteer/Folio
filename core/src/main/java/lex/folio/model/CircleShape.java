package lex.folio.model;

/** A circle centered on its position. */
public final class CircleShape extends CollisionShape {
    private float radius;

    public CircleShape(int id, float x, float y, float radius, String tag) {
        super(id, x, y, tag);
        setRadius(radius);
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        if (!(radius > 0)) throw new IllegalArgumentException("radius must be positive, was " + radius);
        this.radius = radius;
    }
}
