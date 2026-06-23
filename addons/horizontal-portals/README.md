# Horizontal Portals Addon

A custom geometry addon for the **Dimensions** plugin that adds support for horizontal portals (portals built flat on the ground/ceiling, like the Twilight Forest portal).

## Features
- Registers horizontal portal geometry.
- Handles filling/spawning portal block data horizontally.
- Integrates custom teleport destination logic for horizontal structures.

## Configuration
To enable horizontal portals for a custom portal type, add the following to its configuration:
```yaml
Addon:
  HorizontalPortal: "true"
```
