# Hub World Addon

A hub management addon for the **Dimensions** plugin that designates a **hub world** and creates a network of portals within it, randomly routing players to one of multiple linked destination portals.

## Features
- Designates a world as a "hub" — portals inside the hub world will not re-ignite while in the hub.
- Registers multiple destination portals as part of a hub network.
- When a player uses a hub portal, they are sent to a **randomly selected** portal from the hub's pool.
- Supports the `/dim addHub` command to add portals to the hub network in-game.
- Cleans up hub portal entries when a portal in the hub network is destroyed.

## Commands
| Command | Description |
|---|---|
| `/dim addHub` | Adds the portal you are looking at to the hub network |

## Configuration
Add the following to your portal's configuration file:
```yaml
Addon:
  HubWorld:
    World: "hub"   # The name of the hub world
```
