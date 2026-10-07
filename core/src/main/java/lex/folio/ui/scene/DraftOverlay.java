package lex.folio.ui.scene;

import lex.folio.model.CollisionTags;
import lex.folio.scene.shape.ShapeDraft;

import java.util.Arrays;

/** Shows the shape that is being drawn but is not finished yet. */
class DraftOverlay {
    private static final float THICKNESS = 1.5f;

    private final ShapeDrawer drawer;
    private final ShapeDraft draft;
    private final CollisionTags tags;

    DraftOverlay(ShapeDrawer drawer, ShapeDraft draft, CollisionTags tags) {
        this.drawer = drawer;
        this.draft = draft;
        this.tags = tags;
    }

    void draw() {
        if (!draft.isActive()) return;

        int rgb = tags.getDefault().rgb();
        int line = CollisionOverlay.color(rgb, 0.9f);
        int fill = CollisionOverlay.color(rgb, 0.2f);
        float startX = draft.getPointX(0);
        float startY = draft.getPointY(0);
        switch (draft.getTool()) {
            case RECT -> drawer.drawPolygon(new float[]{startX, startY, draft.getHoverX(), startY,
                draft.getHoverX(), draft.getHoverY(), startX, draft.getHoverY()}, line, fill, THICKNESS, false);
            case CIRCLE -> drawer.drawCircle(startX, startY,
                (float) Math.hypot(draft.getHoverX() - startX, draft.getHoverY() - startY), line, fill, THICKNESS);
            case POLYGON, EDGE_CHAIN -> drawPoints(line);
            default -> throw new IllegalStateException("Not a shape tool: " + draft.getTool());
        }
    }

    /** The points so far, and a line from the last one to the mouse. */
    private void drawPoints(int line) {
        float[] points = draft.copyPoints();
        float[] withMouse = Arrays.copyOf(points, points.length + 2);
        withMouse[points.length] = draft.getHoverX();
        withMouse[points.length + 1] = draft.getHoverY();
        drawer.drawLine(withMouse, false, line, THICKNESS, false);
        drawer.drawLine(points, false, line, THICKNESS, true);
    }
}
