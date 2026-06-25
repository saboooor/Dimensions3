# Dimensions Text Display Control Addon

This is an addon for the **Dimensions** plugin that adds granular, high-fidelity control over the data values of `text_display` entities (which are used to render inside sprites in portals) via portal configuration files.

## Features

Customize various visual properties of portal text display sprites:
- **Background Color**: Set custom background colors using Hex codes, standard color names, or decimal integers.
- **Background Opacity**: Set custom background transparency.
- **Sprite/Text Opacity**: Tweak the alpha/opacity level of the portal's inside sprite itself.
- **Visual Flags**:
  - Enable/disable text shadow.
  - Make portals see-through (visible through walls/solid blocks).
  - Toggle Minecraft's default dark grey background container.
- **Formatting**: Adjust text alignment constraints (Left, Right, Center) and custom line-wrapping width.

## Installation

1. Copy the compiled `TextDisplayControl-4.0.0.jar` into your Paper/Spigot server's `plugins/Dimensions/addons/` directory.
2. Restart or reload the Dimensions plugin.

## Configuration

Add the configuration settings under the `Addon.TextDisplayControl` block in any custom portal configuration file located in `plugins/Dimensions/Portals/` (e.g., `aether.yml`):

```yaml
Addon:
  TextDisplayControl:
    # Color of the background container.
    # Supports: Hex codes (#RRGGBB, #AARRGGBB, 0xRRGGBB), standard color names, or "transparent".
    BackgroundColor: "#FF0000"
    
    # Opacity of the background box.
    # If <= 1.0 (e.g., 0.5), it is treated as a percentage (0.0 to 1.0).
    # If > 1.0, it is used directly as a byte value (0 to 255).
    BackgroundOpacity: 0.5
    
    # Opacity of the text/sprite itself. Same scaling rules as BackgroundOpacity.
    SpriteOpacity: 0.8
    
    # Toggles text shadow (boolean)
    Shadow: true
    
    # Toggles whether the sprite is visible through solid blocks (boolean)
    SeeThrough: true
    
    # Toggles Minecraft's default dark grey background box (boolean)
    UseDefaultBackground: false
    
    # Aligns the text display sprite. Options: CENTER, LEFT, RIGHT
    Alignment: "CENTER"
    
    # Maximum width of the text before it wraps (integer)
    LineWidth: 200
```

### Supported Named Colors
The `BackgroundColor` setting supports the following named colors case-insensitively:
- `transparent` (fully invisible background)
- `black`, `dark_blue`, `dark_green`, `dark_aqua`, `dark_red`, `dark_purple`, `gold`, `gray`, `dark_gray`
- `blue`, `green`, `aqua`, `red`, `light_purple`, `yellow`, `white`

## Dependencies

- The main **Dimensions** plugin.
- A Spigot/Paper server running Minecraft 1.19.4 or newer (which introduced Display entities).
