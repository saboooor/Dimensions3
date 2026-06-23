package me.xxastaspastaxx.dimensions.addons.randomlocation;

import me.xxastaspastaxx.dimensions.Dimensions;
import me.xxastaspastaxx.dimensions.DimensionsUtils;
import me.xxastaspastaxx.dimensions.addons.DimensionsAddon;
import me.xxastaspastaxx.dimensions.addons.DimensionsAddonPriority;
import me.xxastaspastaxx.dimensions.completePortal.CompletePortal;
import me.xxastaspastaxx.dimensions.customportal.CustomPortal;
import me.xxastaspastaxx.dimensions.events.CustomPortalUseEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class DimensionsRandomLocation extends DimensionsAddon implements Listener {

  // private Plugin pl;

  public DimensionsRandomLocation() {
    super(
        "DimensionsRandomLocationAddon",
        "3.0.7",
        "Teleport players to a random location",
        DimensionsAddonPriority.NORMAL);
  }

  @Override
  public void onEnable(Dimensions pl) {
    // this.pl = pl;

    Bukkit.getPluginManager().registerEvents(this, pl);
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
  public void onPortalUse(CustomPortalUseEvent e) {

    CompletePortal complete = e.getCompletePortal();
    Object rangeOBJ = getOption(complete, "randomTPRange");
    if (rangeOBJ == null) return;

    if (getOption(complete, "randomTPAllowLink") == null) {
      e.getCompletePortal().unlinkPortal();
    }

    Location loc = complete.getCenter();
    int range = (int) rangeOBJ;

    Location teleportLocation =
        new Location(
            null,
            loc.getX() + DimensionsUtils.getRandom(-range, range),
            loc.getY(),
            loc.getZ() + DimensionsUtils.getRandom(-range, range));

    CustomPortal customPortal = complete.getCustomPortal();

    if (customPortal.canBuildExitPortal()) {
      e.setDestinationPortal(complete.getDestinationPortal(true, teleportLocation, null));
    } else {
      Location destLoc = complete.getDestinationLocation(teleportLocation, null);
      CompletePortal destination =
          new CompletePortal(
              customPortal,
              destLoc.getWorld(),
              complete.getPortalGeometry().createGeometry(destLoc.toVector(), destLoc.toVector()));

      Block b = destination.getCenter().getBlock().getRelative(BlockFace.DOWN);
      if (!b.getType().isSolid()) b.setType(customPortal.getOutsideMaterial());
    }
  }

  @Override
  public void registerPortal(YamlConfiguration portalConfig, CustomPortal portal) {

    int range = portalConfig.getInt("Addon.RandomLocation.Range", 0);
    if (range == 0) return;
    setOption(portal, "randomTPRange", range);
    if (portalConfig.getBoolean("Addon.RandomLocation.AllowLinkTp", false))
      setOption(portal, "randomTPAllowLink", true);

    return;
  }
}
