package lex.folio.scene;

import lex.folio.model.ImageAsset;
import lex.folio.model.Room;

public class PaintTool {
    private final ToolState toolState;
    private final SpritePlacer spritePlacer;

    public PaintTool(ToolState toolState, SpritePlacer spritePlacer) {
        this.toolState = toolState;
        this.spritePlacer = spritePlacer;
    }

    public void press(Room room, float worldX, float worldY) {
        ImageAsset asset = toolState.getArmedAsset();
        if (asset == null) return;

        spritePlacer.place(room, asset, worldX, worldY);
    }
}
