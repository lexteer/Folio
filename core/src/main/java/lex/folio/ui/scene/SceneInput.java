package lex.folio.ui.scene;

import lex.folio.command.ItemOrder;
import lex.folio.model.Room;
import lex.folio.scene.ObjectPicker;
import lex.folio.scene.Selection;
import lex.folio.scene.sprite.SpritePlacer;
import lex.folio.scene.tool.ToolController;

import java.util.function.Consumer;

/** Everything the user can do to the scene with the mouse. */
public class SceneInput {
    private final SceneCameraControls cameraControls;
    private final SceneToolInput toolInput;
    private final SceneAssetDrop assetDrop;
    private final SceneContextMenu contextMenu;

    public SceneInput(SceneViewport viewport, ToolController tools, SpritePlacer spritePlacer, ObjectPicker picker,
                      Selection selection, Consumer<ItemOrder> reorder) {
        this.cameraControls = new SceneCameraControls(viewport);
        this.toolInput = new SceneToolInput(viewport, tools);
        this.assetDrop = new SceneAssetDrop(viewport, spritePlacer);
        this.contextMenu = new SceneContextMenu(viewport, picker, selection, reorder);
    }

    /** Call right after drawing the scene image: the drop target is the last item. */
    void acceptDrops(Room room) {
        assetDrop.handle(room);
    }

    void handle(Room room, boolean hovered) {
        toolInput.handle(room, hovered);
        cameraControls.handle(hovered);
        contextMenu.handle(room, hovered);
    }
}
