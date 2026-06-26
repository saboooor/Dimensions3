package me.xxastaspastaxx.dimensions.addons.dimensionsbungee;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class DimensionsBungee extends Plugin {

  private static String CONFIG_VERSION = "1.0.0";

  @Override
  public void onEnable() {
    getProxy().registerChannel("dimensions:addons");

    try {
      makeConfig();
      Configuration configuration =
          ConfigurationProvider.getProvider(YamlConfiguration.class)
              .load(new File(getDataFolder(), "config.yml"));
      if (!configuration.getString("configVersion", "0").equals(CONFIG_VERSION)) {
        configuration.set("configVersion", CONFIG_VERSION);
        configuration.set("fallbackServer", "main");
        configuration.set("Portals", new ArrayList<String>());
      }

      ConfigurationProvider.getProvider(YamlConfiguration.class)
          .save(configuration, new File(getDataFolder(), "config.yml"));

      String fallbackServer = configuration.getString("fallbackServer");
      // PORTAL->SERVER
      // FROM->PORTAL->SERVER
      HashMap<String, String> map = new HashMap<String, String>();
      HashMap<String, HashMap<String, String>> map2 =
          new HashMap<String, HashMap<String, String>>();
      for (String str : configuration.getStringList("Portals")) {
        String[] spl = str.split("->");
        if (spl.length == 2) {
          map.put(spl[0], spl[1]);
        } else {
          if (!map2.containsKey(spl[0])) map2.put(spl[0], new HashMap<String, String>());
          map2.get(spl[0]).put(spl[1], spl[2]);
        }
      }

      getProxy()
          .getPluginManager()
          .registerListener(this, new PortalManager(this, fallbackServer, map, map2));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void onDisable() {
    // manager.getFileManager().save();
  }

  public void makeConfig() throws IOException {
    if (!getDataFolder().exists()) getDataFolder().mkdir();

    File configFile = new File(getDataFolder(), "config.yml");
    configFile.createNewFile();
  }
}
