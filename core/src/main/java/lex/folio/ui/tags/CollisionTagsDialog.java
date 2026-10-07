package lex.folio.ui.tags;

import imgui.ImGui;
import imgui.flag.ImGuiChildFlags;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImString;
import lex.folio.model.CollisionTag;
import lex.folio.model.CollisionTags;
import lex.folio.ui.common.Icons;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The popup for adding, renaming, recoloring and removing the project's collision tags. Shapes pick their tag from
 * the inspector's list, which is these tags.
 */
public class CollisionTagsDialog {
    private static final String POPUP = "Collision Tags";
    private static final String COLOR_POPUP = "##TagColor";
    private static final float MAX_LIST_HEIGHT_IN_FONT_SIZES = 14f;
    private static final float WIDTH_IN_FONT_SIZES = 26f;
    private static final float SWATCH_ROUNDING = 3f;

    private final CollisionTags tags;
    private boolean openRequested;

    /** The name field of each tag, so that a name can be typed out while the list keeps showing the tag's real name. */
    private final Map<String, ImString> nameFields = new HashMap<>();
    private final ImString newName = new ImString(CollisionTags.MAX_NAME_LENGTH);
    private int newRgb;
    private String error;

    /** Which color the picker popup is editing: the name of a tag, or null for the new tag. */
    private String pickedTag;
    private final float[] pickedColor = new float[3];

    public CollisionTagsDialog(CollisionTags tags) {
        this.tags = tags;
    }

    public void open() {
        openRequested = true;
    }

    public void draw() {
        if (openRequested) {
            newName.set("");
            newRgb = tags.suggestRgb();
            error = null;
            nameFields.clear();
            ImGui.openPopup(POPUP);
            openRequested = false;
        }
        ImGui.setNextWindowSize(ImGui.getFontSize() * WIDTH_IN_FONT_SIZES, 0f);
        if (!ImGui.beginPopupModal(POPUP, ImGuiWindowFlags.NoResize)) return;

        ImGui.pushStyleColor(ImGuiCol.Text, ImGui.getColorU32(ImGuiCol.TextDisabled));
        ImGui.textWrapped("Shapes pick their tag from this list. Removing a tag turns its shapes into \""
            + tags.getDefault().name() + "\".");
        ImGui.popStyleColor();
        ImGui.spacing();
        drawList();
        ImGui.separator();
        drawNewTagRow();
        if (error != null) ImGui.textColored(1f, 0.6f, 0.4f, 1f, error);
        ImGui.spacing();
        if (ImGui.button("Close", ImGui.getContentRegionAvailX(), 0f)) ImGui.closeCurrentPopup();

        drawColorPicker();
        ImGui.endPopup();
    }

    private void drawList() {
        float rows = ImGui.getFrameHeightWithSpacing() * tags.getAll().size() + ImGui.getStyle().getWindowPaddingY();
        float height = Math.min(rows, ImGui.getFontSize() * MAX_LIST_HEIGHT_IN_FONT_SIZES);
        if (!ImGui.beginChild("##tagList", 0f, height, ImGuiChildFlags.None)) {
            ImGui.endChild();
            return;
        }
        String toRemove = null;
        for (CollisionTag tag : List.copyOf(tags.getAll())) {
            ImGui.pushID(tag.name());
            if (drawRow(tag)) toRemove = tag.name();
            ImGui.popID();
        }
        ImGui.endChild();
        if (toRemove != null) {
            tags.remove(toRemove);
            nameFields.clear();
            error = null;
        }
    }

    /** One tag. Returns whether its remove button was clicked. */
    private boolean drawRow(CollisionTag tag) {
        if (drawSwatch("##color", tag.rgb())) {
            pickedTag = tag.name();
            setPicked(tag.rgb());
            ImGui.openPopup(COLOR_POPUP);
        }
        ImGui.sameLine();

        boolean editable = tags.canRename(tag.name());
        ImString field = nameFields.computeIfAbsent(tag.name(), name -> {
            ImString text = new ImString(CollisionTags.MAX_NAME_LENGTH);
            text.set(name);
            return text;
        });
        float buttonWidth = ImGui.getFrameHeight();
        ImGui.setNextItemWidth(ImGui.getContentRegionAvailX() - buttonWidth - ImGui.getStyle().getItemSpacingX());
        ImGui.beginDisabled(!editable);
        ImGui.inputText("##name", field, ImGuiInputTextFlags.EnterReturnsTrue);
        ImGui.endDisabled();
        if (ImGui.isItemDeactivated()) applyRename(tag, field);

        ImGui.sameLine();
        ImGui.beginDisabled(!tags.canRemove(tag.name()));
        boolean remove = ImGui.button(Icons.REMOVE + "##remove", buttonWidth, buttonWidth);
        ImGui.endDisabled();
        if (ImGui.isItemHovered(imgui.flag.ImGuiHoveredFlags.AllowWhenDisabled)) {
            ImGui.setTooltip(tags.canRemove(tag.name()) ? "Remove this tag" : "This tag cannot be removed or renamed");
        }
        return remove;
    }

    private void applyRename(CollisionTag tag, ImString field) {
        String name = field.get();
        if (name.equals(tag.name())) {
            error = null;
            return;
        }
        String problem = tags.nameProblem(name, tag.name());
        if (problem != null) {
            error = problem;
            field.set(tag.name());
            return;
        }
        error = null;
        tags.rename(tag.name(), name);
        nameFields.clear();
    }

    private void drawNewTagRow() {
        if (drawSwatch("##newColor", newRgb)) {
            pickedTag = null;
            setPicked(newRgb);
            ImGui.openPopup(COLOR_POPUP);
        }
        ImGui.sameLine();

        float addWidth = ImGui.calcTextSizeX("Add") + 2f * ImGui.getStyle().getFramePaddingX();
        ImGui.setNextItemWidth(ImGui.getContentRegionAvailX() - addWidth - ImGui.getStyle().getItemSpacingX());
        boolean enter = ImGui.inputTextWithHint("##newName", "New tag name", newName,
            ImGuiInputTextFlags.EnterReturnsTrue);
        ImGui.sameLine();
        boolean add = ImGui.button("Add") || enter;
        if (add) addTag();
    }

    private void addTag() {
        String name = newName.get();
        String problem = tags.nameProblem(name);
        if (problem != null) {
            error = problem;
            return;
        }
        error = null;
        tags.add(new CollisionTag(name, newRgb));
        newName.set("");
        newRgb = tags.suggestRgb();
        nameFields.clear();
    }

    /** The picker, with OK to take the color and Cancel to leave it as it was. */
    private void drawColorPicker() {
        if (!ImGui.beginPopup(COLOR_POPUP)) return;

        ImGui.colorPicker3("##picker", pickedColor);
        if (ImGui.button("OK")) {
            int rgb = CollisionTag.toRgb(pickedColor[0], pickedColor[1], pickedColor[2]);
            if (pickedTag == null) {
                newRgb = rgb;
            } else {
                tags.setColor(pickedTag, rgb);
            }
            ImGui.closeCurrentPopup();
        }
        ImGui.sameLine();
        if (ImGui.button("Cancel")) ImGui.closeCurrentPopup();
        ImGui.endPopup();
    }

    private void setPicked(int rgb) {
        pickedColor[0] = (rgb >> 16 & 0xFF) / 255f;
        pickedColor[1] = (rgb >> 8 & 0xFF) / 255f;
        pickedColor[2] = (rgb & 0xFF) / 255f;
    }

    /** A button showing a color. Returns whether it was clicked. */
    private static boolean drawSwatch(String id, int rgb) {
        float size = ImGui.getFrameHeight();
        boolean clicked = ImGui.invisibleButton(id, size, size);
        float x = ImGui.getItemRectMinX();
        float y = ImGui.getItemRectMinY();
        int color = 0xFF000000 | (rgb & 0xFF) << 16 | (rgb >> 8 & 0xFF) << 8 | (rgb >> 16 & 0xFF);
        ImGui.getWindowDrawList().addRectFilled(x, y, x + size, y + size, color, SWATCH_ROUNDING);
        ImGui.getWindowDrawList().addRect(x, y, x + size, y + size,
            ImGui.getColorU32(ImGui.isItemHovered() ? ImGuiCol.Text : ImGuiCol.Border), SWATCH_ROUNDING);
        if (ImGui.isItemHovered()) ImGui.setTooltip("Change the color");
        return clicked;
    }
}
