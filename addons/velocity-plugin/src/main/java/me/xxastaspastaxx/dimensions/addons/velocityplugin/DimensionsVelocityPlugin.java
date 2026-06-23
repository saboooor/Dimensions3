package me.xxastaspastaxx.dimensions.addons.velocityplugin;

import com.google.inject.Inject;
import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import org.slf4j.Logger;

@Plugin(
    id = "dimensionsvelocity",
    name = "Dimensions Velocity",
    version = "1.0.3",
    authors = {"astaspasta"})
public class DimensionsVelocityPlugin {

  private static String CONFIG_VERSION = "1.0.0";

  public final ProxyServer server;
  public final Logger logger;
  public final Path dataDirectory;
  public ChannelIdentifier identifier;

  @Inject
  public DimensionsVelocityPlugin(
      ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
    this.server = server;
    this.logger = logger;
    this.dataDirectory = dataDirectory;
    this.identifier = MinecraftChannelIdentifier.create("dimensions", "addons");
  }

  @Subscribe
  public void onProxyInitialization(ProxyInitializeEvent event) {
    // "dimensions:addons"

    logger.debug("registering...");
    server.getChannelRegistrar().register(identifier);

    try {
      makeConfig();

      Toml configuration = new Toml().read(new File(getDataFolder() + "/config.toml"));
      if (!configuration.getString("config-version", "0").equals(CONFIG_VERSION)) {
        logger.error("There is an error while attempting to read the config file");
      }
      String fallbackServer = configuration.getString("fallbackServer");
      // PORTAL->SERVER
      // FROM->PORTAL->SERVER
      HashMap<String, String> map = new HashMap<String, String>();
      HashMap<String, HashMap<String, String>> map2 =
          new HashMap<String, HashMap<String, String>>();
      for (Object str : configuration.getList("Portals")) {
        String[] spl = ((String) str).split("->");
        if (spl.length == 2) {
          map.put(spl[0], spl[1]);
        } else {
          if (!map2.containsKey(spl[0])) map2.put(spl[0], new HashMap<String, String>());
          map2.get(spl[0]).put(spl[1], spl[2]);
        }
      }

      server.getEventManager().register(this, new PortalManager(this, fallbackServer, map, map2));
      // getProxy().getPluginManager().registerListener(this, new PortalManager(this,
      // fallbackServer, map, map2));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /*
    @Override
    public void onDisable() {
  //manager.getFileManager().save();
    }*/

  public void makeConfig() throws IOException {
    if (!getDataFolder().exists()) getDataFolder().mkdir();

    File configFile = new File(getDataFolder(), "config.toml");
    configFile.createNewFile();
  }

  public File getDataFolder() {
    return dataDirectory.toFile();
  }
}
