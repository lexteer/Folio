package lex.folio.ui.assets;

import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import lex.folio.model.AssetFolderPath;

/** Shows where in the folders the panel is, and lets the user click back to any folder on the way. */
class AssetPathBar {
    private static final String ROOT_LABEL = "assets";
    private static final String SEARCH_LABEL = "Search results";
    private static final String SEPARATOR = "/";
    private static final float SEGMENT_GAP = 2f;
    private static final float SEGMENT_PADDING_X = 3f;

    private final AssetBrowser browser;
    private final AssetSearch search;
    private final FolderDropTarget folderDrops;

    AssetPathBar(AssetBrowser browser, AssetSearch search, FolderDropTarget folderDrops) {
        this.browser = browser;
        this.search = search;
        this.folderDrops = folderDrops;
    }

    void draw() {
        pushSegmentStyle();
        String clickedFolder = search.isActive() ? drawSearchPath() : drawFolderPath();
        popSegmentStyle();

        if (clickedFolder != null) {
            search.clear();
            browser.open(clickedFolder);
        }
    }

    private String drawFolderPath() {
        String clickedFolder = drawRoot();

        for (String folder : AssetFolderPath.getPathFromRoot(browser.getCurrentFolder())) {
            drawSeparator();
            if (drawSegment(AssetFolderPath.getName(folder), folder)) {
                clickedFolder = folder;
            }
        }
        return clickedFolder;
    }

    private String drawSearchPath() {
        String clickedFolder = drawRoot();
        drawSeparator();
        ImGui.text(SEARCH_LABEL);
        return clickedFolder;
    }

    private String drawRoot() {
        return drawSegment(ROOT_LABEL, AssetFolderPath.ROOT) ? AssetFolderPath.ROOT : null;
    }

    private boolean drawSegment(String label, String folder) {
        boolean clicked = ImGui.smallButton(label + "##" + folder);
        folderDrops.acceptOnLastItem(folder);
        return clicked;
    }

    private void drawSeparator() {
        ImGui.sameLine(0, SEGMENT_GAP);
        ImGui.textDisabled(SEPARATOR);
        ImGui.sameLine(0, SEGMENT_GAP);
    }

    private void pushSegmentStyle() {
        ImGui.pushStyleColor(ImGuiCol.Button, 0f, 0f, 0f, 0f);
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 1f, 1f, 1f, 0.12f);
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, 1f, 1f, 1f, 0.2f);
        ImGui.pushStyleVar(ImGuiStyleVar.FramePadding, SEGMENT_PADDING_X, 0f);
    }

    private void popSegmentStyle() {
        ImGui.popStyleVar();
        ImGui.popStyleColor(3);
    }
}
