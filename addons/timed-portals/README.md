# Timed Portals Addon

A lifecycle addon for the **Dimensions** plugin that automatically **extinguishes portals** after a configurable amount of time since they were ignited.

## Features
- Starts a countdown when a portal is ignited.
- Automatically closes (de-ignites) the portal after the configured duration expires.
- Plays optional particle effects from the **Particles** addon during the countdown as a visual warning.
- If a portal is re-ignited before the timer expires, the previous timer is cancelled and a new one starts.
- Cancels the timer and cleans up when the portal is manually destroyed.

## Requirements
- The **Particles** addon must be present (included in this project) for countdown particle effects.

## Configuration
Add the following to your portal's configuration file:
```yaml
Addon:
  TimedPortals:
    DestroyAfter: 200    # Time in ticks before the portal closes (20 ticks = 1 second)
```
