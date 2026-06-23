# Permissions Addon

A permission-based access control addon for the **Dimensions** plugin that requires players to hold a **specific permission node** in order to ignite or use a portal.

## Features
- Checks that a player has a configured permission before allowing portal ignition.
- Checks that a player has a configured permission before allowing portal use (teleportation).
- Sends a configurable deny message to players who lack the permission.
- Permission node and deny message are configurable per portal.

## Configuration
Add the following to your portal's configuration file:
```yaml
Addon:
  Permission:
    Node: "myplugin.portals.vip"
    DenyMessage: "&cYou don't have permission to use this portal."
```

Set `Node` to `none` (or omit the key) to disable this restriction for that portal.
