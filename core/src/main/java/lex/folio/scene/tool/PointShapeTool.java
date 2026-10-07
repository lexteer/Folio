package lex.folio.scene.tool;

import lex.folio.model.EdgeChainShape;
import lex.folio.model.PolygonShape;
import lex.folio.model.Room;
import lex.folio.scene.shape.ShapeDraft;
import lex.folio.scene.shape.ShapePlacer;

import java.util.function.DoubleSupplier;

/**
 * Click to place the points of a polygon or an edge chain. Double click or Enter completes it, and so does a click on
 * the first point of a polygon. Escape abandons it.
 */
public class PointShapeTool implements SceneTool {
    /** How many pixels from a point a click counts as on that point. */
    public static final float CLICK_RADIUS_IN_PIXELS = 8f;

    private final Tool kind;
    private final ShapeDraft draft;
    private final ShapePlacer placer;
    private final DoubleSupplier worldPerPixel;
    private Room room;

    /** @param worldPerPixel how many meters a screen pixel is, which depends on the zoom */
    public PointShapeTool(Tool kind, ShapeDraft draft, ShapePlacer placer, DoubleSupplier worldPerPixel) {
        if (kind != Tool.POLYGON && kind != Tool.EDGE_CHAIN) {
            throw new IllegalArgumentException("Not a point tool: " + kind);
        }
        this.kind = kind;
        this.draft = draft;
        this.placer = placer;
        this.worldPerPixel = worldPerPixel;
    }

    @Override
    public void press(Room room, float worldX, float worldY, boolean additive) {
        if (!draft.isActive()) {
            if (placer.findTargetLayer(room) == null) return;

            this.room = room;
            draft.start(kind, worldX, worldY);
            return;
        }

        int last = draft.getPointCount() - 1;
        if (isNear(last, worldX, worldY)) return; // The second click of a double click.

        if (kind == Tool.POLYGON && draft.getPointCount() >= PolygonShape.MIN_POINTS && isNear(0, worldX, worldY)) {
            finish();
            return;
        }
        draft.addPoint(worldX, worldY);
    }

    private boolean isNear(int pointIndex, float worldX, float worldY) {
        float radius = (float) (CLICK_RADIUS_IN_PIXELS * worldPerPixel.getAsDouble());
        return Math.hypot(draft.getPointX(pointIndex) - worldX, draft.getPointY(pointIndex) - worldY) <= radius;
    }

    @Override
    public boolean isBuilding() {
        return draft.isActive();
    }

    @Override
    public void hover(float worldX, float worldY) {
        draft.hoverTo(worldX, worldY);
    }

    @Override
    public void doubleClick(Room room, float worldX, float worldY) {
        finish();
    }

    @Override
    public void finish() {
        if (!draft.isActive()) return;

        int needed = kind == Tool.POLYGON ? PolygonShape.MIN_POINTS : EdgeChainShape.MIN_POINTS;
        if (draft.getPointCount() >= needed) {
            if (kind == Tool.POLYGON) {
                placer.addPolygon(room, draft.copyPoints());
            } else {
                placer.addEdgeChain(room, draft.copyPoints());
            }
        }
        draft.clear();
    }

    @Override
    public void cancel() {
        draft.clear();
    }
}
