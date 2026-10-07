package lex.folio.scene.shape;

import com.badlogic.gdx.utils.FloatArray;
import lex.folio.scene.tool.Tool;

/** The shape that the user is drawing but has not finished, in world space. */
public class ShapeDraft {
    private Tool tool;
    private final FloatArray points = new FloatArray();
    private float hoverX;
    private float hoverY;

    public boolean isActive() {
        return tool != null;
    }

    /** The tool that is drawing the shape. */
    public Tool getTool() {
        return tool;
    }

    public void start(Tool tool, float worldX, float worldY) {
        this.tool = tool;
        points.clear();
        points.add(worldX, worldY);
        hoverTo(worldX, worldY);
    }

    public void addPoint(float worldX, float worldY) {
        points.add(worldX, worldY);
    }

    /** Where the mouse is now, which is the other end of the shape that is being drawn. */
    public void hoverTo(float worldX, float worldY) {
        hoverX = worldX;
        hoverY = worldY;
    }

    public int getPointCount() {
        return points.size / 2;
    }

    public float getPointX(int index) {
        return points.get(index * 2);
    }

    public float getPointY(int index) {
        return points.get(index * 2 + 1);
    }

    public float getHoverX() {
        return hoverX;
    }

    public float getHoverY() {
        return hoverY;
    }

    /** The points as x and y one after the other. */
    public float[] copyPoints() {
        return points.toArray();
    }

    public void clear() {
        tool = null;
        points.clear();
    }
}
