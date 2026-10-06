package lex.folio.scene.render;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Disposable;
import lex.folio.assets.AssetLibrary;
import lex.folio.model.Room;
import lex.folio.scene.camera.SceneCamera;
import lex.folio.scene.sprite.SpriteGeometry;

/** Renders a room, with its grid, into a texture that the UI can show. */
public class SceneRenderer implements Disposable {
    private final SceneCamera camera;
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final SpriteBatch spriteBatch = new SpriteBatch();
    private final SceneFrameBuffer frameBuffer = new SceneFrameBuffer();
    private final GridRenderer gridRenderer;
    private final RoomRenderer roomRenderer;

    public SceneRenderer(SceneCamera camera, AssetLibrary assetLibrary, SpriteGeometry spriteGeometry) {
        this.camera = camera;
        this.gridRenderer = new GridRenderer(shapeRenderer, camera);
        this.roomRenderer = new RoomRenderer(spriteBatch, assetLibrary, spriteGeometry);
    }

    // call before getTextureHandle()
    public void render(Room room, int width, int height) {
        camera.setScreenSize(width, height);
        shapeRenderer.setProjectionMatrix(camera.getProjection());
        spriteBatch.setProjectionMatrix(camera.getProjection());

        frameBuffer.begin(width, height);
        gridRenderer.render();
        roomRenderer.render(room);
        frameBuffer.end();
    }

    public int getTextureHandle() {
        return frameBuffer.getTextureHandle();
    }

    @Override
    public void dispose() {
        frameBuffer.dispose();
        shapeRenderer.dispose();
        spriteBatch.dispose();
    }
}
