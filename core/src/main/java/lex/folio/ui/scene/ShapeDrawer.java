package lex.folio.ui.scene;

import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ShortArray;
import imgui.ImDrawList;
import imgui.ImGui;
import imgui.flag.ImDrawListFlags;
import lex.folio.model.CircleShape;
import lex.folio.model.CollisionShape;
import lex.folio.model.EdgeChainShape;
import lex.folio.model.PolygonShape;
import lex.folio.model.RectShape;

/** Draws collision shapes, in screen space. */
class ShapeDrawer {
    private static final int CIRCLE_SEGMENTS = 64;
    private static final float VERTEX_RADIUS = 3.5f;

    private final SceneViewport viewport;
    private final EarClippingTriangulator triangulator = new EarClippingTriangulator();

    ShapeDrawer(SceneViewport viewport) {
        this.viewport = viewport;
    }

    /** @param fill the color of the inside, which edge chains do not have */
    void draw(CollisionShape shape, int outline, int fill, float thickness, boolean showVertices) {
        ImDrawList drawList = ImGui.getWindowDrawList();
        switch (shape) {
            case RectShape rect -> {
                Vector2 a = viewport.worldToScreen(shape.getX() - rect.getWidth() / 2f, shape.getY() - rect.getHeight() / 2f);
                Vector2 b = viewport.worldToScreen(shape.getX() + rect.getWidth() / 2f, shape.getY() + rect.getHeight() / 2f);
                drawList.addRectFilled(Math.min(a.x, b.x), Math.min(a.y, b.y), Math.max(a.x, b.x), Math.max(a.y, b.y), fill);
                drawList.addRect(Math.min(a.x, b.x), Math.min(a.y, b.y), Math.max(a.x, b.x), Math.max(a.y, b.y),
                    outline, 0f, 0, thickness);
            }
            case CircleShape circle -> drawCircle(shape.getX(), shape.getY(), circle.getRadius(), outline, fill, thickness);
            case PolygonShape polygon -> {
                float[] points = worldPoints(polygon.getPointCount(), shape, polygon::getPointX, polygon::getPointY);
                drawPolygon(points, outline, fill, thickness, showVertices);
            }
            case EdgeChainShape chain -> {
                float[] points = worldPoints(chain.getPointCount(), shape, chain::getPointX, chain::getPointY);
                drawLine(points, false, outline, thickness, showVertices);
            }
        }
    }

    void drawCircle(float worldX, float worldY, float radius, int outline, int fill, float thickness) {
        Vector2 center = viewport.worldToScreen(worldX, worldY);
        float pixels = radius / viewport.getCamera().getMetersPerScreenPixel();
        ImDrawList drawList = ImGui.getWindowDrawList();
        drawList.addCircleFilled(center.x, center.y, pixels, fill, CIRCLE_SEGMENTS);
        drawList.addCircle(center.x, center.y, pixels, outline, CIRCLE_SEGMENTS, thickness);
    }

    /** @param worldPoints x and y of each point, one after the other */
    void drawPolygon(float[] worldPoints, int outline, int fill, float thickness, boolean showVertices) {
        float[] screen = toScreen(worldPoints);
        if (screen.length >= 6) {
            ShortArray triangles = triangulator.computeTriangles(screen);
            ImDrawList drawList = ImGui.getWindowDrawList();
            // Smoothed edges would show as seams between the triangles, and the outline is smooth anyway.
            int flags = drawList.getFlags();
            drawList.setFlags(flags & ~ImDrawListFlags.AntiAliasedFill);
            for (int i = 0; i < triangles.size; i += 3) {
                int a = triangles.get(i) * 2;
                int b = triangles.get(i + 1) * 2;
                int c = triangles.get(i + 2) * 2;
                drawList.addTriangleFilled(screen[a], screen[a + 1], screen[b], screen[b + 1], screen[c], screen[c + 1], fill);
            }
            drawList.setFlags(flags);
        }
        drawScreenLine(screen, true, outline, thickness, showVertices);
    }

    /** @param worldPoints x and y of each point, one after the other */
    void drawLine(float[] worldPoints, boolean closed, int color, float thickness, boolean showVertices) {
        drawScreenLine(toScreen(worldPoints), closed, color, thickness, showVertices);
    }

    private static void drawScreenLine(float[] screen, boolean closed, int color, float thickness,
                                       boolean showVertices) {
        ImDrawList drawList = ImGui.getWindowDrawList();
        int count = screen.length / 2;
        for (int i = 1; i < count; i++) {
            drawList.addLine(screen[i * 2 - 2], screen[i * 2 - 1], screen[i * 2], screen[i * 2 + 1], color, thickness);
        }
        if (closed && count > 2) {
            drawList.addLine(screen[count * 2 - 2], screen[count * 2 - 1], screen[0], screen[1], color, thickness);
        }
        if (showVertices) {
            for (int i = 0; i < count; i++) {
                drawList.addCircleFilled(screen[i * 2], screen[i * 2 + 1], VERTEX_RADIUS, color, 12);
            }
        }
    }

    private float[] toScreen(float[] worldPoints) {
        float[] screen = new float[worldPoints.length];
        for (int i = 0; i < worldPoints.length; i += 2) {
            Vector2 point = viewport.worldToScreen(worldPoints[i], worldPoints[i + 1]);
            screen[i] = point.x;
            screen[i + 1] = point.y;
        }
        return screen;
    }

    private interface PointReader {
        float read(int index);
    }

    private static float[] worldPoints(int count, CollisionShape shape, PointReader x, PointReader y) {
        float[] points = new float[count * 2];
        for (int i = 0; i < count; i++) {
            points[i * 2] = shape.getX() + x.read(i);
            points[i * 2 + 1] = shape.getY() + y.read(i);
        }
        return points;
    }
}
