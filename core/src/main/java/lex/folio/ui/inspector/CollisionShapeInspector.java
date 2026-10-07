package lex.folio.ui.inspector;

import com.badlogic.gdx.math.Vector2;
import imgui.ImGui;
import lex.folio.command.CommandStack;
import lex.folio.model.CircleShape;
import lex.folio.model.CollisionShape;
import lex.folio.model.CollisionTag;
import lex.folio.model.CollisionTags;
import lex.folio.model.PointShape;
import lex.folio.model.RectShape;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * Edits the selected collision shapes. The tags to choose from belong to the project and are managed in the
 * collision tags dialog.
 */
public class CollisionShapeInspector {
    private static final float LABEL_WIDTH_IN_FONT_SIZES = 6f;

    private final CommandStack commandStack;
    private final CollisionTags tags;
    private final PropertyRow row = new PropertyRow();

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
    }

    private void drawTag(List<CollisionShape> targets, CollisionShape shown) {
        ImGui.pushID("tag");
        ImGui.alignTextToFramePadding();
        ImGui.text("Tag");
        ImGui.sameLine(ImGui.getFontSize() * LABEL_WIDTH_IN_FONT_SIZES);

        ImGui.setNextItemWidth(ImGui.getContentRegionAvailX());
        if (ImGui.beginCombo("##tag", shown.getTag())) {
            for (CollisionTag tag : tags.getAll()) {
                if (drawTagChoice(tag, tag.name().equalsIgnoreCase(shown.getTag()))) {
                    setTag(targets, tag.name());
                }
            }
            ImGui.endCombo();
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
