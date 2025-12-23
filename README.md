# Visual Keystrokes

Visual Keystrokes is a highly customizable keystroke overlay for Minecraft 1.21.x. It renders a live, configurable layout of keyboard and mouse inputs so players can see exactly what is being pressed in real time.

## Features
- Live key and mouse button visualization with pressed-state feedback.
- Fully editable layout via a JSON config file.
- Scalable overlay with adjustable position, colors, and padding.
- Lightweight HUD renderer with a simple toggle keybind.

## Development Setup
1. Install Java 21.
2. Run `./gradlew runClient` to launch a dev client.
3. Build with `./gradlew build`.

## Usage
- Toggle the overlay with `F8` by default (change in controls menu).
- Use the pause menu button **Edit Keystrokes** to move, resize, or remove elements in-game.
- In the editor: click an element to select it, drag to move, use the corner handles to resize, click the trash icon or drop it into the sidebar to remove, and use the **+**/**-** button to open/close the right sidebar to drag elements back in.
- After first launch, edit `.minecraft/visualkeystrokes/visualkeystrokes.json` to customize:
  - Key positions, sizes, labels.
  - Overlay colors and scale.
  - Default enabled state and padding.

## Version Targets
- Minecraft: 1.21.1 (compatible with 1.21.x)
- Loader: Fabric 0.16.x

## License
See `LICENSE.txt`.
