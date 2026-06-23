# Message On Use Addon

A notification addon for the **Dimensions** plugin that sends a **custom chat message** to a player when they use a portal.

## Features
- Sends a configurable message to the player upon successful portal use.
- Supports color codes (using `&` as a prefix).
- Message is configured per portal.

## Configuration
Add the following to your portal's configuration file:
```yaml
Addon:
  MessageOnUse: "&aWelcome to the other side!"
```

Set the value to `none` (or omit the key) to disable the message for that portal.
