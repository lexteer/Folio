package lex.folio.model;

import java.util.Objects;

/** A shape made of points, which are relative to the position of the shape. */
public abstract sealed class PointShape extends CollisionShape permits PolygonShape, EdgeChainShape {
    private final float[] points;

    /** @param points x and y of each point, one after the other */
    protected PointShape(int id, float x, float y, float[] points, String tag, int minPoints) {
        super(id, x, y, tag);
        Objects.requireNonNull(points, "points");
        if (points.length % 2 != 0 || points.length / 2 < minPoints) {
            throw new IllegalArgumentException("Need at least " + minPoints + " points, got " + points.length / 2);
        }
        this.points = points.clone();
    }

    public int getPointCount() {
        return points.length / 2;
    }

    public float getPointX(int index) {
        return points[index * 2];
    }

    public float getPointY(int index) {
        return points[index * 2 + 1];
    }
}
