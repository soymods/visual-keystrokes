# Visual Keystrokes

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21--1.21.11-00AA00?style=for-the-badge&logo=minecraft)](https://minecraft.net)
[![Fabric](https://img.shields.io/badge/Fabric-0.18.3-CC6E3E?style=for-the-badge&logo=modrinth)](https://fabricmc.net)
[![Java](https://img.shields.io/badge/Java-21+-FF6B6B?style=for-the-badge&logo=openjdk)](https://openjdk.java.net)
[![License](https://img.shields.io/badge/License-See%20LICENSE-lightgrey?style=for-the-badge)](LICENSE.txt)
![Build Status](https://img.shields.io/badge/Build-Local-brightgreen?style=for-the-badge)

A highly customizable keystroke overlay for Minecraft 1.21-1.21.11. It renders a live, configurable layout of keyboard and mouse
inputs so players can see exactly what is being pressed in real time.

## Quick Start

### Prerequisites
- **Minecraft**: 1.21-1.21.11
- **Fabric Loader**: 0.18.3 or higher
- **Fabric API**: 0.140.2+1.21.11
- **Java**: 21 or higher

### Installation

1. **Install Fabric Loader**
   - Download and install Fabric Loader for your Minecraft version
   - [Download from FabricMC](https://fabricmc.net/use/installer/)

2. **Install Fabric API**
   - Download the Fabric API build that matches your Minecraft version
   - [Download from Modrinth](https://modrinth.com/mod/fabric-api)

3. **Install Visual Keystrokes**
   - Build the mod or download a release jar
   - Place the jar in your `mods` folder

4. **Launch and Configure**
   - Start Minecraft with Fabric Loader
   - Toggle the overlay with `F8` (changeable in controls)

## Features
- Live key and mouse button visualization with pressed-state feedback
- Fully editable layout via a JSON config file
- Scalable overlay with adjustable position, colors, and padding
- Lightweight HUD renderer with a simple toggle keybind

## Usage
- Toggle the overlay with `F8` by default (change in controls menu)
- Use the pause menu button **Edit Keystrokes** to move, resize, or remove elements in-game
- In the editor: click an element to select it, drag to move, use the corner handles to resize, click the trash icon or drop
  it into the sidebar to remove, and use the **+**/**-** button to open or close the right sidebar to drag elements back in
- After first launch, edit `.minecraft/visualkeystrokes/visualkeystrokes.json` to customize:
  - Key positions, sizes, labels
  - Overlay colors and scale
  - Default enabled state and padding

## Compatibility
- Targets Minecraft 1.21-1.21.11
- Fabric Loader 0.18.3+
- Fabric API resolves per target version

## Development

### Building from Source

1. **Clone the Repository**
   ```bash
   git clone <repo-url>
   cd visual-keystrokes
   ```

2. **Generate Sources**
   ```bash
   ./gradlew genSources
   ```

3. **Import to IDE**
   - Import as a Gradle project
   - Wait for dependencies to resolve

4. **Build the Mod**
   ```bash
   ./gradlew build
   ```
   Output will be in `build/libs/`

5. **Build a Specific Minecraft Version**
   ```bash
   ./gradlew clean build -Pmc_version=1.21.4
   ```
   Output will be in `build/libs/`

6. **Build All Supported Versions**
   ```bash
   ./gradlew buildAllTargets
   ```
   Outputs will be in `build/libs/` with per-version jar names.

5. **Run in Development**
   ```bash
   ./gradlew runClient
   ```

## Version Information

| Component | Version |
|-----------|---------|
| **Mod Version** | 1.0.0 |
| **Minecraft Version** | 1.21-1.21.11 (default 1.21.11) |
| **Yarn Mappings** | Resolved per target (default 1.21.11+build.3) |
| **Fabric Loader** | 0.18.3 |
| **Fabric API** | Resolved per target (default 0.140.2+1.21.11) |

### Development Guidelines
- Follow Java coding conventions
- Add comments for complex logic
- Test your changes thoroughly
- Update documentation as needed

## License

See [`LICENSE.txt`](LICENSE.txt).

## Bug Reports & Feature Requests

- **Bug Reports**: open a GitHub issue with repro steps and logs
- **Feature Requests**: open a GitHub issue with use cases and mockups if possible

## Support

- **Documentation**: check this README and in-game tooltips
- **Issues**: file an issue in the project tracker

---

<div align="center">

[![GitHub](https://img.shields.io/badge/GitHub-Repository-black?style=for-the-badge&logo=github)](https://github.com/soymods/visual-keystrokes)
[![Modrinth](https://img.shields.io/badge/Modrinth-Download-00D5AA?style=for-the-badge&logo=modrinth)](https://modrinth.com/mod/visual-keystrokes)

</div>
