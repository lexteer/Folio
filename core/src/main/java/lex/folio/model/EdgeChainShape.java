package lex.folio.model;

/** An open line of at least two points, such as a floor or a wall. */
public final class EdgeChainShape extends PointShape {
    public static final int MIN_POINTS = 2;

    public EdgeChainShape(int id, float x, float y, float[] points, String tag) {
        super(id, x, y, points, tag, MIN_POINTS);
    }
}
