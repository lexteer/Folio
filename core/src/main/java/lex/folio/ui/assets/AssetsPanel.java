package lex.folio.ui.assets;

import imgui.ImGui;
import lex.folio.assets.AssetFolders;
import lex.folio.assets.AssetImporter;
import lex.folio.assets.AssetLibrary;
import lex.folio.command.CommandStack;
import lex.folio.model.Project;
import lex.folio.scene.tool.ToolState;
import lex.folio.ui.common.NativeFileDialog;

import java.nio.file.Path;
import java.util.List;

/** The window for browsing, searching, importing and organizing the project's assets. */
public class AssetsPanel {
    public static final String TITLE = "Assets";

    private final AssetToolbar toolbar;
    private final AssetPathBar pathBar;
    private final AssetGrid grid;
    private final AssetImportControls imports;

    /** Builds the panel with its parts, which are all internal to this package. */
    public static AssetsPanel create(Project project, AssetLibrary assetLibrary, CommandStack commandStack,
                                     ToolState toolState) {
        AssetFolders folders = new AssetFolders(project);
        AssetSearch search = new AssetSearch(project);
        AssetSelection selection = new AssetSelection();
        AssetBrowser browser = new AssetBrowser(project, folders, search, selection);

        AssetImporter importer = new AssetImporter(project, assetLibrary);
        AssetImportControls imports = new AssetImportControls(importer, new NativeFileDialog(), browser);
        FolderDropTarget folderDrops = new FolderDropTarget(new AssetMover(folders, commandStack), imports, selection);

        return new AssetsPanel(
            new AssetToolbar(imports, search),
            new AssetPathBar(browser, search, folderDrops),
            new AssetGrid(browser, assetLibrary, new TileInteraction(selection, toolState), folderDrops),
            imports);
    }

    private AssetsPanel(AssetToolbar toolbar, AssetPathBar pathBar, AssetGrid grid, AssetImportControls imports) {
        this.toolbar = toolbar;
        this.pathBar = pathBar;
        this.grid = grid;
        this.imports = imports;
    }

    public void draw() {
        if (ImGui.begin(TITLE)) {
            toolbar.draw();
            pathBar.draw();
            ImGui.separator();
            grid.draw();
            imports.importDropsIntoCurrentFolderIfWindowHovered();
        }
        imports.endFrame();
        ImGui.end();
    }

    public void filesDropped(List<Path> files) {
        imports.filesDropped(files);
    }
}
