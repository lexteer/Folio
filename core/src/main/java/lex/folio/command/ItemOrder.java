package lex.folio.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** How the draw order of a layer changes when some of its items are moved forward or back. Index 0 is drawn first. */
public enum ItemOrder {
    BRING_FORWARD("Bring Forward"),
    SEND_BACKWARD("Send Backward"),
    BRING_TO_FRONT("Bring to Front"),
    SEND_TO_BACK("Send to Back");

    private final String label;

    ItemOrder(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /** The order after moving the selected items, which keep their order among themselves. */
    public <T> List<T> apply(List<T> order, Set<?> selected) {
        List<T> result = new ArrayList<>(order);
        switch (this) {
            case BRING_FORWARD -> {
                // The topmost items are handled first, so a group moves up together by one place.
                for (int i = result.size() - 2; i >= 0; i--) {
                    if (selected.contains(result.get(i)) && !selected.contains(result.get(i + 1))) {
                        result.add(i + 1, result.remove(i));
                    }
                }
            }
            case SEND_BACKWARD -> {
                for (int i = 1; i < result.size(); i++) {
                    if (selected.contains(result.get(i)) && !selected.contains(result.get(i - 1))) {
                        result.add(i - 1, result.remove(i));
                    }
                }
            }
            case BRING_TO_FRONT -> {
                List<T> moved = result.stream().filter(selected::contains).toList();
                result.removeAll(moved);
                result.addAll(moved);
            }
            case SEND_TO_BACK -> {
                List<T> moved = result.stream().filter(selected::contains).toList();
                result.removeAll(moved);
                result.addAll(0, moved);
            }
        }
        return result;
    }
}
