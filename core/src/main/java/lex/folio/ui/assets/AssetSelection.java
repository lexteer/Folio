package lex.folio.ui.assets;

import java.util.*;

public class AssetSelection {
    private final Set<BrowserItem> items = new LinkedHashSet<>();
    private final Set<BrowserItem> readOnlyItems = Collections.unmodifiableSet(items);
    private BrowserItem anchor;

    public Set<BrowserItem> getItems() {
        return readOnlyItems;
    }

    public boolean contains(BrowserItem item) {
        return items.contains(item);
    }

    public void selectOnly(BrowserItem item) {
        items.clear();
        items.add(Objects.requireNonNull(item, "item"));
        anchor = item;
    }

    public void toggle(BrowserItem item) {
        if (!items.remove(item)) {
            items.add(Objects.requireNonNull(item, "item"));
        }
        anchor = item;
    }

    public void selectRange(List<BrowserItem> orderedItems, BrowserItem item) {
        int anchorIndex = anchor == null ? -1 : orderedItems.indexOf(anchor);
        int itemIndex = orderedItems.indexOf(item);
        if (anchorIndex < 0 || itemIndex < 0) {
            selectOnly(item);
            return;
        }
        items.clear();
        items.addAll(orderedItems.subList(Math.min(anchorIndex, itemIndex), Math.max(anchorIndex, itemIndex) + 1));
    }

    public void clear() {
        items.clear();
        anchor = null;
    }

    public void replaceWith(Collection<BrowserItem> newItems) {
        items.clear();
        items.addAll(newItems);
    }
}
