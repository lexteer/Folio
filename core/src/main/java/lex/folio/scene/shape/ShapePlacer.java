package lex.folio.scene.shape;

import lex.folio.command.AddObjectCommand;
import lex.folio.command.CommandStack;
import lex.folio.model.CircleShape;
import lex.folio.model.CollisionLayer;
import lex.folio.model.CollisionShape;
import lex.folio.model.CollisionTags;
import lex.folio.model.EdgeChainShape;
import lex.folio.model.Layer;
import lex.folio.model.PolygonShape;
import lex.folio.model.RectShape;
import lex.folio.model.Room;
import lex.folio.scene.ActiveLayers;
import lex.folio.scene.Selection;

import java.util.function.IntFunction;

/** Adds new collision shapes on top of the active layer of a room, and selects them. */
public class ShapePlacer {
    /** Smaller than this, in meters, a dragged out shape counts as a stray click. */
    private static final float MIN_SIZE = 0.01f;

    private final CommandStack commandStack;
    private final ActiveLayers activeLayers;
    private final Selection selection;
    private final CollisionTags tags;

    public ShapePlacer(CommandStack commandStack, ActiveLayers activeLayers, Selection selection,
                       CollisionTags tags) {
        this.commandStack = commandStack;
        this.activeLayers = activeLayers;
        this.selection = selection;
        this.tags = tags;
    }

    /** The layer a shape would be placed on now, or null if the active layer is not a collision layer or is locked. */
    public CollisionLayer findTargetLayer(Room room) {
        Layer<?> active = activeLayers.get(room);
        return active instanceof CollisionLayer layer && layer.isEditable() ? layer : null;
    }

    /** Adds the rectangle between two opposite corners. */
    public void addRect(Room room, float x1, float y1, float x2, float y2) {
        float width = Math.abs(x2 - x1);
        float height = Math.abs(y2 - y1);
        if (width < MIN_SIZE || height < MIN_SIZE) return;

        add(room, id -> new RectShape(id, (x1 + x2) / 2f, (y1 + y2) / 2f, width, height, defaultTag()));
    }

    public void addCircle(Room room, float centerX, float centerY, float edgeX, float edgeY) {
        float radius = (float) Math.hypot(edgeX - centerX, edgeY - centerY);
        if (radius < MIN_SIZE / 2f) return;

        add(room, id -> new CircleShape(id, centerX, centerY, radius, defaultTag()));
    }

    /** @param worldPoints x and y of each point, one after the other */
    public void addPolygon(Room room, float[] worldPoints) {
        if (worldPoints.length / 2 < PolygonShape.MIN_POINTS) return;

        float[] bounds = boundsOf(worldPoints);
        float[] local = relativeTo(worldPoints, bounds);
        add(room, id -> new PolygonShape(id, bounds[0], bounds[1], local, defaultTag()));
    }

    public void addEdgeChain(Room room, float[] worldPoints) {
        if (worldPoints.length / 2 < EdgeChainShape.MIN_POINTS) return;

        float[] bounds = boundsOf(worldPoints);
        float[] local = relativeTo(worldPoints, bounds);
        add(room, id -> new EdgeChainShape(id, bounds[0], bounds[1], local, defaultTag()));
    }

    private String defaultTag() {
        return tags.getDefault().name();
    }

    private void add(Room room, IntFunction<CollisionShape> create) {
        CollisionLayer layer = findTargetLayer(room);
        if (layer == null) return;

        CollisionShape shape = create.apply(room.createId());
        commandStack.execute(new AddObjectCommand<>(layer, shape));
        selection.selectOnly(shape);
    }

    /** The center of the bounding box, which becomes the position of the shape, as x and y. */
    private static float[] boundsOf(float[] points) {
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;
        for (int i = 0; i < points.length; i += 2) {
            minX = Math.min(minX, points[i]);
            maxX = Math.max(maxX, points[i]);
            minY = Math.min(minY, points[i + 1]);
            maxY = Math.max(maxY, points[i + 1]);
        }
        return new float[]{(minX + maxX) / 2f, (minY + maxY) / 2f};
    }

    private static float[] relativeTo(float[] points, float[] origin) {
        float[] local = new float[points.length];
        for (int i = 0; i < points.length; i += 2) {
            local[i] = points[i] - origin[0];
            local[i + 1] = points[i + 1] - origin[1];
        }
        return local;
    }
}
