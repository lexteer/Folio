package lex.folio.scene;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class SceneCamera {
    private static final float SCREEN_PIXELS_PER_METER = 64f; // (view at 100% zoom) change later to the projects PPM
    private static final float MIN_ZOOM = 0.02f;
    private static final float MAX_ZOOM = 50f;

    private final OrthographicCamera camera = new OrthographicCamera();
    private int screenWidth;
    private int screenHeight;

    public void setScreenSize(int width, int height) {
        screenWidth = width;
        screenHeight = height;
        camera.viewportWidth = width / SCREEN_PIXELS_PER_METER;
        camera.viewportHeight = height / SCREEN_PIXELS_PER_METER;
        camera.update();
    }

    public void panByScreenPixels(float deltaX, float deltaY) {
        float metersPerPixel = getMetersPerScreenPixel();
        camera.position.x -= deltaX * metersPerPixel;
        camera.position.y += deltaY * metersPerPixel;
        camera.update();
    }

    public void zoomAt(float screenX, float screenY, float factor) {
        Vector2 pointBefore = screenToWorld(screenX, screenY);
        camera.zoom = MathUtils.clamp(camera.zoom * factor, MIN_ZOOM, MAX_ZOOM);
        Vector2 pointAfter = screenToWorld(screenX, screenY);

        camera.position.x += pointBefore.x - pointAfter.x;
        camera.position.y += pointBefore.y - pointAfter.y;
        camera.update();
    }

    public Vector2 screenToWorld(float screenX, float screenY) {
        float metersPerPixel = getMetersPerScreenPixel();
        float worldX = camera.position.x + (screenX - screenWidth / 2f) * metersPerPixel;
        float worldY = camera.position.y - (screenY - screenHeight / 2f) * metersPerPixel;
        return new Vector2(worldX, worldY);
    }

    public Rectangle getVisibleArea() {
        float width = camera.viewportWidth * camera.zoom;
        float height = camera.viewportHeight * camera.zoom;
        return new Rectangle(camera.position.x - width / 2f, camera.position.y - height / 2f, width, height);
    }

    public Matrix4 getProjection() {
        return camera.combined;
    }

    private float getMetersPerScreenPixel() {
        return camera.zoom / SCREEN_PIXELS_PER_METER;
    }
}
