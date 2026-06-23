# Custom Lighter Addon

A customization addon for the **Dimensions** plugin that allows configuring **custom lighter items**, **frame blocks**, and **inside blocks** for portals — including support for custom item plugins.

## Features
- Lets you set a custom item (vanilla, Oraxen, ItemsAdder, or CustomItems) as the "lighter" that ignites a portal.
- Lets you configure custom **frame block data** and **inside block data** using vanilla or custom block plugins.
- Provides in-game commands to set the lighter/frame/inside block for a portal by holding the desired item.
- Supports Oraxen, ItemsAdder, and CustomItems custom blocks for frame and inside visuals.

## Commands
| Command | Description |
|---|---|
| `/dim setLighter <portal>` | Set the held item as the portal's lighter |
| `/dim setFrameBlock <portal>` | Set the held/looked-at block as the portal's frame block |
| `/dim setInsideBlock <portal>` | Set the held/looked-at block as the portal's inside block |

## Requirements
- (Optional) [Oraxen](https://www.spigotmc.org/resources/oraxen.72448/) for custom block support.
- (Optional) [ItemsAdder](https://www.spigotmc.org/resources/itemsadder.73355/) for custom block support.
- (Optional) CustomItems plugin for custom item/block support.

## Configuration
Add the following to your portal's configuration file:
```yaml
Addon:
  CustomLighter:
    Item: "minecraft:flint_and_steel"  # or an Oraxen/ItemsAdder ID
    FrameBlock: "minecraft:obsidian"
    InsideBlock: "minecraft:purple_stained_glass"
```
