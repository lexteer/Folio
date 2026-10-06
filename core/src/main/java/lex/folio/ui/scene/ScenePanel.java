package lex.folio.ui.scene;

import imgui.ImGuiWindowClass;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiMouseButton;
import imgui.flag.ImGuiHoveredFlags;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiTabBarFlags;
import imgui.flag.ImGuiTabItemFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.internal.ImGui;
import imgui.type.ImString;
import imgui.flag.ImGuiInputTextFlags;
import imgui.internal.ImGuiDockNode;
import imgui.internal.flag.ImGuiDockNodeFlags;
import lex.folio.model.Room;
import lex.folio.scene.Selection;
import lex.folio.scene.camera.SceneCamera;
import lex.folio.project.RoomStorage;
import lex.folio.scene.render.SceneRenderer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * The window showing the rooms: one tab per room, each with its own camera. The window docks as a whole, but nothing
 * can be tabbed into it or out of it, so the room tabs are the only tabs it ever has.
 */
public class ScenePanel {
    public static final String TITLE = "Scene";
    private static final String ROOM_NAME_PREFIX = "Room ";
    private static final String UNSAVED_POPUP = "Unsaved changes";
    private static final int RENAME_MAX_LENGTH = 128;
    private static final int WINDOW_FLAGS = ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoScrollWithMouse
        | ImGuiWindowFlags.NoCollapse;
    private static final int TAB_BAR_FLAGS = ImGuiTabBarFlags.Reorderable | ImGuiTabBarFlags.FittingPolicyScroll;
    /** Keeps the dock node from becoming a tab group, and from showing any tab or title bar of its own. */
    private static final int DOCK_NODE_FLAGS = ImGuiDockNodeFlags.NoTabBar
        | ImGuiDockNodeFlags.NoDockingOverMe | ImGuiDockNodeFlags.NoDockingOverOther;

    private final List<RoomTab> tabs = new ArrayList<>();
    private final float pixelsPerMeter;
    private final Function<String, Room> roomFactory;
    private final RoomStorage storage;
    private final Consumer<String> errorSink;
    private final SceneRenderer renderer;
    private final SceneViewport viewport;
    private final SceneOverlay overlay;
    private final SceneInput input;
    private final Selection selection;
    private final ImGuiWindowClass windowClass = new ImGuiWindowClass();
    private RoomTab activeTab;
    private RoomTab tabToSelect;
    private boolean hovered;
    private boolean movingWindow;
    private boolean recheckUnsaved;
    /** The tab whose close button was clicked while it has unsaved changes, while the user is being asked. */
    private RoomTab tabAskedToClose;
    /** What to do once the user has dealt with all unsaved rooms, while they are being asked. */
    private Runnable afterLeavePrompt;
    private RoomTab renamingTab;
    private final ImString renameText = new ImString(RENAME_MAX_LENGTH);
    private boolean renameActive;
    private float renameX;
    private float renameY;
    private float renameWidth;

    /** @param savedRooms rooms that were loaded from storage, so they start out without unsaved changes */
    public ScenePanel(List<Room> savedRooms, float pixelsPerMeter, Function<String, Room> roomFactory,
                      RoomStorage storage, Consumer<String> errorSink, SceneRenderer renderer,
                      SceneViewport viewport, SceneOverlay overlay, SceneInput input, Selection selection) {
        this.pixelsPerMeter = pixelsPerMeter;
        this.roomFactory = roomFactory;
        this.storage = storage;
        this.errorSink = errorSink;
        this.renderer = renderer;
        this.viewport = viewport;
        this.overlay = overlay;
        this.input = input;
        this.selection = selection;
        windowClass.setDockNodeFlagsOverrideSet(DOCK_NODE_FLAGS);

        for (Room room : savedRooms) {
            addTab(room).markSaved(storage.snapshot(room));
        }
    }

    private RoomTab addTab(Room room) {
        RoomTab tab = new RoomTab(room, new SceneCamera(pixelsPerMeter));
        tabs.add(tab);
        return tab;
    }

    /** Adds a room that exists only in memory until it is saved, and shows it. */
    public void openNewRoom(Room room) {
        tabToSelect = addTab(room);
    }

    /** Call after anything may have changed a room, so the unsaved marks are worked out again. */
    public void roomsChanged() {
        recheckUnsaved = true;
    }

    /** Points the sprites of the open rooms that use the asset id at the new id. */
    public void assetRenamed(String oldId, String newId) {
        for (RoomTab tab : tabs) {
            tab.getRoom().replaceAssetId(oldId, newId);
        }
        roomsChanged();
    }

    public boolean canSaveActiveRoom() {
        return activeTab != null && activeTab.isUnsaved();
    }

    /** Shows a room that was loaded from storage, or the tab of that room if it is open already. */
    public void openSavedRoom(Room room) {
        for (RoomTab tab : tabs) {
            if (tab.getRoom().getName().equalsIgnoreCase(room.getName())) {
                tabToSelect = tab;
                return;
            }
        }
        RoomTab tab = addTab(room);
        tab.markSaved(storage.snapshot(room));
        tabToSelect = tab;
    }

    public void saveActiveRoom() {
        if (canSaveActiveRoom()) save(activeTab);
    }

    public boolean hasUnsavedRooms() {
        return tabs.stream().anyMatch(RoomTab::isUnsaved);
    }

    /** Runs {@code action} now if no room has unsaved changes. Otherwise asks first, and runs it unless cancelled. */
    public void runWhenNothingIsUnsaved(Runnable action) {
        if (!hasUnsavedRooms()) {
            action.run();
        } else if (tabAskedToClose == null && afterLeavePrompt == null) {
            afterLeavePrompt = action;
        }
    }

    private boolean save(RoomTab tab) {
        try {
            tab.markSaved(storage.save(tab.getRoom(), tab.getSavedName()));
            return true;
        } catch (IOException e) {
            errorSink.accept("Could not save room \"" + tab.getRoom().getName() + "\": " + e.getMessage());
            return false;
        }
    }

    private void requestClose(RoomTab tab) {
        if (!tab.isUnsaved()) {
            closeTab(tab);
            return;
        }
        if (tabAskedToClose == null && afterLeavePrompt == null) tabAskedToClose = tab;
    }

    private void closeTab(RoomTab tab) {
        tabs.remove(tab);
        if (tab == activeTab) {
            activeTab = null;
            selection.clear();
        }
        if (tab == renamingTab) renamingTab = null;
    }

    public void draw() {
        refreshUnsavedMarks();
        ImGui.setNextWindowClass(windowClass);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 0, 0);
        boolean visible = ImGui.begin(TITLE, WINDOW_FLAGS);
        ImGui.popStyleVar();
        hovered = ImGui.isWindowHovered(ImGuiHoveredFlags.RootAndChildWindows);

        if (visible) {
            if (isDocked()) drawHeader();
            drawTabs();
        }
        ImGui.end();
        drawUnsavedPrompt();
    }

    private void refreshUnsavedMarks() {
        if (!recheckUnsaved) return;

        recheckUnsaved = false;
        for (RoomTab tab : tabs) {
            tab.refreshUnsaved(storage.snapshot(tab.getRoom()));
        }
    }

    /** Asks whether to save: the one room being closed, or all unsaved rooms when leaving. */
    private void drawUnsavedPrompt() {
        List<RoomTab> unsaved = tabAskedToClose != null ? List.of(tabAskedToClose)
            : afterLeavePrompt != null ? tabs.stream().filter(RoomTab::isUnsaved).toList() : List.of();
        if (unsaved.isEmpty()) return;

        if (!ImGui.isPopupOpen(UNSAVED_POPUP)) {
            ImGui.openPopup(UNSAVED_POPUP);
        }
        if (!ImGui.beginPopupModal(UNSAVED_POPUP, ImGuiWindowFlags.AlwaysAutoResize)) return;

        if (unsaved.size() == 1) {
            ImGui.text("Room \"" + unsaved.get(0).getRoom().getName() + "\" has unsaved changes.");
        } else {
            ImGui.text("These rooms have unsaved changes:");
            for (RoomTab tab : unsaved) {
                ImGui.bulletText(tab.getRoom().getName());
            }
        }
        ImGui.spacing();
        boolean save = ImGui.button(unsaved.size() == 1 ? "Save" : "Save All");
        ImGui.sameLine();
        boolean discard = ImGui.button("Don't Save");
        ImGui.sameLine();
        boolean cancel = ImGui.button("Cancel");
        if (save || discard || cancel) ImGui.closeCurrentPopup();
        ImGui.endPopup();

        if (cancel || (save && !saveAll(unsaved))) {
            tabAskedToClose = null;
            afterLeavePrompt = null;
        } else if (save || discard) {
            finishPrompt();
        }
    }

    private boolean saveAll(List<RoomTab> toSave) {
        for (RoomTab tab : toSave) {
            if (!save(tab)) return false;
        }
        return true;
    }

    private void finishPrompt() {
        RoomTab tab = tabAskedToClose;
        Runnable action = afterLeavePrompt;
        tabAskedToClose = null;
        afterLeavePrompt = null;
        if (tab != null) closeTab(tab);
        if (action != null) action.run();
    }

    /** A floating panel has a title bar of its own, which already moves it. */
    private static boolean isDocked() {
        int dockId = ImGui.getWindowDockID();
        if (dockId == 0) return false;

        ImGuiDockNode node = ImGui.dockBuilderGetNode(dockId);
        return node != null && !node.isFloatingNode();
    }

    /**
     * The plain bar at the top, standing in for the title bar that a docked window without a tab bar doesn't get. Only shown while docked.
     * Dragging anywhere on it undocks the whole panel and moves it.
     */
    private void drawHeader() {
        float width = ImGui.getContentRegionAvailX();
        float height = ImGui.getFrameHeight();
        float x = ImGui.getCursorScreenPosX();
        float y = ImGui.getCursorScreenPosY();

        ImGui.getWindowDrawList().addRectFilled(x, y, x + width, y + height, ImGui.getColorU32(ImGuiCol.TitleBg));
        ImGui.getWindowDrawList().addText(x + ImGui.getStyle().getFramePaddingX() * 2f,
            y + ImGui.getStyle().getFramePaddingY(), ImGui.getColorU32(ImGuiCol.Text), TITLE);
        ImGui.invisibleButton("##SceneHeader", width, height);
        moveWindowWhenDragged();
    }

    private void moveWindowWhenDragged() {
        if (!ImGui.isMouseDown(ImGuiMouseButton.Left)) movingWindow = false;
        if (movingWindow || !ImGui.isItemActive() || !ImGui.isMouseDragging(ImGuiMouseButton.Left)) return;

        movingWindow = true;
        ImGui.startMouseMovingWindowOrNode(ImGui.getCurrentWindow(),
            ImGui.dockBuilderGetNode(ImGui.getWindowDockID()), true);
    }

    public void dispose() {
        windowClass.destroy();
    }

    /** Whether the mouse was over the window, including its overlay, when it was last drawn. */
    public boolean isHovered() {
        return hovered;
    }

    private void drawTabs() {
        boolean addRoom = false;
        RoomTab tabToClose = null;
        if (ImGui.beginTabBar("RoomTabs", TAB_BAR_FLAGS)) {
            for (RoomTab tab : List.copyOf(tabs)) {
                if (!drawTab(tab)) tabToClose = tab;
            }
            addRoom = TabButtons.drawAdd();
            ImGui.endTabBar();
        }
        drawRenameInput();
        if (tabToClose != null) {
            requestClose(tabToClose);
        }
        if (addRoom) {
            openNewRoom(roomFactory.apply(newRoomName()));
        }
    }

    /** Returns false when the user asked to close the tab. */
    private boolean drawTab(RoomTab tab) {
        int flags = tab == tabToSelect ? ImGuiTabItemFlags.SetSelected : ImGuiTabItemFlags.None;
        // The rename field is drawn over the tab, so the old name must not show through.
        if (tab == renamingTab) ImGui.pushStyleColor(ImGuiCol.Text, 0);
        boolean selected = ImGui.beginTabItem(tab.getLabel(TabButtons.closeButtonPadding()), flags);
        if (tab == renamingTab) ImGui.popStyleColor();
        trackRename(tab);
        boolean close = TabButtons.drawClose(tab.isUnsaved());
        close |= drawTabMenu(tab);
        if (!selected) return !close;

        if (tab == tabToSelect) tabToSelect = null;
        if (tab != activeTab) {
            // The selection holds objects of the room that was shown, which are no longer in view.
            selection.clear();
            activeTab = tab;
        }
        drawContent(tab);
        ImGui.endTabItem();
        return !close;
    }

    /** The right click menu of the tab just submitted. Returns whether Close was chosen. */
    private boolean drawTabMenu(RoomTab tab) {
        if (!ImGui.beginPopupContextItem("##RoomTabMenu")) return false;

        boolean close = false;
        if (ImGui.menuItem("Rename")) startRename(tab);
        if (ImGui.menuItem("Save", "", false, tab.isUnsaved())) save(tab);
        if (ImGui.menuItem("Close")) close = true;
        ImGui.endPopup();
        return close;
    }

    private void startRename(RoomTab tab) {
        renamingTab = tab;
        renameText.set(tab.getRoom().getName());
        renameActive = false;
    }

    /** Call right after a tab item: the tab is the last item. Double clicking it starts renaming. */
    private void trackRename(RoomTab tab) {
        if (ImGui.isItemHovered() && ImGui.isMouseDoubleClicked(ImGuiMouseButton.Left)) {
            startRename(tab);
        }
        if (tab == renamingTab) {
            renameX = ImGui.getItemRectMinX();
            renameY = ImGui.getItemRectMinY();
            renameWidth = ImGui.getItemRectMaxX() - renameX;
        }
    }

    /** A text field over the tab being renamed. It starts with everything selected and applies when it loses focus. */
    private void drawRenameInput() {
        if (renamingTab == null) return;

        ImGui.setCursorScreenPos(renameX, renameY);
        ImGui.setNextItemWidth(renameWidth);
        if (!renameActive) ImGui.setKeyboardFocusHere();
        // No background of its own: the tab is already there, with its rounded corners.
        ImGui.pushStyleColor(ImGuiCol.FrameBg, 0);
        ImGui.pushStyleColor(ImGuiCol.FrameBgHovered, 0);
        ImGui.pushStyleColor(ImGuiCol.FrameBgActive, 0);
        ImGui.pushStyleColor(ImGuiCol.TextSelectedBg, 1f, 1f, 1f, 0.35f);
        ImGui.inputText("##RenameRoom", renameText, ImGuiInputTextFlags.AutoSelectAll | ImGuiInputTextFlags.EnterReturnsTrue);
        ImGui.popStyleColor(4);
        if (ImGui.isItemActive()) {
            renameActive = true;
        } else if (renameActive) {
            RoomTab tab = renamingTab;
            renamingTab = null;
            rename(tab, renameText.get().trim());
        }
    }

    private void rename(RoomTab tab, String name) {
        Room room = tab.getRoom();
        if (name.equals(room.getName())) return;

        String problem = nameProblem(tab, name);
        if (problem != null) {
            errorSink.accept(problem);
            return;
        }
        room.setName(name);
        recheckUnsaved = true;
    }

    private String nameProblem(RoomTab tab, String name) {
        if (name.isEmpty()) return "A room needs a name.";
        if (name.matches(".*[\\\\/:*?\"<>|].*")) return "A room name cannot contain any of \\ / : * ? \" < > |";

        boolean usedByOpenRoom = tabs.stream().anyMatch(other -> other != tab
            && other.getRoom().getName().equalsIgnoreCase(name));
        boolean usedBySavedRoom = storage.exists(name) && !name.equalsIgnoreCase(tab.getSavedName());
        if (usedByOpenRoom || usedBySavedRoom) return "There is already a room called \"" + name + "\".";
        return null;
    }

    private String newRoomName() {
        for (int number = tabs.size() + 1; ; number++) {
            String name = ROOM_NAME_PREFIX + number;
            boolean taken = tabs.stream().anyMatch(tab -> tab.getRoom().getName().equals(name)) || storage.exists(name);
            if (!taken) return name;
        }
    }

    private void drawContent(RoomTab tab) {
        int width = (int) ImGui.getContentRegionAvailX();
        int height = (int) ImGui.getContentRegionAvailY();
        if (width <= 0 || height <= 0) return;

        Room room = tab.getRoom();
        viewport.setCamera(tab.getCamera());
        renderer.render(room, tab.getCamera(), width, height);
        ImGui.image(renderer.getTextureHandle(), width, height, 0, 1, 1, 0);
        viewport.updateFromLastItem();
        boolean imageHovered = ImGui.isItemHovered();
        input.acceptDrops(room);

        overlay.draw(room);
        input.handle(room, imageHovered && !overlay.isHovered());
    }
}
