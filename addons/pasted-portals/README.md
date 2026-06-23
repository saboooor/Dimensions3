# Pasted Portals Addon

An automation addon for the **Dimensions** plugin that **automatically ignites portals** that have been pasted into the world via **WorldEdit**, **BentoBox**, **IridiumSkyblock**, or **SuperiorSkyblock2** — so they activate without a player lighting them manually.

## Features
- Detects portals pasted by WorldEdit schematics and automatically ignites them.
- Integrates with skyblock plugins to ignite portals when a player's island is created or regenerated:
  - **BentoBox** — listens for island generation events.
  - **IridiumSkyblock** (v4.x) — scans island bounds on creation.
  - **SuperiorSkyblock2** — scans island bounds on creation.
- Can optionally ignite portals during world chunk population (for portal generation via world gen).
- Reads portal sign data to determine which portal type to ignite.

## Requirements
- [WorldEdit](https://enginehub.org/worldedit) (optional) — for schematic paste detection.
- [BentoBox](https://www.spigotmc.org/resources/bentobox-island-manager.73261/) (optional) — for BentoBox skyblock integration.
- [IridiumSkyblock](https://www.spigotmc.org/resources/iridiumskyblock.62480/) v4+ (optional) — for IridiumSkyblock integration.
- [SuperiorSkyblock2](https://www.spigotmc.org/resources/superiorskyblock2.87411/) (optional) — for SuperiorSkyblock2 integration.

## Configuration
Enable the desired integrations in the Dimensions `config.yml`:
```yaml
PastedPortals:
  WorldEdit: true
  Skyblock: true
  OnWorldGeneration: false
```
