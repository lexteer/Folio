package lex.folio.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

/** The tags the user can give collision shapes. They belong to the project, so every room can use them. */
public class CollisionTags {
    /** Always there, and the tag new shapes get. It cannot be removed. */
    public static final String DEFAULT_NAME = "solid";
    public static final int DEFAULT_RGB = 0xE0605A;
    /** Colors given to new tags, one after the other. */
    private static final int[] PALETTE = {0x4FA3E8, 0x5CC27A, 0xE8B84F, 0xB070E0, 0xE87FB0, 0x4FD0C8};
    public static final int MAX_NAME_LENGTH = 32;
    /** Names are stored in the project file with these in between, so they cannot be part of a name. */
    private static final String FORBIDDEN_IN_NAMES = "|=";

    private final List<CollisionTag> tags = new ArrayList<>();
    private final List<CollisionTag> readOnlyTags = Collections.unmodifiableList(tags);
    private Runnable changeListener = () -> {
    };
    private BiConsumer<String, String> replacedListener = (oldName, newName) -> {
    };

    public CollisionTags() {
        tags.add(new CollisionTag(DEFAULT_NAME, DEFAULT_RGB));
    }

    /** Called after a tag was added or removed. */
    public void setChangeListener(Runnable changeListener) {
        this.changeListener = Objects.requireNonNull(changeListener, "changeListener");
    }

    /** Called with the old and the new name of the tag that shapes have to be given instead, after a rename or removal. */
    public void setReplacedListener(BiConsumer<String, String> replacedListener) {
        this.replacedListener = Objects.requireNonNull(replacedListener, "replacedListener");
    }

    public List<CollisionTag> getAll() {
        return readOnlyTags;
    }

    /** Returns null if there is no tag with that name. Names are not case sensitive. */
    public CollisionTag find(String name) {
        for (CollisionTag tag : tags) {
            if (tag.name().equalsIgnoreCase(name)) return tag;
        }
        return null;
    }

    public CollisionTag getDefault() {
        return tags.getFirst();
    }

    /** Why the name cannot be used for a new tag, or null if it can. */
    public String nameProblem(String name) {
        return nameProblem(name, null);
    }

    /** Like {@link #nameProblem(String)}, but the tag called {@code ignoring} does not count as using its name. */
    public String nameProblem(String name, String ignoring) {
        if (name.isBlank()) return "A tag needs a name.";
        if (!name.equals(name.strip())) return "A tag name cannot start or end with a space.";
        if (name.length() > MAX_NAME_LENGTH) return "A tag name can have at most " + MAX_NAME_LENGTH + " characters.";
        for (char forbidden : FORBIDDEN_IN_NAMES.toCharArray()) {
            if (name.indexOf(forbidden) >= 0) return "A tag name cannot contain " + forbidden;
        }
        CollisionTag used = find(name);
        if (used != null && !used.name().equalsIgnoreCase(ignoring)) return "There is already a tag called \"" + name + "\".";
        return null;
    }

    /** The color the next new tag starts out with. */
    public int suggestRgb() {
        return PALETTE[(tags.size() - 1) % PALETTE.length];
    }

    public void add(CollisionTag tag) {
        String problem = nameProblem(tag.name());
        if (problem != null) throw new IllegalArgumentException(problem);

        tags.add(tag);
        changeListener.run();
    }

    public boolean canRename(String name) {
        return canRemove(name);
    }

    /** Gives the tag a new name. The shapes that use it are pointed at the new name. */
    public void rename(String oldName, String newName) {
        CollisionTag tag = find(oldName);
        if (tag == null || !canRename(oldName) || nameProblem(newName, oldName) != null) return;
        if (tag.name().equals(newName)) return;

        tags.set(tags.indexOf(tag), new CollisionTag(newName, tag.rgb()));
        changeListener.run();
        replacedListener.accept(tag.name(), newName);
    }

    public void setColor(String name, int rgb) {
        CollisionTag tag = find(name);
        if (tag == null || tag.rgb() == rgb) return;

        tags.set(tags.indexOf(tag), new CollisionTag(tag.name(), rgb));
        changeListener.run();
    }

    public boolean canRemove(String name) {
        return !name.equalsIgnoreCase(DEFAULT_NAME) && find(name) != null;
    }

    public void remove(String name) {
        if (!canRemove(name)) return;

        CollisionTag tag = find(name);
        tags.remove(tag);
        changeListener.run();
        replacedListener.accept(tag.name(), getDefault().name());
    }

    /** Replaces the tags with the loaded ones, keeping the default one. Does not call the change listener. */
    public void load(List<CollisionTag> loaded) {
        CollisionTag defaultTag = tags.getFirst();
        for (CollisionTag tag : loaded) {
            if (tag.name().equalsIgnoreCase(DEFAULT_NAME)) {
                defaultTag = tag;
            }
        }
        tags.clear();
        tags.add(defaultTag);
        for (CollisionTag tag : loaded) {
            if (nameProblem(tag.name()) == null) tags.add(tag);
        }
    }
}
