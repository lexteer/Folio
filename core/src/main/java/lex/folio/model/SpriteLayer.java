package lex.folio.model;

public final class SpriteLayer extends Layer<Sprite> {
    private float parallaxX = 1f;
    private float parallaxY = 1f;
    private float opacity = 1f;

    public SpriteLayer(int id, String name) {
        super(id, name);
    }

    /** 1 = gameplay plane, below 1 = background (moves slower), above 1 = foreground (moves faster) */
    public float getParallaxX() {
        return parallaxX;
    }

    public float getParallaxY() {
        return parallaxY;
    }

    public void setParallax(float parallaxX, float parallaxY) {
        this.parallaxX = parallaxX;
        this.parallaxY = parallaxY;
    }

    /** 0 = invisible, 1 = fully opaque */
    public float getOpacity() {
        return opacity;
    }

    public void setOpacity(float opacity) {
        this.opacity = Math.clamp(opacity, 0f, 1f);
    }
}
