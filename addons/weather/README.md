# Weather Addon

A weather-based access control addon for the **Dimensions** plugin that restricts portal ignition and use based on the **current weather** in the portal's world.

## Features
- Prevents portals from being ignited or used during specific weather conditions.
- Supported weather conditions: `CLEAR`, `RAIN`, `THUNDER`.
- Multiple conditions can be blocked simultaneously.
- Sends a configurable deny message to players when the portal is blocked by weather.

## Configuration
Add the following to your portal's configuration file:
```yaml
Addon:
  Weather:
    Disabled: "RAIN THUNDER"   # Space-separated list of blocked weather types
    DenyMessage: "&cThis portal cannot be used in this weather!"
```
