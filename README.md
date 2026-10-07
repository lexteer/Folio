# Folio

A 2D Level Editor for games for hand drawn assets. It does not feature tiling.

Fully written with LibGDX framework.

## Code layout

Most classes do one thing; a few "glue" classes wire them together.

| Package | Responsibility |
| --- | --- |
| `model` | Plain project data: `Project`, `Room`, layers, objects, assets. No rendering or UI. |
| `command` | Undo/redo: `Command`, `CommandStack` and the reusable commands. |
| `assets` | Loading, importing, searching and moving asset files. |
| `project` | Creating projects (always in a new folder) and opening them (`ProjectStorage`); saving and loading rooms (`RoomStorage`, one file per room in `rooms/`). |
| `scene.camera` / `scene.render` | The scene camera, and drawing a room into a texture. |
| `scene` | What the scene tools work with: selection, box select, dragging objects, `ObjectPicker`, `ActiveLayers` (the layer new objects go to). |
| `scene.sprite` / `scene.shape` | Sprite geometry, picking and placing; collision shape geometry, placing and the unfinished shape being drawn. |
| `scene.tool` | Tools (`SceneTool`) and the `ToolController` that routes input to them. |
| `ui.scene` / `ui.layers` / `ui.inspector` / `ui.assets` | One package per panel. `ScenePanel`, `LayersPanel`, `InspectorPanel` and `AssetsPanel` are the glue. |
| `ui.common` | Widgets and helpers shared by panels. |
| `app` | `EditorApp` (lifecycle), `Editor` (main menu, welcome popup, the open project), `NewProjectDialog`, `ProjectSession` (wires and draws one project), docking and shortcuts. |

To add a tool: add a value to `Tool`, implement `SceneTool`, and register it in `Editor.createTools`.
To add a kind of layer: add it to the sealed `Layer`, then follow the compiler (saving in `RoomJson`, picking in `ObjectPicker`, the icon in `LayersPanel`).
To add a kind of object: add it to the sealed `RoomObject`; the compiler then points at what is missing.
