package lex.folio.ui.assets;

import imgui.ImGui;
import imgui.type.ImString;
import lex.folio.assets.AssetSearcher;
import lex.folio.model.ImageAsset;
import lex.folio.model.Project;

import java.util.List;

/** The search box above the assets, and the results for what is typed in it. */
final class AssetSearch {
    private static final float WIDTH_IN_FONT_SIZES = 14f;
    private static final int MAX_QUERY_LENGTH = 128;

    private final Project project;
    private final ImString query = new ImString(MAX_QUERY_LENGTH);

    AssetSearch(Project project) {
        this.project = project;
    }

    void drawField() {
        ImGui.setNextItemWidth(ImGui.getFontSize() * WIDTH_IN_FONT_SIZES);
        ImGui.inputTextWithHint("##assetSearch", "Search assets", query);
    }

    boolean isActive() {
        return !query.get().trim().isEmpty();
    }

    void clear() {
        query.set("");
    }

    List<ImageAsset> findMatches() {
        return AssetSearcher.search(project.getAssets(), query.get());
    }
}
