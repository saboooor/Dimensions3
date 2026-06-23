# Stylish Portals Addon

A visual customization addon for the **Dimensions** plugin that lets you give portal **frames** and **interiors** a custom block appearance using vanilla blocks or custom block plugins.

## Features
- Renders a custom block pattern on the portal's **frame** blocks.
- Renders a custom block pattern on the portal's **inside** (filling) blocks using fake block data.
- Supports per-side and per-position block patterns (frame style and side pattern).
- Supports **Oraxen**, **ItemsAdder**, and **CustomItems** custom blocks for portal frames.
- Provides a `/dim blockData` command to retrieve the string block data of the block you're looking at.
- Uses a proxy-based `FakeBlockData` to safely represent custom block appearances across server versions.

## Commands
| Command | Description |
|---|---|
| `/dim blockData` | Get the block data string of the block you are looking at |

## Requirements
- (Optional) [Oraxen](https://www.spigotmc.org/resources/oraxen.72448/) for Oraxen custom frame blocks.
- (Optional) [ItemsAdder](https://www.spigotmc.org/resources/itemsadder.73355/) for ItemsAdder custom frame blocks.
- (Optional) CustomItems plugin for CustomItems frame blocks.

## Configuration
Add the following to your portal's configuration file:
```yaml
Addon:
  StylishPortal:
    FrameStyle:
      - "minecraft:crying_obsidian"   # Block data string for frame
      - "oraxen:my_custom_block"      # Or an Oraxen block ID
```
