package lex.folio.ui.assets;

import lex.folio.model.AssetFolderPath;
import lex.folio.assets.AssetFolders;
import lex.folio.model.ImageAsset;
import lex.folio.model.Project;

import java.util.ArrayList;
import java.util.List;

/** Which folder the assets panel is showing, and which items that puts on screen. */
class AssetBrowser {
    private final Project project;
    private final AssetFolders folders;
    private final AssetSearch search;
    private final AssetSelection selection;

    private String currentFolder = AssetFolderPath.ROOT;
    private List<String> subfolders = List.of();

    AssetBrowser(Project project, AssetFolders folders, AssetSearch search, AssetSelection selection) {
        this.project = project;
        this.folders = folders;
        this.search = search;
        this.selection = selection;
        refresh();
    }

    String getCurrentFolder() {
        return currentFolder;
    }

    void open(String folder) {
        currentFolder = folder;
        selection.clear();
        refresh();
    }

    /** Re-reads the subfolders, for when folders were created or removed on disk. */
    void refresh() {
        subfolders = folders.listSubfolders(currentFolder);
    }

    /** An id that differs for each thing shown, so that each gets its own scroll position. */
    String getViewId() {
        return search.isActive() ? "##searchResults" : "##folder:" + currentFolder;
    }

    List<BrowserItem> getItems() {
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
}
