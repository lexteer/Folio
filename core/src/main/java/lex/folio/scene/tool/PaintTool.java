package lex.folio.scene.tool;

import lex.folio.model.ImageAsset;
import lex.folio.model.Room;
import lex.folio.scene.sprite.SpritePlacer;

public class PaintTool implements SceneTool {
    private final ToolState toolState;
    private final SpritePlacer spritePlacer;

    public PaintTool(ToolState toolState, SpritePlacer spritePlacer) {
        this.toolState = toolState;
        this.spritePlacer = spritePlacer;
    }

    @Override
    public void press(Room room, float worldX, float worldY, boolean additive) {
        ImageAsset asset = toolState.getArmedAsset();
        if (asset == null) return;

        spritePlacer.place(room, asset, worldX, worldY);
    }
}
