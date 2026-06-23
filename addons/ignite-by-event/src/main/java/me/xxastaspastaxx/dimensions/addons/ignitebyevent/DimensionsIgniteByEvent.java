package me.xxastaspastaxx.dimensions.addons.ignitebyevent;

import me.xxastaspastaxx.dimensions.Dimensions;
import me.xxastaspastaxx.dimensions.addons.DimensionsAddon;
import me.xxastaspastaxx.dimensions.addons.DimensionsAddonPriority;
import me.xxastaspastaxx.dimensions.customportal.CustomPortal;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class DimensionsIgniteByEvent extends DimensionsAddon {

  // private Plugin pl;

  private ListenerHandler handler;

  public DimensionsIgniteByEvent() {
    super(
        "DimensionsIgniteByEventAddon",
        "3.0.1",
        "Ignite portals without lighter items",
        DimensionsAddonPriority.NORMAL);
  }

  @Override
  public void onEnable(Dimensions pl) {
    // this.pl = pl;

    handler = new ListenerHandler(pl);
  }

  @Override
  public void registerPortal(YamlConfiguration portalConfig, CustomPortal portal) {

    if (portalConfig.getBoolean("Addon.IgniteByEvent.Enable", false)) return;

    for (String event :
        portalConfig.getConfigurationSection("Addon.IgniteByEvent.Events").getKeys(false)) {

      ConfigurationSection path =
          portalConfig.getConfigurationSection("Addon.IgniteByEvent.Events." + event);

      handler.add(
          portal,
          event,
          new EventRequirements(path.getString("ignite"), path.getStringList("ifs")));
    }

    return;
  }
}
