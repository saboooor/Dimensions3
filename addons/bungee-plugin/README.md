# Dimensions Bungee Plugin

This is a BungeeCord proxy plugin that coordinates cross-server portal teleportation for the **Dimensions** plugin. It listens to the `dimensions:addons` plugin messaging channel, intercepts player teleports initiated by portals on Spigot/Paper servers, and routes them to the correct target server.

## Features

- **Cross-Server Teleportation**: Seamlessly teleport players across different servers in your BungeeCord network using Dimensions portals.
- **Source-Specific Routing**: Route players to different servers depending on which server they are traveling from.
- **Fallback Server**: Safely redirect players to a fallback server if a teleport fails or target is unspecified.

## Installation

1. Copy the compiled `BungeePlugin-4.0.0.jar` into your BungeeCord proxy's `plugins/` folder.
2. Restart your BungeeCord proxy to generate the configuration folder.

## Configuration (`plugins/DimensionsBungee/config.yml`)

```yaml
configVersion: "1.0.0"

# The server to send players to if their destination server is offline or unavailable
fallbackServer: "main"

# Routing mapping for portals
# Formats:
# 1. "PORTAL_ID->TARGET_SERVER" - Simple portal to server mapping
# 2. "SOURCE_SERVER->PORTAL_ID->TARGET_SERVER" - Context-aware routing based on the origin server
Portals:
  - "aether->aether_server"
  - "lobby->nether->nether_server"
```

## Dependencies

- **BungeeCord** or compatible proxy (e.g., Waterfall).
- The main **Dimensions** plugin installed on your backend Spigot/Paper servers.
