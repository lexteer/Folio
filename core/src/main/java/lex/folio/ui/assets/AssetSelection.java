package lex.folio.ui.assets;

import java.util.*;

class AssetSelection {
    private final Set<BrowserItem> items = new LinkedHashSet<>();
    private final Set<BrowserItem> readOnlyItems = Collections.unmodifiableSet(items);
    private BrowserItem anchor;

    Set<BrowserItem> getItems() {
        return readOnlyItems;
    }

    boolean contains(BrowserItem item) {
        return items.contains(item);
    }

    void selectOnly(BrowserItem item) {
        items.clear();
        items.add(Objects.requireNonNull(item, "item"));
        anchor = item;
    }

    void toggle(BrowserItem item) {
        if (!items.remove(item)) {
            items.add(Objects.requireNonNull(item, "item"));
        }
        anchor = item;
    }

    void selectRange(List<BrowserItem> orderedItems, BrowserItem item) {
        int anchorIndex = anchor == null ? -1 : orderedItems.indexOf(anchor);
        int itemIndex = orderedItems.indexOf(item);
        if (anchorIndex < 0 || itemIndex < 0) {
            selectOnly(item);
            return;
        }
        items.clear();
        items.addAll(orderedItems.subList(Math.min(anchorIndex, itemIndex), Math.max(anchorIndex, itemIndex) + 1));
    }

    /** The items that move together when the grabbed item is dragged: the whole selection if it is part of it. */
    List<BrowserItem> getDragItems(BrowserItem grabbed) {
        return items.contains(grabbed) ? List.copyOf(items) : List.of(grabbed);
    }

    void clear() {
        items.clear();
        anchor = null;
    }

    void replaceWith(Collection<BrowserItem> newItems) {
        items.clear();
        items.addAll(newItems);
    }
}
