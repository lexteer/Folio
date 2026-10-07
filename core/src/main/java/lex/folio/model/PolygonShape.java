package lex.folio.model;

/** A closed shape of at least three points. */
public final class PolygonShape extends PointShape {
    public static final int MIN_POINTS = 3;

    public PolygonShape(int id, float x, float y, float[] points, String tag) {
        super(id, x, y, points, tag, MIN_POINTS);
    }
}
