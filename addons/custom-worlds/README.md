# Custom Worlds Addon

A world generation addon for the **Dimensions** plugin that automatically creates and registers a **custom world** when a portal is registered, using a configurable world generator.

## Features
- Creates a Bukkit world for a portal's destination on startup/portal registration.
- Supports multiple built-in world generator types (e.g., void, default, etc.).
- Configurable world environment (NORMAL, NETHER, THE_END).
- Supports a custom seed for reproducible world generation.

## Configuration
Add the following to your portal's configuration file:
```yaml
Addon:
  WorldGenerator:
    Name: "VOID"          # Generator type (e.g., VOID, DEFAULT)
    Environment: "NORMAL" # World environment: NORMAL, NETHER, THE_END
    Seed: 12345           # Optional seed (random if omitted)
```
