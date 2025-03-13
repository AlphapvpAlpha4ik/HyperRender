# HyperRenderX - FPS Optimization Mod for Minecraft

*HyperRenderX* is a Minecraft mod designed to enhance game performance by optimizing rendering, chunk management, particle effects, entity rendering, and more. The mod provides a user-friendly graphical interface for customization, allowing players to tailor optimizations to their needs.

## Key Features

HyperRenderX offers a wide range of features to improve FPS (frames per second), especially on low- or mid-range systems. Key highlights include:

- **Dynamic Render Distance Adjustment**  
  Automatically adjusts render distance based on current FPS to maintain stable performance. Customize minimum and maximum render distances, target FPS, and other parameters.

- **Entity Rendering Optimization**  
  Entities far away or obscured by blocks are culled, reducing system load. For example, zombies, spiders, and bats are culled at specific distances.

- **Particle Optimization**  
  Reduces the rendering of particles (e.g., fire, smoke effects) based on their total count, helping with large effect clusters.

- **Lighting and Chunk Optimization**  
  HyperRenderX uses smart chunk updates and caching to minimize unnecessary lighting recalculations and rendering. Chunks outside the player's view can be deferred or skipped.

- **LOD (Level of Detail) for Bamboo**  
  Bamboo is rendered with varying levels of detail based on distance from the player, reducing GPU load.

- **FSR Shader Optimization**  
  The mod includes support for FidelityFX Super Resolution (FSR) via custom shaders, rendering the game at a lower resolution with upscaling for improved performance.

- **Settings Interface**  
  HyperRenderX provides an intuitive GUI for configuring all mod options, including toggling features and adjusting optimization thresholds.

- **Performance HUD**  
  Displays an FPS counter and details about active optimizations on-screen. Press `F3 + H` to toggle extended performance info (e.g., peak and low FPS).

- **Asynchronous Chunk Loading**  
  Chunks are loaded asynchronously using multithreading, reducing lag during world exploration.

- **Cloud and Animation Optimization**  
  Clouds and animations (e.g., player hand rendering) can be simplified or disabled at low FPS.

## Installation

1. **Requirements**:
    - Minecraft version 1.20.x (Fabric).
    - Installed [Fabric Loader](https://fabricmc.net/use/installer/).
    - Installed [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api).

2. **Downloading the Mod**:
    - Download the latest version of HyperRenderX

3. **Installation**:
    - Place the `hyperrenderx-x.x.x.jar` file in the `mods` folder of your Minecraft client (typically `~/.minecraft/mods` on Windows/Linux or `~/Library/Application Support/minecraft/mods` on macOS).
    - Launch Minecraft with the Fabric profile.

4. **Verification**:
    - If installed correctly, you will see the message `Initializing HyperRenderX Client...` in the logs.
    - The "HyperRenderX Settings" button will appear in the Minecraft options menu.

## Usage

1. **Accessing Settings**:
    - Open the Minecraft options menu (`Options`).
    - Locate the **HyperRenderX Settings** button (usually at the bottom of the screen).
    - Adjust mod settings via the graphical interface.

2. **FPS HUD**:
    - The FPS counter is displayed in the top-left corner if enabled in settings.
    - Press `F3 + H` to toggle extended performance information.

3. **Hotkeys**:
    - `F6` — Forces a render distance update (if enabled in settings).

## Configuration

The mod offers numerous configurable options through its graphical interface. Key categories and settings include:

### General Settings
- **Show FPS Counter** (`options.hyperrenderx.showFpsCounter`)  
  Enables or disables the on-screen FPS counter.

### Performance
- **Smart Chunk Updates** (`options.hyperrenderx.smartChunkUpdates`)  
  Enables smart chunk updates to reduce load.

### Render Distance
- **Auto Adjust Render Distance** (`options.hyperrenderx.autoAdjustRenderDistance`)  
  Enables automatic render distance adjustment.
- **Min/Max Render Distance**  
  Sets the minimum and maximum render distances (from 2 to 32 chunks).
- **Target FPS**  
  Sets the target FPS the mod aims to maintain (default: 60).
- **Min FPS to Decrease/Max FPS to Increase**  
  FPS thresholds for decreasing or increasing render distance.

### Advanced
- **Optimization Threshold** (`options.hyperrenderx.optimizationThreshold`)  
  Threshold for additional optimizations (e.g., disabling clouds at low FPS).

### Configuration File
All settings are saved to a `hyperrenderx.json` file in the `config` folder of your Minecraft installation. Manual editing is possible but using the settings interface is recommended.

## Technical Details

### Rendering Optimization
The mod uses mixins to intercept and optimize Minecraft rendering processes:
- **EntityRendererMixin** — Culls entities based on distance and occlusion.
- **WorldRendererMixin** — Smart chunk caching, cloud culling, and asynchronous loading.
- **GameRendererMixin** — Applies FSR shaders and optimizes hand rendering.

### FSR Shaders
The mod includes shaders for FidelityFX Super Resolution:
- **fsr.vsh** (Vertex Shader):
  ```glsl
  #version 150
  in vec3 Position;
  in vec2 TexCoord;
  out vec2 texCoord;
  void main() {
      gl_Position = vec4(Position, 1.0);
      texCoord = TexCoord;
  }