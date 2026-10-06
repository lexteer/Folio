package lex.folio.scene.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ScreenUtils;

/** The off-screen texture the scene is drawn into, recreated whenever the size changes. */
class SceneFrameBuffer implements Disposable {
    private static final Color BACKGROUND = new Color(0.14f, 0.14f, 0.16f, 1f);

    private FrameBuffer frameBuffer;

    /** Starts drawing into a cleared buffer of the given size. Pair with {@link #end()}. */
    void begin(int width, int height) {
        ensureSize(width, height);
        frameBuffer.begin();
        ScreenUtils.clear(BACKGROUND);
    }

    void end() {
        frameBuffer.end();
    }

    int getTextureHandle() {
        return frameBuffer.getColorBufferTexture().getTextureObjectHandle();
    }

    private void ensureSize(int width, int height) {
        if (frameBuffer != null && frameBuffer.getWidth() == width && frameBuffer.getHeight() == height) return;
        if (frameBuffer != null) frameBuffer.dispose();

        frameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
    }

    @Override
    public void dispose() {
        if (frameBuffer != null) {
            frameBuffer.dispose();
        }
    }
}
