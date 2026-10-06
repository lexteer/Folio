package lex.folio.ui.assets;

import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import lex.folio.assets.AssetFolders;
import java.util.function.Consumer;

final class AssetPathBar {
    private static final String ROOT_LABEL = "assets";
    private static final String SEARCH_LABEL = "Search results";
    private static final String SEPARATOR = "/";
    private static final float SEGMENT_GAP = 2f;
    private static final float SEGMENT_PADDING_X = 3f;

    private final Consumer<String> acceptDropsOnSegment;

    AssetPathBar(Consumer<String> acceptDropsOnSegment) {
        this.acceptDropsOnSegment = acceptDropsOnSegment;
    }

    String draw(String currentFolder) {
        pushSegmentStyle();
        String clickedFolder = null;

        if (drawSegment(ROOT_LABEL, AssetFolders.ROOT)) {
            clickedFolder = AssetFolders.ROOT;
        }
        for (String folder : AssetFolders.getPathFromRoot(currentFolder)) {
            drawSeparator();
            if (drawSegment(AssetFolders.getName(folder), folder)) {
                clickedFolder = folder;
            }
        }

        popSegmentStyle();
        return clickedFolder;
    }

    private boolean drawSegment(String label, String folder) {
        boolean clicked = ImGui.smallButton(label + "##" + folder);
        acceptDropsOnSegment.accept(folder);
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

    String drawForSearch() {
        pushSegmentStyle();
        String clickedFolder = drawSegment(ROOT_LABEL, AssetFolders.ROOT) ? AssetFolders.ROOT : null;
        drawSeparator();
        ImGui.text(SEARCH_LABEL);
        popSegmentStyle();
        return clickedFolder;
    }
}
