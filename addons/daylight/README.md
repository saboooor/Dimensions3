# Daylight Addon

A time-based access addon for the **Dimensions** plugin that restricts portal ignition and use to a **specific period of the Minecraft day/night cycle**.

## Features
- Allows portals to only be ignited or used during a configured time window (based on Minecraft world time ticks).
- Supports wrapping time ranges (e.g., night time spanning across the tick 0 boundary).
- Sends a configurable deny message to players who try to use the portal outside the allowed time.

## Configuration
Add the following to your portal's configuration file:
```yaml
Addon:
  Daylight:
    Min: 0        # Minimum world time in ticks (0 = dawn)
    Max: 12000    # Maximum world time in ticks (12000 = noon, 18000 = dusk, 24000 = next dawn)
    DenyMessage: "&cThis portal can only be used during the day!"
```

> **Tip:** Use `/time query daytime` in-game to check the current tick value.
