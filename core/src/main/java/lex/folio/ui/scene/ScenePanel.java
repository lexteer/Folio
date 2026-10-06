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
import imgui.internal.flag.ImGuiDockNodeFlags;
import lex.folio.model.Room;
import lex.folio.scene.Selection;
import lex.folio.scene.camera.SceneCamera;
import lex.folio.scene.render.SceneRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * The window showing the rooms: one tab per room, each with its own camera. The window docks as a whole, but nothing
 * can be tabbed into it or out of it, so the room tabs are the only tabs it ever has.
 */
public class ScenePanel {
    public static final String TITLE = "Scene";
    private static final String NEW_ROOM_BUTTON = "+";
    private static final String ROOM_NAME_PREFIX = "Room ";
    private static final int WINDOW_FLAGS = ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoScrollWithMouse
        | ImGuiWindowFlags.NoCollapse;
    private static final int TAB_BAR_FLAGS = ImGuiTabBarFlags.Reorderable | ImGuiTabBarFlags.FittingPolicyScroll;
    /** Keeps the dock node from becoming a tab group, and from showing any tab or title bar of its own. */
    private static final int DOCK_NODE_FLAGS = ImGuiDockNodeFlags.NoTabBar
        | ImGuiDockNodeFlags.NoDockingOverMe | ImGuiDockNodeFlags.NoDockingOverOther;

    private final List<RoomTab> tabs = new ArrayList<>();
    private final float pixelsPerMeter;
    private final Function<String, Room> roomFactory;
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

    public ScenePanel(List<Room> rooms, float pixelsPerMeter, Function<String, Room> roomFactory,
                      SceneRenderer renderer, SceneViewport viewport, SceneOverlay overlay, SceneInput input,
                      Selection selection) {
        this.pixelsPerMeter = pixelsPerMeter;
        this.roomFactory = roomFactory;
        this.renderer = renderer;
        this.viewport = viewport;
        this.overlay = overlay;
        this.input = input;
        this.selection = selection;
        windowClass.setDockNodeFlagsOverrideSet(DOCK_NODE_FLAGS);

        for (Room room : rooms) {
            addTab(room);
        }
    }

    private RoomTab addTab(Room room) {
        RoomTab tab = new RoomTab(room, new SceneCamera(pixelsPerMeter));
        tabs.add(tab);
        return tab;
    }

    public void draw() {
        ImGui.setNextWindowClass(windowClass);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 0, 0);
        boolean visible = ImGui.begin(TITLE, WINDOW_FLAGS);
        ImGui.popStyleVar();
        hovered = ImGui.isWindowHovered(ImGuiHoveredFlags.RootAndChildWindows);

        if (visible) {
            drawHeader();
            drawTabs();
        }
        ImGui.end();
    }

    /**
     * The plain bar at the top, standing in for the title bar that a docked window without a tab bar doesn't get.
     * Dragging anywhere on it moves the whole panel, undocking it first if it is docked.
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
        int dockId = ImGui.getWindowDockID();
        if (dockId == 0) {
            ImGui.startMouseMovingWindow(ImGui.getCurrentWindow());
        } else {
            ImGui.startMouseMovingWindowOrNode(ImGui.getCurrentWindow(), ImGui.dockBuilderGetNode(dockId), true);
        }
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
        if (ImGui.beginTabBar("RoomTabs", TAB_BAR_FLAGS)) {
            for (RoomTab tab : tabs) {
                drawTab(tab);
            }
            addRoom = ImGui.tabItemButton(NEW_ROOM_BUTTON, ImGuiTabItemFlags.Trailing | ImGuiTabItemFlags.NoTooltip);
            ImGui.endTabBar();
        }
        if (addRoom) {
            tabToSelect = addTab(roomFactory.apply(newRoomName()));
        }
    }

    private void drawTab(RoomTab tab) {
        int flags = tab == tabToSelect ? ImGuiTabItemFlags.SetSelected : ImGuiTabItemFlags.None;
        if (!ImGui.beginTabItem(tab.getLabel(), flags)) return;

        if (tab == tabToSelect) tabToSelect = null;
        if (tab != activeTab) {
            // The selection holds objects of the room that was shown, which are no longer in view.
            selection.clear();
            activeTab = tab;
        }
        drawContent(tab);
        ImGui.endTabItem();
    }

    private String newRoomName() {
        for (int number = tabs.size() + 1; ; number++) {
            String name = ROOM_NAME_PREFIX + number;
            if (tabs.stream().noneMatch(tab -> tab.getRoom().getName().equals(name))) return name;
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
