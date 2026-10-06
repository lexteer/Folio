package lex.folio.ui.assets;

import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiMouseButton;
import imgui.type.ImString;
import lex.folio.assets.AssetFolders;
import lex.folio.model.ImageAsset;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/** Renames an asset in its tile: a text field over the name, the way rooms are renamed in their tab. */
class AssetRenamer {
    private static final int MAX_LENGTH = 128;

    private final AssetFolders folders;
    private final Consumer<String> errorSink;
    private final BiConsumer<String, String> onRenamed;
    private final ImString text = new ImString(MAX_LENGTH);

    private ImageAsset renaming;
    private boolean active;
    private float x;
    private float y;
    private float width;

    /** @param onRenamed called with the old and the new id after an asset was renamed */
    AssetRenamer(AssetFolders folders, Consumer<String> errorSink, BiConsumer<String, String> onRenamed) {
        this.folders = folders;
        this.errorSink = errorSink;
        this.onRenamed = onRenamed;
    }

    boolean isRenaming(ImageAsset asset) {
        return renaming == asset;
    }

    void start(ImageAsset asset) {
        renaming = asset;
        text.set(asset.getId());
        active = false;
    }

    /**
     * Call right after the tile of the asset: it is the last item. Double clicking it starts renaming, and so
     * does the Rename entry of its right click menu.
     */
    void trackTile(ImageAsset asset, float nameY) {
        if (ImGui.isItemHovered() && ImGui.isMouseDoubleClicked(ImGuiMouseButton.Left)) {
            start(asset);
        }
        if (ImGui.beginPopupContextItem("##AssetTileMenu")) {
            if (ImGui.menuItem("Rename")) start(asset);
            ImGui.endPopup();
        }
        if (isRenaming(asset)) {
            x = ImGui.getItemRectMinX();
            y = nameY;
            width = ImGui.getItemRectMaxX() - x;
        }
    }

    /** Draws the text field. It starts with everything selected and applies when it loses focus. */
    void draw() {
        if (renaming == null) return;

        ImGui.setCursorScreenPos(x, y);
        ImGui.setNextItemWidth(width);
        if (!active) ImGui.setKeyboardFocusHere();
        ImGui.pushStyleColor(ImGuiCol.TextSelectedBg, 1f, 1f, 1f, 0.35f);
        ImGui.inputText("##RenameAsset", text, ImGuiInputTextFlags.AutoSelectAll | ImGuiInputTextFlags.EnterReturnsTrue);
        ImGui.popStyleColor();
        if (ImGui.isItemActive()) {
            active = true;
        } else if (active) {
            ImageAsset asset = renaming;
            renaming = null;
            rename(asset, text.get().trim());
        }
    }

    private void rename(ImageAsset asset, String name) {
        String oldId = asset.getId();
        if (name.equals(oldId)) return;

        String problem = folders.renameAsset(asset, name);
        if (problem != null) {
            errorSink.accept(problem);
            return;
        }
        onRenamed.accept(oldId, asset.getId());
    }
}
