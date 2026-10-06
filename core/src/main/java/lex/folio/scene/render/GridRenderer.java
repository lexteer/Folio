package lex.folio.scene.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import lex.folio.scene.camera.SceneCamera;

public class GridRenderer {
    private static final Color GRID_COLOR = new Color(0.18f, 0.18f, 0.205f, 1f);
    private static final Color X_AXIS_COLOR = new Color(0.85f, 0.32f, 0.32f, 1f);
    private static final Color Y_AXIS_COLOR = new Color(0.40f, 0.78f, 0.40f, 1f);

    private static final float GRID_CELL_SIZE = 1f; // meters
    private static final float MIN_GRID_CELL_PIXELS = 8f;

    private final ShapeRenderer shapeRenderer;

    public GridRenderer(ShapeRenderer shapeRenderer) {
        this.shapeRenderer = shapeRenderer;
    }

    /** Expects the shape renderer's projection matrix to already be set. */
    public void render(SceneCamera camera) {
        drawGrid(camera);
        drawAxes(camera);
    }

    private void drawGrid(SceneCamera camera) {
        if (!isGridCellLargeEnough(camera)) {
            return;
        }
        Rectangle visible = camera.getVisibleArea();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(GRID_COLOR);
        drawVerticalGridLines(visible);
        drawHorizontalGridLines(visible);
        shapeRenderer.end();
    }

    private boolean isGridCellLargeEnough(SceneCamera camera) {
        float cellSizeInPixels = GRID_CELL_SIZE / camera.getMetersPerScreenPixel();
        return cellSizeInPixels >= MIN_GRID_CELL_PIXELS;
    }

    private void drawVerticalGridLines(Rectangle visible) {
        int firstColumn = MathUtils.floor(visible.x / GRID_CELL_SIZE);
        int lastColumn = MathUtils.ceil((visible.x + visible.width) / GRID_CELL_SIZE);
        float top = visible.y + visible.height;

        for (int column = firstColumn; column <= lastColumn; column++) {
            float x = column * GRID_CELL_SIZE;
            shapeRenderer.line(x, visible.y, x, top);
        }
    }

    private void drawHorizontalGridLines(Rectangle visible) {
        int firstRow = MathUtils.floor(visible.y / GRID_CELL_SIZE);
        int lastRow = MathUtils.ceil((visible.y + visible.height) / GRID_CELL_SIZE);
        float right = visible.x + visible.width;

        for (int row = firstRow; row <= lastRow; row++) {
            float y = row * GRID_CELL_SIZE;
            shapeRenderer.line(visible.x, y, right, y);
        }
    }

    private void drawAxes(SceneCamera camera) {
        Rectangle visible = camera.getVisibleArea();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(X_AXIS_COLOR);
        shapeRenderer.line(visible.x, 0, visible.x + visible.width, 0);
        shapeRenderer.setColor(Y_AXIS_COLOR);
        shapeRenderer.line(0, visible.y, 0, visible.y + visible.height);
        shapeRenderer.end();
    }
}
