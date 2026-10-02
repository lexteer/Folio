package lex.folio.scene;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ScreenUtils;

public class SceneRenderer implements Disposable {
    private static final Color BACKGROUND = new Color(0.14f, 0.14f, 0.16f, 1f);
    private static final Color X_AXIS_COLOR = new Color(0.85f, 0.32f, 0.32f, 1f);
    private static final Color Y_AXIS_COLOR = new Color(0.40f, 0.78f, 0.40f, 1f);

    private final SceneCamera camera;
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private FrameBuffer frameBuffer;

    public SceneRenderer(SceneCamera camera) {
        this.camera = camera;
    }

    // call before getTextureHandle()
    public void render(int width, int height) {
        ensureFrameBufferSize(width, height);
        camera.setScreenSize(width, height);

        frameBuffer.begin();
        ScreenUtils.clear(BACKGROUND);
        drawAxes();
        frameBuffer.end();
    }

    public int getTextureHandle() {
        return frameBuffer.getColorBufferTexture().getTextureObjectHandle();
    }

    private void ensureFrameBufferSize(int width, int height) {
        if (frameBuffer != null && frameBuffer.getWidth() == width && frameBuffer.getHeight() == height) return;
        if (frameBuffer != null) frameBuffer.dispose();

        frameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
    }

    private void drawAxes() {
        Rectangle visible = camera.getVisibleArea();

        shapeRenderer.setProjectionMatrix(camera.getProjection());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(X_AXIS_COLOR);
        shapeRenderer.line(visible.x, 0, visible.x + visible.width, 0);
        shapeRenderer.setColor(Y_AXIS_COLOR);
        shapeRenderer.line(0, visible.y, 0, visible.y + visible.height);
        shapeRenderer.end();
    }

    @Override
    public void dispose() {
        if (frameBuffer != null) {
            frameBuffer.dispose();
        }
        shapeRenderer.dispose();
    }
}
