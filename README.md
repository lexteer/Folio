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
| `scene.camera` / `scene.render` | The scene camera, and drawing a room into a texture. |
| `scene.sprite` | Sprite geometry, picking, placing and dragging. |
| `scene.tool` | Tools (`SceneTool`) and the `ToolController` that routes input to them. |
| `ui.scene` / `ui.inspector` / `ui.assets` | One package per panel. `ScenePanel`, `InspectorPanel` and `AssetsPanel` are the glue. |
| `ui.common` | Widgets and helpers shared by panels. |
| `app` | `EditorApp` (lifecycle), `Editor` (wires and draws everything), docking and shortcuts. |

To add a tool: add a value to `Tool`, implement `SceneTool`, and register it in `Editor.createTools`.
To add a kind of object: add it to the sealed `RoomObject`; the compiler then points at what is missing.
