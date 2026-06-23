# Particles Addon

A visual effects addon for the **Dimensions** plugin that plays **custom particle animations** at a portal while it is active (ignited).

## Features
- Displays configurable particle packs around a portal while it is ignited.
- Supports multiple simultaneous particle packs per portal.
- Each pack has configurable particle type, shape, frequency, and other parameters.
- Automatically hides the default portal particles when a custom particle pack is active.
- Cleans up particle tasks when a portal is extinguished or destroyed.

## Configuration
Add the following to your portal's configuration file:
```yaml
Addon:
  Particles:
    - Type: "FLAME"
      Frequency: 5       # Ticks between each particle burst
      # Additional particle-specific options...
```

> This addon is also used as a **library** by other addons (e.g., Timed Portals) to play delayed particle effects.
