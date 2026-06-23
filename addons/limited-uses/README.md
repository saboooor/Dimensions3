# Limited Uses Addon

A portal durability addon for the **Dimensions** plugin that gives portals a **maximum number of uses** before they are automatically closed or destroyed.

## Features
- Tracks the total number of times a portal has been used.
- When the use limit is reached, the portal can either:
  - **Close** — the portal is de-ignited (frame remains intact).
  - **Destroy** — the portal frame is physically removed.
- Integrates with the Hub World addon to optionally exclude hub-side portal uses from the count.
- Limit and action are configurable per portal.

## Configuration
Add the following to your portal's configuration file:
```yaml
Addon:
  LimitedUses:
    MaxUses: 10
    Action: "Close"    # Options: Close, Destroy
```
