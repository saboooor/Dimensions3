# Random Location Addon

A teleportation addon for the **Dimensions** plugin that sends players to a **random location** near the portal's center when they use it, instead of a fixed destination portal.

## Features
- Teleports the player to a random X/Z coordinate within a configurable range from the portal's center.
- Optionally unlinks the portal's destination so no exit portal is created.
- Can optionally allow the portal to still link to a destination even with random teleportation enabled.
- The world and Y-coordinate are determined by the portal's standard destination logic.

## Configuration
Add the following to your portal's configuration file:
```yaml
Addon:
  RandomLocation:
    Range: 500         # Maximum block radius from portal center
    AllowLink: false   # If true, also use the linked portal as destination
```
