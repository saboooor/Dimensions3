# Biomes Addon

A conditional access addon for the **Dimensions** plugin that restricts portal ignition and use based on the **biome** where the portal is located.

## Features
- Allows configuring a whitelist of biomes where a portal can be ignited/used.
- Allows configuring a blacklist of biomes where a portal is blocked.
- Sends a configurable deny message to players who attempt to use the portal in a restricted biome.

## Configuration
Add the following to your portal's configuration file:
```yaml
Addon:
  Biomes:
    Enabled:    # Whitelist — only these biomes allow the portal
      - JUNGLE
      - FOREST
    Disabled:   # Blacklist — these biomes block the portal
      - DESERT
    DenyMessage: "&cYou cannot use this portal here!"
```
