package me.xxastaspastaxx.dimensions.addons.customworlds;

import java.util.Random;
import me.xxastaspastaxx.dimensions.Dimensions;
import me.xxastaspastaxx.dimensions.DimensionsScheduler;
import me.xxastaspastaxx.dimensions.addons.DimensionsAddon;
import me.xxastaspastaxx.dimensions.addons.DimensionsAddonPriority;
import me.xxastaspastaxx.dimensions.addons.customworlds.generators.WorldGenerator;
import me.xxastaspastaxx.dimensions.customportal.CustomPortal;
import org.bukkit.Bukkit;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class DimensionsCustomWorlds extends DimensionsAddon implements Listener {

  private Plugin pl;

  public DimensionsCustomWorlds() {
    super(
        "DimensionsCustomWorldsAddon",
        "3.0.0",
        "Custom world generator",
        DimensionsAddonPriority.HIGHEST);
  }

  @Override
  public void onEnable(Dimensions pl) {
    // this.pl = pl;

    Bukkit.getPluginManager().registerEvents(this, pl);
  }

  @Override
  public void registerPortal(YamlConfiguration portalConfig, CustomPortal portal) {

    DimensionsScheduler.runDelayed(
        pl,
        () -> {
          // code
        },
        20);

    String spl = portalConfig.getString("Addon.WorldGenerator.Name", "none");

    if (spl.equalsIgnoreCase("none")) return;

    WorldGenerator generator = WorldGenerator.valueOf(spl);

    Bukkit.createWorld(
        new WorldCreator(portal.getWorldName())
            .generator(generator)
            .environment(
                Environment.valueOf(
                    portalConfig.getString("Addon.WorldGenerator.Environment", "normal")))
            .seed(portalConfig.getLong("Addon.WorldGenerator.Seed", (new Random()).nextLong())));
  }
}
