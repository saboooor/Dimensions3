# Better Portals Addon

A visual enhancement addon for the **Dimensions** plugin that integrates with the **BetterPortals** API to render seamless, see-through portal views between linked portals.

## Features
- Renders a real-time view through each portal to its linked destination using the BetterPortals API.
- Supports both vertical and horizontal portal geometries.
- Provides a `/dim mirrorPortal` command to flip the exit orientation of a linked portal pair.
- Automatically re-links portals when they are used or rebuilt.
- Cleans up BetterPortals renderers when portals are destroyed or the addon is disabled.

## Requirements
- [BetterPortals](https://www.spigotmc.org/resources/betterportals.75408/) plugin must be installed.

## Configuration
Add the following to your portal's configuration file to enable this addon:
```yaml
Addon:
  EnableBetterPortals: true  # default: true
```
