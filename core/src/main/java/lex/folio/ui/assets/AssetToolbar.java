package lex.folio.ui.assets;

import imgui.ImGui;

/** The row of controls above the folder path: import and search. */
class AssetToolbar {
    private final AssetImportControls imports;
    private final AssetSearch search;

    AssetToolbar(AssetImportControls imports, AssetSearch search) {
        this.imports = imports;
        this.search = search;
    }

    void draw() {
        imports.drawImportButton();
        ImGui.sameLine();
        search.drawField();
    }
}
