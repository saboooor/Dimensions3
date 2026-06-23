# Ignite By Event 2 Addon

An event-driven portal ignition addon for the **Dimensions** plugin that allows portals to be **automatically ignited** by in-game events — without requiring a player to hold a lighter item.

## Features
- Ignites a portal automatically when a configured **item is dropped** nearby.
- Ignites a portal automatically when a configured **entity type dies** nearby.
- Each portal can have its own set of trigger events.
- Useful for adventure maps, puzzles, or automated server mechanics.

## Configuration
Add the following to your portal's configuration file:
```yaml
Addon:
  IgniteByEvent:
    Enable: true
    Events:
      DropItem:
        - "diamond"        # Material name of the dropped item
      EntityDeath:
        - "ZOMBIE"         # Entity type name
```
