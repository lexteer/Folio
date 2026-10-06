package lex.folio.ui.assets;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiHoveredFlags;
import imgui.flag.ImGuiMouseButton;
import lex.folio.assets.AssetFolders;
import lex.folio.assets.AssetImporter;
import lex.folio.assets.AssetLibrary;
import lex.folio.command.Command;
import lex.folio.command.CommandGroup;
import lex.folio.command.CommandStack;
import lex.folio.command.SetValueCommand;
import lex.folio.model.ImageAsset;
import lex.folio.model.Project;
import lex.folio.scene.Tool;
import lex.folio.scene.ToolState;
import lex.folio.ui.DropFlash;
import lex.folio.ui.NativeFileDialog;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class AssetsPanel {
    public static final String TITLE = "Assets";
    private static final float TILE_SIZE_IN_FONT_SIZES = 5f;
    private static final float TILE_GAP_IN_FONT_SIZES = 0.8f;

    private final Project project;
    private final AssetLibrary assetLibrary;
    private final AssetImporter assetImporter;
    private final AssetFolders assetFolders;
    private final NativeFileDialog fileDialog = new NativeFileDialog();
    private final CommandStack commandStack;
    private final ToolState toolState;
    private final AssetTile assetTile = new AssetTile();
    private final AssetSearch search;
    private final AssetPathBar pathBar = new AssetPathBar(this::acceptDropsOnLastItem);
    private final DropFlash dropFlash = new DropFlash();
    private final AssetSelection selection = new AssetSelection();
    private List<BrowserItem> visibleItems;
    private final BoxSelection boxSelection = new BoxSelection();

    private String currentFolder;
    private List<String> subfolders;

    private float tileSize;
    private float tileGap;
    private int columns;
    private int tileIndex;

    private List<Path> droppedFiles = List.of();

    public AssetsPanel(Project project, AssetLibrary assetLibrary, AssetImporter assetImporter,
                       CommandStack commandStack, ToolState toolState) {
        this.project = project;
        this.assetLibrary = assetLibrary;
        this.assetImporter = assetImporter;
        this.commandStack = commandStack;
        this.toolState = toolState;
        this.assetFolders = new AssetFolders(project);
        this.search = new AssetSearch(project);
        openFolder(AssetFolders.ROOT);
    }

    public void draw() {
        boolean visible = ImGui.begin(TITLE);
        if (visible) {
            drawContent();
            importDroppedFilesIntoCurrentFolder();
        }
        droppedFiles = List.of();
        ImGui.end();
    }

    private void importDroppedFilesIntoCurrentFolder() {
        if (droppedFiles.isEmpty() || !ImGui.isWindowHovered(ImGuiHoveredFlags.ChildWindows)) return;
        importFiles(droppedFiles, currentFolder);
    }

    private void drawContent() {
        drawToolbar();
        drawPathBar();
        ImGui.separator();

        if (ImGui.beginChild(getGridId())) {
            drawGrid();
        }
        ImGui.endChild();
    }

    private void drawToolbar() {
        ImGui.beginDisabled(fileDialog.isOpen());
        if (ImGui.button("Import")) {
            String targetFolder = currentFolder;
            fileDialog.choosePngFiles(files -> importFiles(files, targetFolder));
        }
        ImGui.endDisabled();

        ImGui.sameLine();
        search.drawField();
    }

    private void drawPathBar() {
        String clickedFolder = search.isActive() ? pathBar.drawForSearch() : pathBar.draw(currentFolder);
        if (clickedFolder != null) {
            search.clear();
            openFolder(clickedFolder);
        }
    }

    private void openFolder(String folder) {
        currentFolder = folder;
        selection.clear();
        refreshSubfolders();
    }

    private String getGridId() {
        return search.isActive() ? "##searchResults" : "##folder:" + currentFolder;
    }

    private void drawGrid() {
        tileSize = ImGui.getFontSize() * TILE_SIZE_IN_FONT_SIZES;
        tileGap = ImGui.getFontSize() * TILE_GAP_IN_FONT_SIZES;

        ImGui.setCursorPosY(ImGui.getCursorPosY() + AssetTile.SHADOW_SIZE);
        ImGui.indent(AssetTile.SHADOW_SIZE);
        columns = getColumnCount();
        tileIndex = 0;

        visibleItems = getVisibleItems();
        boxSelection.clearTiles();
        for (BrowserItem item : visibleItems) {
            drawTile(item);
        }
        handleBoxSelection();

        ImGui.unindent(AssetTile.SHADOW_SIZE);
        ImGui.dummy(0, 0);
    }

    private void handleBoxSelection() {
        if (toolState.getTool() == Tool.SELECT && isEmptyGridSpaceClicked()) {
            boxSelection.start(isAddingToSelection() ? selection.getItems() : List.of());
        }
        if (!boxSelection.isActive()) return;

        selection.replaceWith(boxSelection.getSelectedItems());
        boxSelection.draw();
        if (!ImGui.isMouseDown(ImGuiMouseButton.Left)) {
            boxSelection.finish();
        }
    }

    private static boolean isEmptyGridSpaceClicked() {
        return ImGui.isWindowHovered() && !ImGui.isAnyItemHovered() && ImGui.isMouseClicked(ImGuiMouseButton.Left);
    }

    private static boolean isAddingToSelection() {
        ImGuiIO io = ImGui.getIO();
        return io.getKeyCtrl() || io.getKeyShift();
    }

    private List<BrowserItem> getVisibleItems() {
        List<BrowserItem> items = new ArrayList<>();
        if (search.isActive()) {
            for (ImageAsset asset : search.findMatches()) {
                items.add(new BrowserItem.Asset(asset));
            }
            return items;
        }
        for (String folder : subfolders) {
            items.add(new BrowserItem.Folder(folder));
        }
        for (ImageAsset asset : project.getAssets()) {
            if (asset.getFolder().equals(currentFolder)) {
                items.add(new BrowserItem.Asset(asset));
            }
        }
        return items;
    }

    private void drawTile(BrowserItem item) {
        switch (item) {
            case BrowserItem.Folder folder -> drawFolderTile(folder);
            case BrowserItem.Asset asset -> drawAssetTile(asset);
        }
    }

    private void drawFolderTile(BrowserItem.Folder item) {
        String folder = item.path();
        placeNextTile();
        boolean clicked = assetTile.drawFolder("folder:" + folder, AssetFolders.getName(folder), tileSize, selection.contains(item));
        boxSelection.addLastItemAsTile(item);

        if (clicked && toolState.getTool() == Tool.SELECT) {
            select(item);
        }
        if (ImGui.isItemHovered() && ImGui.isMouseDoubleClicked(ImGuiMouseButton.Left)) {
            openFolder(folder);
        }
        acceptDropsOnLastItem(folder);
    }

    private boolean importDroppedFilesIfHovered(String folder) {
        if (droppedFiles.isEmpty() || !ImGui.isItemHovered()) return false;
        importFiles(droppedFiles, folder);
        droppedFiles = List.of();
        return true;
    }

    private void drawAssetTile(BrowserItem.Asset item) {
        ImageAsset asset = item.asset();
        placeNextTile();
        TextureRegion region = assetLibrary.findRegion(asset.getId());
        boolean selected = selection.contains(item);
        boolean armed = toolState.isArmed(asset);
        boolean clicked = assetTile.drawImage("asset:" + asset.getId(), asset.getId(), region, tileSize, selected, armed);
        boxSelection.addLastItemAsTile(item);

        if (clicked) {
            onAssetTileClicked(item);
        }
        if (AssetDragDrop.beginLastItemAsSource(asset)) {
            selectOnDragStart(item);
            assetTile.drawPreview(region, tileSize, getItemsToMove(item).size());
            AssetDragDrop.endSource();
        }
    }

    private void selectOnDragStart(BrowserItem item) {
        if (toolState.getTool() == Tool.SELECT && !selection.contains(item)) {
            selection.selectOnly(item);
        }
    }

    private void onAssetTileClicked(BrowserItem.Asset item) {
        switch (toolState.getTool()) {
            case PAINT -> toolState.arm(item.asset());
            case SELECT -> select(item);
        }
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

    private void acceptDropsOnLastItem(String folder) {
        ImageAsset dropped = AssetDragDrop.acceptDropOnLastItem();
        if (dropped != null) {
            moveItems(getItemsToMove(new BrowserItem.Asset(dropped)), folder);
        }
        if (importDroppedFilesIfHovered(folder)) {
            dropFlash.start(folder);
        }
        dropFlash.drawOnLastItem(folder);
    }

    private List<BrowserItem> getItemsToMove(BrowserItem grabbed) {
        return selection.contains(grabbed) ? List.copyOf(selection.getItems()) : List.of(grabbed);
    }

    private void moveItems(List<BrowserItem> items, String folder) {
        List<Command> moves = new ArrayList<>();
        for (BrowserItem item : items) {
            if (item instanceof BrowserItem.Asset(ImageAsset asset) && !asset.getFolder().equals(folder)) {
                moves.add(createMoveCommand(asset, folder));
            }
        }
        if (moves.isEmpty()) return;

        commandStack.execute(new CommandGroup(moves));
        selection.clear();
    }

    private Command createMoveCommand(ImageAsset asset, String folder) {
        return new SetValueCommand<>(target -> assetFolders.moveAsset(asset, target), asset.getFolder(), folder);
    }

    private void placeNextTile() {
        if (tileIndex % columns != 0) ImGui.sameLine(0, tileGap);
        tileIndex++;
    }

    private int getColumnCount() {
        float available = ImGui.getContentRegionAvailX() - AssetTile.SHADOW_SIZE;
        int count = (int) ((available + tileGap) / (tileSize + tileGap));
        return Math.max(1, count);
    }

    public void filesDropped(List<Path> files) {
        droppedFiles = files;
    }

    private void importFiles(List<Path> files, String folder) {
        assetImporter.importFiles(files, folder);
        refreshSubfolders();
    }

    private void refreshSubfolders() {
        subfolders = assetFolders.listSubfolders(currentFolder);
    }
}
