package lex.folio.ui.assets;

import lex.folio.model.ImageAsset;
import lex.folio.ui.common.DropFlash;

/** Makes the last item drawn a folder that assets can be dragged onto and files dropped into. */
class FolderDropTarget {
    private final AssetMover mover;
    private final AssetImportControls imports;
    private final AssetSelection selection;
    private final DropFlash dropFlash = new DropFlash();

    FolderDropTarget(AssetMover mover, AssetImportControls imports, AssetSelection selection) {
        this.mover = mover;
        this.imports = imports;
        this.selection = selection;
    }

    void acceptOnLastItem(String folder) {
        ImageAsset dropped = AssetDragDrop.acceptDropOnLastItem();
        if (dropped != null) {
            moveDragged(dropped, folder);
        }
        if (imports.importDropsIfLastItemHovered(folder)) {
            dropFlash.start(folder);
        }
        dropFlash.drawOnLastItem(folder);
    }

    private void moveDragged(ImageAsset dragged, String folder) {
        if (mover.moveAssets(selection.getDragItems(new BrowserItem.Asset(dragged)), folder)) {
            selection.clear();
        }
    }
}
