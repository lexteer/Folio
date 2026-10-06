package lex.folio.scene.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ScreenUtils;
import lex.folio.assets.AssetLibrary;
import lex.folio.model.Room;
import lex.folio.scene.camera.SceneCamera;
import lex.folio.scene.sprite.SpriteGeometry;

public class SceneRenderer implements Disposable {
    private static final Color BACKGROUND = new Color(0.14f, 0.14f, 0.16f, 1f);

    private final SceneCamera camera;
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final SpriteBatch spriteBatch = new SpriteBatch();
    private final GridRenderer gridRenderer;
    private final RoomRenderer roomRenderer;
    private FrameBuffer frameBuffer;

    public SceneRenderer(SceneCamera camera, AssetLibrary assetLibrary, SpriteGeometry spriteGeometry) {
        this.camera = camera;
        this.gridRenderer = new GridRenderer(shapeRenderer, camera);
        this.roomRenderer = new RoomRenderer(spriteBatch, assetLibrary, spriteGeometry);
    }

    // call before getTextureHandle()
    public void render(Room room, int width, int height) {
        ensureFrameBufferSize(width, height);
        camera.setScreenSize(width, height);
        shapeRenderer.setProjectionMatrix(camera.getProjection());
        spriteBatch.setProjectionMatrix(camera.getProjection());

        frameBuffer.begin();
        ScreenUtils.clear(BACKGROUND);
        gridRenderer.render();
        roomRenderer.render(room);
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

    @Override
    public void dispose() {
        if (frameBuffer != null) {
            frameBuffer.dispose();
        }
        shapeRenderer.dispose();
        spriteBatch.dispose();
    }
}
