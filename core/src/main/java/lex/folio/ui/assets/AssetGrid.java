package lex.folio.ui.assets;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import imgui.ImGui;
import imgui.flag.ImGuiMouseButton;
import lex.folio.model.AssetFolderPath;
import lex.folio.assets.AssetLibrary;
import lex.folio.model.ImageAsset;

import java.util.List;

/** The scrolling grid of tiles for the folders and assets in view. */
class AssetGrid {
    private final AssetBrowser browser;
    private final AssetLibrary assetLibrary;
    private final TileInteraction interaction;
    private final FolderDropTarget folderDrops;
    private final TileLayout layout = new TileLayout();
    private final AssetTile tile = new AssetTile();

    AssetGrid(AssetBrowser browser, AssetLibrary assetLibrary, TileInteraction interaction,
              FolderDropTarget folderDrops) {
        this.browser = browser;
        this.assetLibrary = assetLibrary;
        this.interaction = interaction;
        this.folderDrops = folderDrops;
    }

    void draw() {
        if (ImGui.beginChild(browser.getViewId())) {
            drawTiles();
        }
        ImGui.endChild();
    }

    private void drawTiles() {
        ImGui.setCursorPosY(ImGui.getCursorPosY() + AssetTile.SHADOW_SIZE);
        ImGui.indent(AssetTile.SHADOW_SIZE);
        layout.begin();

        List<BrowserItem> items = browser.getItems();
        interaction.beginFrame(items);
        for (BrowserItem item : items) {
            drawTile(item);
        }
        interaction.endFrame();

        ImGui.unindent(AssetTile.SHADOW_SIZE);
        ImGui.dummy(0, 0);
    }

    private void drawTile(BrowserItem item) {
        layout.placeNextTile();
        switch (item) {
            case BrowserItem.Folder folder -> drawFolderTile(folder);
            case BrowserItem.Asset asset -> drawAssetTile(asset);
        }
    }

    private void drawFolderTile(BrowserItem.Folder item) {
        String folder = item.path();
        boolean clicked = tile.drawFolder("folder:" + folder, AssetFolderPath.getName(folder),
            layout.getTileSize(), interaction.isSelected(item));
        interaction.trackLastItemAsTile(item);

        if (clicked) {
            interaction.onFolderClicked(item);
        }
        if (ImGui.isItemHovered() && ImGui.isMouseDoubleClicked(ImGuiMouseButton.Left)) {
            browser.open(folder);
        }
        folderDrops.acceptOnLastItem(folder);
    }

    private void drawAssetTile(BrowserItem.Asset item) {
        ImageAsset asset = item.asset();
        TextureRegion region = assetLibrary.findRegion(asset.getId());
        boolean clicked = tile.drawImage("asset:" + asset.getId(), asset.getId(), region, layout.getTileSize(),
            interaction.isSelected(item), interaction.isArmed(asset));
        interaction.trackLastItemAsTile(item);

        if (clicked) {
            interaction.onAssetClicked(item);
        }
        if (AssetDragDrop.beginLastItemAsSource(asset)) {
            interaction.onDragStarted(item);
            tile.drawPreview(region, layout.getTileSize(), interaction.getDragItemCount(item));
            AssetDragDrop.endSource();
        }
    }
}
