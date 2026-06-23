# Charge On Use Addon

An economy integration addon for the **Dimensions** plugin that charges players a configurable amount of **in-game currency** each time they use a portal.

## Features
- Deducts a configured amount from the player's balance on every successful portal use.
- Cancels the portal use and sends a deny message if the player cannot afford it.
- Supports per-portal charge amounts and deny messages.
- Integrates with **Vault** for economy provider compatibility.
- Optionally exempts certain players (e.g., by group or permission) from being charged.

## Requirements
- [Vault](https://www.spigotmc.org/resources/vault.34315/) plugin must be installed.
- A compatible economy plugin (e.g., EssentialsX Economy, CMI) must be present.

## Configuration
Add the following to your portal's configuration file:
```yaml
Addon:
  ChargeOnUse:
    Amount: 100.0
    DenyMessage: "&cYou need $100 to use this portal!"
```
