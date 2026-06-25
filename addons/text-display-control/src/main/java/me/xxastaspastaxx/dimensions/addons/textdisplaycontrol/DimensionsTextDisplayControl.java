package me.xxastaspastaxx.dimensions.addons.textdisplaycontrol;

import me.xxastaspastaxx.dimensions.addons.DimensionsAddon;
import me.xxastaspastaxx.dimensions.addons.DimensionsAddonPriority;
import me.xxastaspastaxx.dimensions.customportal.CustomPortal;
import org.bukkit.configuration.file.YamlConfiguration;

public class DimensionsTextDisplayControl extends DimensionsAddon {

  public DimensionsTextDisplayControl() {
    super(
        "DimensionsTextDisplayControlAddon",
        "4.0.0",
        "Control granular properties of portal text displays",
        DimensionsAddonPriority.NORMAL);
  }

  @Override
  public void registerPortal(YamlConfiguration portalConfig, CustomPortal portal) {
    // 1. Background Color
    if (portalConfig.contains("Addon.TextDisplayControl.BackgroundColor")) {
      Object colorObj = portalConfig.get("Addon.TextDisplayControl.BackgroundColor");
      if (colorObj instanceof Integer) {
        setOption(portal, "text_display_background_color", colorObj);
      } else if (colorObj instanceof String) {
        Integer parsedColor = parseColor((String) colorObj);
        if (parsedColor != null) {
          setOption(portal, "text_display_background_color", parsedColor);
        }
      }
    }

    // 2. Background Opacity
    if (portalConfig.contains("Addon.TextDisplayControl.BackgroundOpacity")) {
      Object opObj = portalConfig.get("Addon.TextDisplayControl.BackgroundOpacity");
      if (opObj instanceof Number) {
        setOption(portal, "text_display_background_opacity", opObj);
      }
    }

    // 3. Sprite / Text Opacity
    if (portalConfig.contains("Addon.TextDisplayControl.SpriteOpacity")) {
      Object opObj = portalConfig.get("Addon.TextDisplayControl.SpriteOpacity");
      if (opObj instanceof Number) {
        setOption(portal, "text_display_sprite_opacity", opObj);
      }
    } else if (portalConfig.contains("Addon.TextDisplayControl.TextOpacity")) {
      Object opObj = portalConfig.get("Addon.TextDisplayControl.TextOpacity");
      if (opObj instanceof Number) {
        setOption(portal, "text_display_sprite_opacity", opObj);
      }
    }

    // 4. Shadow
    if (portalConfig.contains("Addon.TextDisplayControl.Shadow")) {
      boolean val = portalConfig.getBoolean("Addon.TextDisplayControl.Shadow");
      setOption(portal, "text_display_shadow", val);
    }

    // 5. See Through
    if (portalConfig.contains("Addon.TextDisplayControl.SeeThrough")) {
      boolean val = portalConfig.getBoolean("Addon.TextDisplayControl.SeeThrough");
      setOption(portal, "text_display_see_through", val);
    }

    // 6. Default Background
    if (portalConfig.contains("Addon.TextDisplayControl.UseDefaultBackground")) {
      boolean val = portalConfig.getBoolean("Addon.TextDisplayControl.UseDefaultBackground");
      setOption(portal, "text_display_default_background", val);
    }

    // 7. Alignment
    if (portalConfig.contains("Addon.TextDisplayControl.Alignment")) {
      String val = portalConfig.getString("Addon.TextDisplayControl.Alignment");
      if (val != null) {
        setOption(portal, "text_display_alignment", val);
      }
    }

    // 8. Line Width
    if (portalConfig.contains("Addon.TextDisplayControl.LineWidth")) {
      int val = portalConfig.getInt("Addon.TextDisplayControl.LineWidth");
      setOption(portal, "text_display_line_width", val);
    }
  }

  private Integer parseColor(String colorStr) {
    if (colorStr == null) return null;
    colorStr = colorStr.trim();
    if (colorStr.equalsIgnoreCase("transparent")) {
      return 0;
    }

    // Hex colors
    if (colorStr.startsWith("#") || colorStr.startsWith("0x")) {
      String hex = colorStr.startsWith("#") ? colorStr.substring(1) : colorStr.substring(2);
      try {
        long val = Long.parseLong(hex, 16);
        if (hex.length() == 6) {
          // RRGGBB, default alpha to FF (fully opaque)
          val |= 0xFF000000L;
        }
        return (int) val;
      } catch (NumberFormatException e) {
        return null;
      }
    }

    // Named colors mapping
    String colorUpper = colorStr.toUpperCase().replace(" ", "_");
    switch (colorUpper) {
      case "BLACK":
        return 0xFF000000;
      case "DARK_BLUE":
        return 0xFF0000AA;
      case "DARK_GREEN":
        return 0xFF00AA00;
      case "DARK_AQUA":
        return 0xFF00AAAA;
      case "DARK_RED":
        return 0xFFAA0000;
      case "DARK_PURPLE":
        return 0xFFAA00AA;
      case "GOLD":
        return 0xFFFFAA00;
      case "GRAY":
        return 0xFFAAAAAA;
      case "DARK_GRAY":
        return 0xFF555555;
      case "BLUE":
        return 0xFF5555FF;
      case "GREEN":
        return 0xFF55FF55;
      case "AQUA":
        return 0xFF55FFFF;
      case "RED":
        return 0xFFFF5555;
      case "LIGHT_PURPLE":
        return 0xFFFF55FF;
      case "YELLOW":
        return 0xFFFFFF55;
      case "WHITE":
        return 0xFFFFFFFF;
      default:
        // Try parsing as raw decimal integer
        try {
          return Integer.parseInt(colorStr);
        } catch (NumberFormatException e) {
          return null;
        }
    }
  }
}
