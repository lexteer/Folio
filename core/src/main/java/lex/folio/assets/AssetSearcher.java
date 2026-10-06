package lex.folio.assets;

import lex.folio.model.ImageAsset;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Finds assets by id: names that start with the text come first, then names that merely contain it. */
public final class AssetSearcher {
    private AssetSearcher() {
    }

    public static List<ImageAsset> search(Collection<ImageAsset> assets, String text) {
        String needle = text.trim().toLowerCase(Locale.ROOT);

        return assets.stream()
            .filter(asset -> toLowerCase(asset).contains(needle))
            .sorted(Comparator.comparingInt((ImageAsset asset) -> getMatchRank(asset, needle))
                .thenComparing(ImageAsset::getId, String.CASE_INSENSITIVE_ORDER))
            .toList();
    }

    private static int getMatchRank(ImageAsset asset, String needle) {
        return toLowerCase(asset).startsWith(needle) ? 0 : 1;
    }

    private static String toLowerCase(ImageAsset asset) {
        return asset.getId().toLowerCase(Locale.ROOT);
    }
}
