# Portable Portals Addon

A mobility addon for the **Dimensions** plugin that lets players **spawn temporary portal instances** at custom locations with configurable constraints, as if carrying a portal in their inventory.

## Features
- Provides a `/dim spawnPortal -portal <portal>` command to spawn a temporary portal at any location.
- Supports forcing the spawned portal to teleport players to a **specific location** or **specific world**.
- Supports "solo use" mode — only the player who spawned the portal can use it.
- Temporary portals are automatically destroyed after use.
- Integrates with the portal break and use events for cleanup.

## Commands
| Command | Description |
|---|---|
| `/dim spawnPortal -portal <portal> [args...]` | Spawn a temporary instance of the given portal |

## Configuration
Add the following to your portal's configuration file:
```yaml
Addon:
  PortablePortals:
    ForceLocation: "world, 100, 64, 200"  # Optional fixed destination
    ForceWorld: "world_nether"             # Optional fixed destination world
    SoloUse: true                          # If true, only the spawner can use it
```
