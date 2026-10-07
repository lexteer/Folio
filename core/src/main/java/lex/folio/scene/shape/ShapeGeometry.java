package lex.folio.scene.shape;

import lex.folio.model.CircleShape;
import lex.folio.model.CollisionShape;
import lex.folio.model.EdgeChainShape;
import lex.folio.model.PointShape;
import lex.folio.model.PolygonShape;
import lex.folio.model.RectShape;

/** Where collision shapes are in the world. */
public final class ShapeGeometry {
    private ShapeGeometry() {
    }

    /** Whether the world position is inside the shape, or for an edge chain, within the tolerance of its line. */
    public static boolean contains(CollisionShape shape, float worldX, float worldY, float tolerance) {
        float x = worldX - shape.getX();
        float y = worldY - shape.getY();
        return switch (shape) {
            case RectShape rect -> Math.abs(x) <= rect.getWidth() / 2f && Math.abs(y) <= rect.getHeight() / 2f;
            case CircleShape circle -> x * x + y * y <= circle.getRadius() * circle.getRadius();
            case PolygonShape polygon -> insidePolygon(polygon, x, y);
            case EdgeChainShape chain -> distanceToChain(chain, x, y) <= tolerance;
        };
    }

    /** Whether the shape's bounding box touches the world rectangle. */
    public static boolean overlaps(CollisionShape shape, float minX, float minY, float maxX, float maxY) {
        float[] bounds = getBounds(shape);
        return bounds[0] <= maxX && bounds[2] >= minX && bounds[1] <= maxY && bounds[3] >= minY;
    }

    /** The world bounding box as minX, minY, maxX, maxY. */
    public static float[] getBounds(CollisionShape shape) {
        float x = shape.getX();
        float y = shape.getY();
        return switch (shape) {
            case RectShape rect -> new float[]{x - rect.getWidth() / 2f, y - rect.getHeight() / 2f,
                x + rect.getWidth() / 2f, y + rect.getHeight() / 2f};
            case CircleShape circle -> new float[]{x - circle.getRadius(), y - circle.getRadius(),
                x + circle.getRadius(), y + circle.getRadius()};
            case PointShape points -> pointBounds(points, x, y);
        };
    }

    private static float[] pointBounds(PointShape shape, float x, float y) {
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;
        for (int i = 0; i < shape.getPointCount(); i++) {
            minX = Math.min(minX, x + shape.getPointX(i));
            minY = Math.min(minY, y + shape.getPointY(i));
            maxX = Math.max(maxX, x + shape.getPointX(i));
            maxY = Math.max(maxY, y + shape.getPointY(i));
        }
        return new float[]{minX, minY, maxX, maxY};
    }

    /** The even-odd rule, with the position relative to the shape. */
    private static boolean insidePolygon(PolygonShape polygon, float x, float y) {
        boolean inside = false;
        int count = polygon.getPointCount();
        for (int i = 0, previous = count - 1; i < count; previous = i++) {
            float ax = polygon.getPointX(i);
            float ay = polygon.getPointY(i);
            float bx = polygon.getPointX(previous);
            float by = polygon.getPointY(previous);
            if ((ay > y) != (by > y) && x < (bx - ax) * (y - ay) / (by - ay) + ax) {
                inside = !inside;
            }
        }
        return inside;
    }

    private static float distanceToChain(EdgeChainShape chain, float x, float y) {
        float nearest = Float.MAX_VALUE;
        for (int i = 1; i < chain.getPointCount(); i++) {
            nearest = Math.min(nearest, distanceToSegment(x, y,
                chain.getPointX(i - 1), chain.getPointY(i - 1), chain.getPointX(i), chain.getPointY(i)));
        }
        return nearest;
    }

    private static float distanceToSegment(float x, float y, float ax, float ay, float bx, float by) {
        float dx = bx - ax;
        float dy = by - ay;
        float lengthSquared = dx * dx + dy * dy;
        float along = lengthSquared == 0 ? 0 : Math.clamp(((x - ax) * dx + (y - ay) * dy) / lengthSquared, 0f, 1f);
        float nearestX = ax + along * dx;
        float nearestY = ay + along * dy;
        return (float) Math.hypot(x - nearestX, y - nearestY);
    }
}
