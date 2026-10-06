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
import imgui.type.ImBoolean;
import imgui.internal.ImGuiDockNode;
import imgui.internal.flag.ImGuiDockNodeFlags;
import lex.folio.model.Room;
import lex.folio.scene.Selection;
import lex.folio.scene.camera.SceneCamera;
import lex.folio.project.RoomStorage;
import lex.folio.scene.render.SceneRenderer;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * The window showing the rooms: one tab per room, each with its own camera. The window docks as a whole, but nothing
 * can be tabbed into it or out of it, so the room tabs are the only tabs it ever has.
 */
public class ScenePanel {
    public static final String TITLE = "Scene";
    private static final String NEW_ROOM_BUTTON = "+";
    private static final String ROOM_NAME_PREFIX = "Room ";
    private static final String UNSAVED_POPUP = "Unsaved changes";
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
    private final Deque<RoomTab> tabsToAskAbout = new ArrayDeque<>();
    private boolean closeTabsAfterAsking;
    private Runnable afterAsking;

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

    public boolean canSaveActiveRoom() {
        return activeTab != null && activeTab.isUnsaved();
    }

    public void saveActiveRoom() {
        if (canSaveActiveRoom()) save(activeTab);
    }

    public boolean hasUnsavedRooms() {
        return tabs.stream().anyMatch(RoomTab::isUnsaved);
    }

    /** Asks about each room with unsaved changes, then runs {@code action}. Cancelling anywhere abandons it. */
    public void runWhenNothingIsUnsaved(Runnable action) {
        if (!tabsToAskAbout.isEmpty()) return;

        tabs.stream().filter(RoomTab::isUnsaved).forEach(tabsToAskAbout::add);
        closeTabsAfterAsking = false;
        afterAsking = action;
    }

    private boolean save(RoomTab tab) {
        try {
            tab.markSaved(storage.save(tab.getRoom()));
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
        tabsToAskAbout.add(tab);
        closeTabsAfterAsking = true;
        afterAsking = null;
    }

    private void closeTab(RoomTab tab) {
        tabs.remove(tab);
        if (tab == activeTab) {
            activeTab = null;
            selection.clear();
        }
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

    /** Asks, one room at a time, whether to save it. Draws nothing when there is nothing to ask. */
    private void drawUnsavedPrompt() {
        RoomTab tab = tabsToAskAbout.peek();
        if (tab == null) return;

        if (!ImGui.isPopupOpen(UNSAVED_POPUP)) {
            ImGui.openPopup(UNSAVED_POPUP);
        }
        if (!ImGui.beginPopupModal(UNSAVED_POPUP, ImGuiWindowFlags.AlwaysAutoResize)) return;

        ImGui.text("Room \"" + tab.getRoom().getName() + "\" has unsaved changes. Save it?");
        ImGui.spacing();
        boolean save = ImGui.button("Save");
        ImGui.sameLine();
        boolean discard = ImGui.button("Don't Save");
        ImGui.sameLine();
        boolean cancel = ImGui.button("Cancel");
        if (save || discard || cancel) ImGui.closeCurrentPopup();
        ImGui.endPopup();

        if (cancel || (save && !save(tab))) {
            tabsToAskAbout.clear();
            afterAsking = null;
        } else if (save || discard) {
            tabsToAskAbout.remove();
            if (closeTabsAfterAsking) closeTab(tab);
            if (tabsToAskAbout.isEmpty() && afterAsking != null) {
                Runnable action = afterAsking;
                afterAsking = null;
                action.run();
            }
        }
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
            addRoom = ImGui.tabItemButton(NEW_ROOM_BUTTON, ImGuiTabItemFlags.Trailing | ImGuiTabItemFlags.NoTooltip);
            ImGui.endTabBar();
        }
        if (tabToClose != null) {
            requestClose(tabToClose);
        }
        if (addRoom) {
            openNewRoom(roomFactory.apply(newRoomName()));
        }
    }

    /** Returns false when the close button of the tab was clicked. */
    private boolean drawTab(RoomTab tab) {
        int flags = tab == tabToSelect ? ImGuiTabItemFlags.SetSelected : ImGuiTabItemFlags.None;
        if (tab.isUnsaved()) flags |= ImGuiTabItemFlags.UnsavedDocument;
        ImBoolean open = new ImBoolean(true);
        if (!ImGui.beginTabItem(tab.getLabel(), open, flags)) return open.get();

        if (tab == tabToSelect) tabToSelect = null;
        if (tab != activeTab) {
            // The selection holds objects of the room that was shown, which are no longer in view.
            selection.clear();
            activeTab = tab;
        }
        drawContent(tab);
        ImGui.endTabItem();
        return open.get();
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
