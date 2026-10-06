package lex.folio.scene;

import lex.folio.model.Room;
import lex.folio.model.Sprite;

public class SelectTool {
    private final SpritePicker spritePicker;
    private final Selection selection;
    private final SpriteDrag spriteDrag;

    public SelectTool(SpritePicker spritePicker, Selection selection, SpriteDrag spriteDrag) {
        this.spritePicker = spritePicker;
        this.selection = selection;
        this.spriteDrag = spriteDrag;
    }

    public void press(Room room, float worldX, float worldY) {
        Sprite sprite = spritePicker.findSpriteAt(room, worldX, worldY);
        if (sprite == null) {
            selection.clear();
            return;
        }
        selection.selectOnly(sprite);
        spriteDrag.start(sprite, worldX, worldY);
    }

    public boolean isDragging() {
        return spriteDrag.isActive();
    }

    public void drag(float worldX, float worldY) {
        spriteDrag.moveTo(worldX, worldY);
    }

    public void release() {
        spriteDrag.finish();
    }
}
