package lex.folio.ui.assets;

import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiDragDropFlags;
import imgui.flag.ImGuiMouseCursor;
import lex.folio.model.ImageAsset;

public final class AssetDragDrop {
    private static final String PAYLOAD_TYPE = "ASSET";

    private AssetDragDrop() {
    }

    static boolean beginLastItemAsSource(ImageAsset asset) {
        pushInvisiblePreviewWindow();
        if (!ImGui.beginDragDropSource()) {
            popInvisiblePreviewWindow();
            return false;
        }
        ImGui.setDragDropPayload(PAYLOAD_TYPE, asset);
        ImGui.setMouseCursor(ImGuiMouseCursor.Hand);
        return true;
    }

    static void endSource() {
        ImGui.endDragDropSource();
        popInvisiblePreviewWindow();
    }

    private static void pushInvisiblePreviewWindow() {
        ImGui.pushStyleColor(ImGuiCol.PopupBg, 0f, 0f, 0f, 0f);
        ImGui.pushStyleColor(ImGuiCol.Border, 0f, 0f, 0f, 0f);
    }

    private static void popInvisiblePreviewWindow() {
        ImGui.popStyleColor(2);
    }

    static ImageAsset acceptDropOnLastItem() {
        return acceptDrop(ImGuiDragDropFlags.None);
    }

    public static ImageAsset acceptDropOnLastItemWithoutOutline() {
        return acceptDrop(ImGuiDragDropFlags.AcceptNoDrawDefaultRect);
    }

    private static ImageAsset acceptDrop(int flags) {
        ImageAsset dropped = null;
        if (ImGui.beginDragDropTarget()) {
            dropped = ImGui.acceptDragDropPayload(PAYLOAD_TYPE, flags, ImageAsset.class);
            ImGui.endDragDropTarget();
        }
        return dropped;
    }
}
