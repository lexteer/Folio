package lex.folio.scene.tool;

import lex.folio.model.Room;
import lex.folio.scene.shape.ShapeDraft;
import lex.folio.scene.shape.ShapePlacer;

/** Drags out a rectangle between two corners, or a circle from its center to its edge. */
public class DragShapeTool implements SceneTool {
    private final Tool kind;
    private final ShapeDraft draft;
    private final ShapePlacer placer;
    private Room room;

    public DragShapeTool(Tool kind, ShapeDraft draft, ShapePlacer placer) {
        if (kind != Tool.RECT && kind != Tool.CIRCLE) throw new IllegalArgumentException("Not a drag tool: " + kind);
        this.kind = kind;
        this.draft = draft;
        this.placer = placer;
    }

    @Override
    public void press(Room room, float worldX, float worldY, boolean additive) {
        if (placer.findTargetLayer(room) == null) return;

        this.room = room;
        draft.start(kind, worldX, worldY);
    }

    @Override
    public boolean isDragging() {
        return draft.isActive();
    }

    @Override
    public void drag(float worldX, float worldY) {
        draft.hoverTo(worldX, worldY);
    }

    @Override
    public void release() {
        if (!draft.isActive()) return;

        float startX = draft.getPointX(0);
        float startY = draft.getPointY(0);
        if (kind == Tool.RECT) {
            placer.addRect(room, startX, startY, draft.getHoverX(), draft.getHoverY());
        } else {
            placer.addCircle(room, startX, startY, draft.getHoverX(), draft.getHoverY());
        }
        draft.clear();
    }

    @Override
    public void cancel() {
        draft.clear();
    }
}
