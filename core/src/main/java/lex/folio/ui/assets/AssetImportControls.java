package lex.folio.ui.assets;

import imgui.ImGui;
import imgui.flag.ImGuiHoveredFlags;
import lex.folio.assets.AssetImporter;
import lex.folio.ui.common.NativeFileDialog;

import java.nio.file.Path;
import java.util.List;

/** Imports files into the asset folders, from the file dialog or from files dropped onto the window. */
class AssetImportControls {
    private final AssetImporter importer;
    private final NativeFileDialog fileDialog;
    private final AssetBrowser browser;

    private List<Path> droppedFiles = List.of();

    AssetImportControls(AssetImporter importer, NativeFileDialog fileDialog, AssetBrowser browser) {
        this.importer = importer;
        this.fileDialog = fileDialog;
        this.browser = browser;
    }

    void drawImportButton() {
        ImGui.beginDisabled(fileDialog.isOpen());
        if (ImGui.button("Import")) {
            String targetFolder = browser.getCurrentFolder();
            fileDialog.choosePngFiles(files -> importFiles(files, targetFolder));
        }
        ImGui.endDisabled();
    }

    /** Files dropped onto the application window; they are imported where the mouse is, or else dropped. */
    void filesDropped(List<Path> files) {
        droppedFiles = files;
    }

    /** Imports dropped files into the current folder, if the mouse is over the window being drawn. */
    void importDropsIntoCurrentFolderIfWindowHovered() {
        if (droppedFiles.isEmpty() || !ImGui.isWindowHovered(ImGuiHoveredFlags.ChildWindows)) return;
        importFiles(droppedFiles, browser.getCurrentFolder());
    }

    /** Imports dropped files into the folder, if the mouse is over the last item. Returns whether it did. */
    boolean importDropsIfLastItemHovered(String folder) {
        if (droppedFiles.isEmpty() || !ImGui.isItemHovered()) return false;

        importFiles(droppedFiles, folder);
        droppedFiles = List.of();
        return true;
    }

    /** Drops that nothing took are forgotten, so they are not imported by a later frame. */
    void endFrame() {
        droppedFiles = List.of();
    }

    private void importFiles(List<Path> files, String folder) {
        importer.importFiles(files, folder);
        browser.refresh();
    }
}
