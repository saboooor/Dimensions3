package me.xxastaspastaxx.dimensions.addons.unbreakable;

import java.util.ArrayList;
import java.util.List;
import me.xxastaspastaxx.dimensions.Dimensions;
import me.xxastaspastaxx.dimensions.addons.DimensionsAddon;
import me.xxastaspastaxx.dimensions.addons.DimensionsAddonPriority;
import me.xxastaspastaxx.dimensions.customportal.CustomPortal;
import me.xxastaspastaxx.dimensions.customportal.CustomPortalDestroyCause;
import me.xxastaspastaxx.dimensions.events.CustomPortalBreakEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class DimensionsUnbreakable extends DimensionsAddon implements Listener {

  // private Plugin pl;

  public DimensionsUnbreakable() {
    super(
        "DimensionsUnbreakableAddon",
        "3.0.2",
        "Unbreakable portals",
        DimensionsAddonPriority.NORMAL);
  }

  @Override
  public void onEnable(Dimensions pl) {
    // this.pl = pl;

    Bukkit.getPluginManager().registerEvents(this, pl);
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
  public void destroyPortal(CustomPortalBreakEvent e) {
    Object option = getOption(e.getCompletePortal(), "unbreakableList");
    if (option == null) return;
    @SuppressWarnings("unchecked")
    ArrayList<CustomPortalDestroyCause> reasons = (ArrayList<CustomPortalDestroyCause>) option;

    if (reasons.contains(e.getCause())) e.setCancelled(true);
  }

  @Override
  public void registerPortal(YamlConfiguration portalConfig, CustomPortal portal) {

    List<String> spl = portalConfig.getStringList("Addon.Unbreakable");

    if (spl.size() == 0) return;

    ArrayList<CustomPortalDestroyCause> list = new ArrayList<CustomPortalDestroyCause>();
    for (String str : spl) {
      list.add(CustomPortalDestroyCause.valueOf(str));
    }

    setOption(portal, "unbreakableList", list);

    return;
  }
}
