# Bungee Addon

A cross-server teleportation addon for the **Dimensions** plugin that allows portals to send players to a specific location on **another BungeeCord server**.

## Features
- Teleports players through a portal to a configured server, world, and coordinate on a BungeeCord network.
- Uses BungeeCord's plugin messaging channel (`BungeeCord`) to connect servers.
- Applies a blindness effect during the cross-server transition for a smooth experience.
- Queues players who are mid-transit and teleports them to the correct location once they join the destination server.
- Supports `GameMode.SPECTATOR` check to handle edge cases during transit.

## Requirements
- A running **BungeeCord** (or compatible) proxy.
- The **Dimensions** plugin must be installed on all connected servers.

## Configuration
Add the following to your portal's configuration file:
```yaml
Addon:
  Bungee:
    Server: "lobby"      # Target BungeeCord server name
    World: "world"       # Target world on that server
    X: 0.0
    Y: 64.0
    Z: 0.0
```
