# Model Engine Addon

A visual addon for the **Dimensions** plugin that spawns **ModelEngine** model entities at portal locations when the portal is ignited, giving portals a custom 3D animated appearance.

## Features
- Spawns one or more ModelEngine model entities at the center of a portal when it is ignited.
- Removes model entities when the portal is extinguished or destroyed.
- Supports **PlaceholderAPI** placeholders in model IDs (e.g., player-specific models).
- Configurable model ID, position offset, and scale per portal.

## Requirements
- [ModelEngine](https://mythiccraft.io/index.php?resources/model-engine%E2%80%94ultimate-entity-model-manager-1-16-5-1-20.389/) plugin must be installed.
- (Optional) [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) for dynamic model IDs.

## Configuration
Add the following to your portal's configuration file:
```yaml
Addon:
  ModelEngine:
    Models:
      - Id: "my_portal_model"
        X: 0.0   # Offset from portal center
        Y: 0.0
        Z: 0.0
```
