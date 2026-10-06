package lex.folio.scene.tool;

import lex.folio.model.Room;
import lex.folio.model.Sprite;
import lex.folio.scene.Selection;
import lex.folio.scene.sprite.SpriteDrag;
import lex.folio.scene.sprite.SpritePicker;

public class SelectTool implements SceneTool {
    private final SpritePicker spritePicker;
    private final Selection selection;
    private final SpriteDrag spriteDrag;

    public SelectTool(SpritePicker spritePicker, Selection selection, SpriteDrag spriteDrag) {
        this.spritePicker = spritePicker;
        this.selection = selection;
        this.spriteDrag = spriteDrag;
    }

    @Override
    public void press(Room room, float worldX, float worldY) {
        Sprite sprite = spritePicker.findSpriteAt(room, worldX, worldY);
        if (sprite == null) {
            selection.clear();
            return;
        }
        selection.selectOnly(sprite);
        spriteDrag.start(sprite, worldX, worldY);
    }

    @Override
    public boolean isDragging() {
        return spriteDrag.isActive();
    }

    @Override
    public void drag(float worldX, float worldY) {
        spriteDrag.moveTo(worldX, worldY);
    }

    @Override
    public void release() {
        spriteDrag.finish();
    }

    @Override
    public void cancel() {
        spriteDrag.cancel();
    }
}
