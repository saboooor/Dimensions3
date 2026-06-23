package me.xxastaspastaxx.dimensions.addons.stylishportals;

import java.util.List;
import me.xxastaspastaxx.dimensions.Dimensions;
import me.xxastaspastaxx.dimensions.addons.DimensionsAddon;
import me.xxastaspastaxx.dimensions.addons.DimensionsAddonPriority;
import me.xxastaspastaxx.dimensions.addons.stylishportals.style.FrameStyle;
import me.xxastaspastaxx.dimensions.completePortal.PortalGeometry;
import me.xxastaspastaxx.dimensions.customportal.CustomPortal;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;

public class DimensionsStylishPortals extends DimensionsAddon implements Listener {

  // private Plugin pl;

  public static DimensionsStylishPortals instance;

  public DimensionsStylishPortals() {
    super(
        "DimensionsStylishPortalsAddon",
        "3.0.5",
        "Give style to your portals",
        DimensionsAddonPriority.NORMAL);
  }

  @Override
  public void onEnable(Dimensions pl) {
    // this.pl = pl;

    DimensionsStylishPortals.instance = this;

    Dimensions.getCommandManager()
        .registerCommand(
            new StylishPortalCreateCommand(
                "blockData",
                "",
                new String[0],
                "Get the string block data of the block you are looking at",
                "",
                true,
                this));

    Bukkit.getPluginManager().registerEvents(this, pl);
  }

  @Override
  public void registerPortal(YamlConfiguration portalConfig, CustomPortal portal) {

    List<String> frameStyle = portalConfig.getStringList("Addon.StylishPortal.FrameStyle");
    // List<String> insideStyle = portalConfig.getStringList("Addon.StylishPortal.InsideStyle");
    if (frameStyle.size() == 0) return;

    FrameStyle style = new FrameStyle(frameStyle);
    setOption(portal, "frameStyle", style);
    PortalGeometry.setCustomGeometry(portal, new CustomPortalGeometry(null, null, style));
    // options.put(portal, new AddonOptions(frameStyle, disabledWorlds, portalDiameters[3],
    // portalDiameters[2], worldHeight[0],
    // worldHeight[1],portalDiameters[1],portalDiameters[0],portalConfig.getStringList("Entities.Spawning.List").size()!=0,buildExitPortal));

    return;
  }
}
