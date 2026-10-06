package lex.folio.ui.scene;

import com.badlogic.gdx.math.Vector2;
import lex.folio.model.ImageAsset;
import lex.folio.model.Room;
import lex.folio.scene.sprite.SpritePlacer;
import lex.folio.ui.assets.AssetDragDrop;

/** Places an asset dragged from the assets panel where it is dropped on the scene. */
class SceneAssetDrop {
    private final SceneViewport viewport;
    private final SpritePlacer spritePlacer;

    SceneAssetDrop(SceneViewport viewport, SpritePlacer spritePlacer) {
        this.viewport = viewport;
        this.spritePlacer = spritePlacer;
    }

    /** Call right after drawing the scene image: the drop target is the last item. */
    void handle(Room room) {
        ImageAsset dropped = AssetDragDrop.acceptDropOnLastItemWithoutOutline();
        if (dropped == null) return;

        Vector2 world = viewport.getMouseWorld();
        spritePlacer.place(room, dropped, world.x, world.y);
    }
}
