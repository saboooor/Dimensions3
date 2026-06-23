# Commands On Use Addon

An automation addon for the **Dimensions** plugin that executes configurable **console or player commands** when a portal is ignited or used.

## Features
- Runs a list of commands as the **console** or as the **player** when a portal is used.
- Supports **PlaceholderAPI** placeholders in commands (e.g., `%player_name%`).
- Can optionally **disable teleportation**, turning the portal into a pure command trigger with no TP.
- Commands can be configured to run on portal ignition, on player use, or both.
- Supports per-portal command lists.

## Requirements
- (Optional) [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) for placeholder support in commands.

## Configuration
Add the following to your portal's configuration file:
```yaml
Addon:
  CommandsOnUse:
    DisableTp: false           # Set to true to block teleportation
    Commands:
      - "console: say %player_name% used the portal!"
      - "player: warp spawn"
```
