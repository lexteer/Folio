package lex.folio.scene;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ScreenUtils;

public class SceneRenderer implements Disposable {
    private static final Color BACKGROUND = new Color(0.14f, 0.14f, 0.16f, 1f);
    private static final Color X_AXIS_COLOR = new Color(0.85f, 0.32f, 0.32f, 1f);
    private static final Color Y_AXIS_COLOR = new Color(0.40f, 0.78f, 0.40f, 1f);

    private static final float SCREEN_PIXELS_PER_METER = 64f; // change later to the projects PPM

    private final OrthographicCamera camera = new OrthographicCamera();
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private FrameBuffer frameBuffer;

    // call before getTextureHandle()
    public void render(int width, int height) {
        ensureFrameBufferSize(width, height);
        updateCamera(width, height);

        frameBuffer.begin();
        ScreenUtils.clear(BACKGROUND);
        drawAxes();
        frameBuffer.end();
    }

    public int getTextureHandle() {
        return frameBuffer.getColorBufferTexture().getTextureObjectHandle();
    }

    private void ensureFrameBufferSize(int width, int height) {
        if (frameBuffer != null && frameBuffer.getWidth() == width && frameBuffer.getHeight() == height) {
            return;
        }
        if (frameBuffer != null) {
            frameBuffer.dispose();
        }
        frameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
    }

    private void updateCamera(int width, int height) {
        camera.viewportWidth = width / SCREEN_PIXELS_PER_METER;
        camera.viewportHeight = height / SCREEN_PIXELS_PER_METER;
        camera.update();
    }

    private void drawAxes() {
        float halfWidth = camera.viewportWidth * camera.zoom / 2f;
        float halfHeight = camera.viewportHeight * camera.zoom / 2f;
        float left = camera.position.x - halfWidth;
        float right = camera.position.x + halfWidth;
        float bottom = camera.position.y - halfHeight;
        float top = camera.position.y + halfHeight;

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        shapeRenderer.setColor(X_AXIS_COLOR);
        shapeRenderer.line(left, 0, right, 0);

        shapeRenderer.setColor(Y_AXIS_COLOR);
        shapeRenderer.line(0, bottom, 0, top);

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
