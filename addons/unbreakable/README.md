# Unbreakable Addon

A protection addon for the **Dimensions** plugin that makes portals **immune to specific destruction causes**, preventing them from being broken by players, explosions, or other configured events.

## Features
- Marks a portal as unbreakable for one or more destruction causes.
- Cancels any `CustomPortalBreakEvent` that matches a configured `CustomPortalDestroyCause`.
- Supported causes include: `PLAYER`, `EXPLOSION`, `PLUGIN`, and others provided by the Dimensions API.
- Per-portal configuration of allowed and blocked destruction causes.

## Configuration
Add the following to your portal's configuration file:
```yaml
Addon:
  Unbreakable:
    - "PLAYER"      # Block players from breaking the portal
    - "EXPLOSION"   # Block explosions from destroying the portal
```

Leave the list empty or omit the key to allow all destruction causes.
