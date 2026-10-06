package lex.folio.ui.assets;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiMouseButton;
import lex.folio.model.ImageAsset;
import lex.folio.scene.tool.Tool;
import lex.folio.scene.tool.ToolState;

import java.util.List;

/** What clicking, dragging and box selecting tiles does, depending on the active tool and held keys. */
class TileInteraction {
    private final AssetSelection selection;
    private final ToolState toolState;
    private final BoxSelection boxSelection = new BoxSelection();

    private List<BrowserItem> visibleItems = List.of();

    TileInteraction(AssetSelection selection, ToolState toolState) {
        this.selection = selection;
        this.toolState = toolState;
    }

    void beginFrame(List<BrowserItem> visibleItems) {
        this.visibleItems = visibleItems;
        boxSelection.clearTiles();
    }

    /** Registers the last item drawn as the tile of the item, for box selection. */
    void trackLastItemAsTile(BrowserItem item) {
        boxSelection.addLastItemAsTile(item);
    }

    /** Call after all tiles of the frame are tracked. */
    void endFrame() {
        if (toolState.getTool() == Tool.SELECT && isEmptySpaceClicked()) {
            boxSelection.start(isAddingToSelection() ? selection.getItems() : List.of());
        }
        if (!boxSelection.isActive()) return;

        selection.replaceWith(boxSelection.getSelectedItems());
        boxSelection.draw();
        if (!ImGui.isMouseDown(ImGuiMouseButton.Left)) {
            boxSelection.finish();
        }
    }

    boolean isSelected(BrowserItem item) {
        return selection.contains(item);
    }

    boolean isArmed(ImageAsset asset) {
        return toolState.isArmed(asset);
    }

    void onFolderClicked(BrowserItem.Folder folder) {
        if (toolState.getTool() == Tool.SELECT) {
            select(folder);
        }
    }

    void onAssetClicked(BrowserItem.Asset asset) {
        switch (toolState.getTool()) {
            case PAINT -> toolState.arm(asset.asset());
            case SELECT -> select(asset);
        }
    }

    void onDragStarted(BrowserItem item) {
        if (toolState.getTool() == Tool.SELECT && !selection.contains(item)) {
            selection.selectOnly(item);
        }
    }

    /** How many items travel along when the item is dragged. */
    int getDragItemCount(BrowserItem item) {
        return selection.getDragItems(item).size();
    }

    private void select(BrowserItem item) {
        ImGuiIO io = ImGui.getIO();
        if (io.getKeyShift()) {
            selection.selectRange(visibleItems, item);
        } else if (io.getKeyCtrl()) {
            selection.toggle(item);
        } else {
            selection.selectOnly(item);
        }
    }

    private static boolean isEmptySpaceClicked() {
        return ImGui.isWindowHovered() && !ImGui.isAnyItemHovered() && ImGui.isMouseClicked(ImGuiMouseButton.Left);
    }

    private static boolean isAddingToSelection() {
        ImGuiIO io = ImGui.getIO();
        return io.getKeyCtrl() || io.getKeyShift();
    }
}
