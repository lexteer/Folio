package lex.folio.ui.assets;

import imgui.ImGui;
import imgui.type.ImString;
import lex.folio.model.ImageAsset;
import lex.folio.model.Project;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

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
        return !getNeedle().isEmpty();
    }

    void clear() {
        query.set("");
    }

    List<ImageAsset> findMatches() {
        String needle = getNeedle();

        return project.getAssets().stream()
            .filter(asset -> toLowerCase(asset).contains(needle))
            .sorted(Comparator.comparingInt((ImageAsset asset) -> getMatchRank(asset, needle))
                .thenComparing(ImageAsset::getId, String.CASE_INSENSITIVE_ORDER))
            .toList();
    }

    private String getNeedle() {
        return query.get().trim().toLowerCase(Locale.ROOT);
    }

    private static int getMatchRank(ImageAsset asset, String needle) {
        return toLowerCase(asset).startsWith(needle) ? 0 : 1;
    }

    private static String toLowerCase(ImageAsset asset) {
        return asset.getId().toLowerCase(Locale.ROOT);
    }
}
