package lex.folio.scene;

import lex.folio.model.ImageAsset;
import lex.folio.model.Room;

public class SceneTools {
    private final ToolState toolState;
    private final SelectTool selectTool;
    private final PaintTool paintTool;
    private final SpritePlacer spritePlacer;

    public SceneTools(ToolState toolState, SelectTool selectTool, PaintTool paintTool, SpritePlacer spritePlacer) {
        this.toolState = toolState;
        this.selectTool = selectTool;
        this.paintTool = paintTool;
        this.spritePlacer = spritePlacer;
    }

    public void dropAsset(Room room, ImageAsset asset, float worldX, float worldY) {
        spritePlacer.place(room, asset, worldX, worldY);
    }

    public ToolState getToolState() {
        return toolState;
    }

    public void press(Room room, float worldX, float worldY) {
        switch (toolState.getTool()) {
            case SELECT -> selectTool.press(room, worldX, worldY);
            case PAINT -> paintTool.press(room, worldX, worldY);
        }
    }

    public boolean isDragging() {
        return selectTool.isDragging();
    }

    public void drag(float worldX, float worldY) {
        selectTool.drag(worldX, worldY);
    }

    public void release() {
        selectTool.release();
    }
}
