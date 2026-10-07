package lex.folio.ui.inspector;

import com.badlogic.gdx.math.Vector2;
import imgui.ImGui;
import imgui.flag.ImGuiColorEditFlags;
import imgui.flag.ImGuiInputTextFlags;
import imgui.type.ImString;
import lex.folio.command.CommandStack;
import lex.folio.model.CircleShape;
import lex.folio.model.CollisionShape;
import lex.folio.model.CollisionTag;
import lex.folio.model.CollisionTags;
import lex.folio.model.PointShape;
import lex.folio.model.RectShape;
import lex.folio.ui.common.Icons;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * Edits the selected collision shapes. The tag list belongs to the project: a tag added here can be used by every
 * shape of every room.
 */
public class CollisionShapeInspector {
    private static final String NEW_TAG_POPUP = "New collision tag";
    private static final String REMOVE_TAG_POPUP = "Remove collision tag";
    private static final float LABEL_WIDTH_IN_FONT_SIZES = 6f;
    private static final int MISSING_TAG_RGB = 0x909090;

    private final CommandStack commandStack;
    private final CollisionTags tags;
    private final PropertyRow row = new PropertyRow();
    private final ImString newTagName = new ImString(CollisionTags.MAX_NAME_LENGTH);
    private final float[] newTagColor = new float[3];
    private boolean openNewTagPopup;
    private boolean openRemoveTagPopup;
    private boolean newTagNeedsFocus;
    /** The shapes the tag popups are about, which stay the same while a popup is open even if the selection changes. */
    private List<CollisionShape> popupTargets = List.of();

    public CollisionShapeInspector(CommandStack commandStack, CollisionTags tags) {
        this.commandStack = commandStack;
        this.tags = tags;
    }

    public void draw(List<CollisionShape> shapes) {
        List<CollisionShape> targets = List.copyOf(shapes);
        CollisionShape shown = targets.getFirst();
        drawTag(targets, shown);
        drawPosition(targets, shown);
        drawSizes(targets);
        drawPointCount(targets);
        drawNewTagPopup();
        drawRemoveTagPopup();
    }

    private void drawTag(List<CollisionShape> targets, CollisionShape shown) {
        ImGui.pushID("tag");
        ImGui.alignTextToFramePadding();
        ImGui.text("Tag");
        ImGui.sameLine(ImGui.getFontSize() * LABEL_WIDTH_IN_FONT_SIZES);

        float spacing = ImGui.getStyle().getItemSpacingX();
        float buttonWidth = ImGui.getFrameHeight();
        ImGui.setNextItemWidth(ImGui.getContentRegionAvailX() - 2f * (buttonWidth + spacing));
        if (ImGui.beginCombo("##tag", shown.getTag())) {
            for (CollisionTag tag : tags.getAll()) {
                if (drawTagChoice(tag, tag.name().equalsIgnoreCase(shown.getTag()))) {
                    setTag(targets, tag.name());
                }
            }
            ImGui.endCombo();
        }
        ImGui.sameLine();
        if (ImGui.button(Icons.ADD + "##addTag", buttonWidth, buttonWidth)) {
            popupTargets = targets;
            newTagName.set("");
            int rgb = tags.suggestRgb();
            newTagColor[0] = (rgb >> 16 & 0xFF) / 255f;
            newTagColor[1] = (rgb >> 8 & 0xFF) / 255f;
            newTagColor[2] = (rgb & 0xFF) / 255f;
            newTagNeedsFocus = true;
            openNewTagPopup = true;
        }
        if (ImGui.isItemHovered()) ImGui.setTooltip("Add a tag");
        ImGui.sameLine();
        ImGui.beginDisabled(!tags.canRemove(shown.getTag()));
        if (ImGui.button(Icons.REMOVE + "##removeTag", buttonWidth, buttonWidth)) {
            popupTargets = targets;
            openRemoveTagPopup = true;
        }
        ImGui.endDisabled();
        if (ImGui.isItemHovered(imgui.flag.ImGuiHoveredFlags.AllowWhenDisabled)) {
            ImGui.setTooltip(tags.canRemove(shown.getTag()) ? "Remove this tag" : "This tag cannot be removed");
        }
        ImGui.popID();
    }

    /** One entry of the tag list, with a swatch of the tag's color. Returns whether it was chosen. */
    private static boolean drawTagChoice(CollisionTag tag, boolean selected) {
        float size = ImGui.getFontSize();
        float x = ImGui.getCursorScreenPosX();
        float y = ImGui.getCursorScreenPosY() + ImGui.getStyle().getFramePaddingY() / 2f;
        ImGui.getWindowDrawList().addRectFilled(x, y, x + size, y + size, packRgb(tag.rgb()), 2f);
        ImGui.setCursorScreenPos(x + size + ImGui.getStyle().getItemSpacingX(), ImGui.getCursorScreenPosY());
        return ImGui.selectable(tag.name(), selected);
    }

    private static int packRgb(int rgb) {
        return 0xFF000000 | (rgb & 0xFF) << 16 | (rgb >> 8 & 0xFF) << 8 | (rgb >> 16 & 0xFF);
    }

    private void setTag(List<CollisionShape> targets, String name) {
        commit(targets, CollisionShape::getTag, CollisionShape::setTag, old -> name);
    }

    private void drawNewTagPopup() {
        if (openNewTagPopup) {
            ImGui.openPopup(NEW_TAG_POPUP);
            openNewTagPopup = false;
        }
        if (!ImGui.beginPopup(NEW_TAG_POPUP)) return;

        if (newTagNeedsFocus) {
            ImGui.setKeyboardFocusHere();
            newTagNeedsFocus = false;
        }
        boolean enter = ImGui.inputTextWithHint("##newTagName", "Tag name", newTagName,
            ImGuiInputTextFlags.EnterReturnsTrue);
        ImGui.colorEdit3("Color", newTagColor, ImGuiColorEditFlags.NoInputs);

        String name = newTagName.get();
        String problem = name.isEmpty() ? null : tags.nameProblem(name);
        if (problem != null) ImGui.textColored(1f, 0.6f, 0.4f, 1f, problem);

        boolean valid = !name.isEmpty() && problem == null;
        ImGui.beginDisabled(!valid);
        boolean add = ImGui.button("Add") || (enter && valid);
        ImGui.endDisabled();
        ImGui.sameLine();
        boolean cancel = ImGui.button("Cancel");
        if (add) {
            // The new tag goes to the selected shapes, since that is what it was made for.
            tags.add(new CollisionTag(name, CollisionTag.toRgb(newTagColor[0], newTagColor[1], newTagColor[2])));
            setTag(popupTargets, name);
        }
        if (add || cancel) ImGui.closeCurrentPopup();
        ImGui.endPopup();
    }

    private void drawRemoveTagPopup() {
        if (openRemoveTagPopup) {
            ImGui.openPopup(REMOVE_TAG_POPUP);
            openRemoveTagPopup = false;
        }
        if (!ImGui.beginPopupModal(REMOVE_TAG_POPUP, imgui.flag.ImGuiWindowFlags.AlwaysAutoResize)) return;

        String name = popupTargets.isEmpty() ? "" : popupTargets.getFirst().getTag();
        ImGui.text("Remove the tag \"" + name + "\" from the project?");
        ImGui.text("Shapes that use it keep the name, but are shown in grey.");
        ImGui.spacing();
        if (ImGui.button("Remove")) {
            tags.remove(name);
            ImGui.closeCurrentPopup();
        }
        ImGui.sameLine();
        if (ImGui.button("Cancel")) ImGui.closeCurrentPopup();
        ImGui.endPopup();
    }

    private void drawPosition(List<CollisionShape> targets, CollisionShape shown) {
        row.begin("Position");
        row.drawAxisField("X", shown.getX(), x ->
            commit(targets, CollisionShapeInspector::getPosition, CollisionShapeInspector::setPosition,
                v -> new Vector2(x, v.y)));
        row.sameLine();
        row.drawAxisField("Y", shown.getY(), y ->
            commit(targets, CollisionShapeInspector::getPosition, CollisionShapeInspector::setPosition,
                v -> new Vector2(v.x, y)));
        row.end();
    }

    private void drawSizes(List<CollisionShape> targets) {
        List<RectShape> rects = targets.stream().filter(RectShape.class::isInstance).map(RectShape.class::cast).toList();
        if (!rects.isEmpty()) {
            RectShape shown = rects.getFirst();
            row.begin("Size");
            row.drawAxisField("W", shown.getWidth(), width -> {
                if (width > 0) ObjectEdits.commit(commandStack, rects, CollisionShapeInspector::getSize,
                    CollisionShapeInspector::setSize, v -> new Vector2(width, v.y));
            });
            row.sameLine();
            row.drawAxisField("H", shown.getHeight(), height -> {
                if (height > 0) ObjectEdits.commit(commandStack, rects, CollisionShapeInspector::getSize,
                    CollisionShapeInspector::setSize, v -> new Vector2(v.x, height));
            });
            row.end();
        }

        List<CircleShape> circles = targets.stream().filter(CircleShape.class::isInstance)
            .map(CircleShape.class::cast).toList();
        if (!circles.isEmpty()) {
            row.begin("Radius");
            row.drawWideField("radius", circles.getFirst().getRadius(), radius -> {
                if (radius > 0) ObjectEdits.commit(commandStack, circles, CircleShape::getRadius,
                    CircleShape::setRadius, old -> radius);
            });
            row.end();
        }
    }

    private void drawPointCount(List<CollisionShape> targets) {
        if (targets.size() != 1 || !(targets.getFirst() instanceof PointShape points)) return;

        ImGui.alignTextToFramePadding();
        ImGui.text("Points");
        ImGui.sameLine(ImGui.getFontSize() * LABEL_WIDTH_IN_FONT_SIZES);
        ImGui.text(String.valueOf(points.getPointCount()));
    }

    private static Vector2 getPosition(CollisionShape shape) {
        return new Vector2(shape.getX(), shape.getY());
    }

    private static void setPosition(CollisionShape shape, Vector2 position) {
        shape.setPosition(position.x, position.y);
    }

    private static Vector2 getSize(RectShape rect) {
        return new Vector2(rect.getWidth(), rect.getHeight());
    }

    private static void setSize(RectShape rect, Vector2 size) {
        rect.setSize(size.x, size.y);
    }

    private <O extends CollisionShape, T> void commit(List<O> shapes, Function<O, T> read, BiConsumer<O, T> write,
                                                      UnaryOperator<T> change) {
        ObjectEdits.commit(commandStack, shapes, read, write, change);
    }
}
