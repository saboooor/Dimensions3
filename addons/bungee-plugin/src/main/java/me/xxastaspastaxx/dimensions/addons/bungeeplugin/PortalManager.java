package me.xxastaspastaxx.dimensions.addons.bungeeplugin;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import java.util.HashMap;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PortalManager implements Listener {

  // portal -> server
  private HashMap<String, String> portals = new HashMap<String, String>();
  private HashMap<String, HashMap<String, String>> overridenPortals =
      new HashMap<String, HashMap<String, String>>();
  private String fallbackServer;

  private DimensionsBungeePlugin main;

  public PortalManager(
      DimensionsBungeePlugin main,
      String fallbackServer,
      HashMap<String, String> portals,
      HashMap<String, HashMap<String, String>> overridenPortals) {
    this.main = main;
    this.fallbackServer = fallbackServer;
    this.portals = portals;
    this.overridenPortals = overridenPortals;
  }

  @EventHandler
  public void onPluginMessage(PluginMessageEvent event) {
    if (!event.getTag().equalsIgnoreCase("dimensions:addons")) return;

    ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
    String subChannel = in.readUTF();
    ProxiedPlayer sender = (ProxiedPlayer) event.getReceiver();
    if (subChannel.equalsIgnoreCase(
        "UsePortal")) { // If we get this, then we must figure out where to what server we must send
      // the server at the location that we received

      String uuid = in.readUTF();
      String portal = in.readUTF(); // portal used
      String linkedStr = in.readUTF();
      BungeeLocation linked = null;
      if (!linkedStr.equals("null")) {
        linked = BungeeLocation.parseBungeeLocation(linkedStr);
      }
      BungeeLocation location =
          BungeeLocation.parseBungeeLocation(in.readUTF()); // Parse the location of the portal
      location.setServer(sender.getServer().getInfo().getName());

      String dest = "";
      if (linked
          == null) { // If we dont have an overwritten destination we trying to figure out where to
        // send the player

        if (overridenPortals.containsKey(location.getServer())) {
          dest = overridenPortals.get(location.getServer()).get(portal);
        } else {
          if (location.getServer().equals(portals.get(portal))) {
            dest = fallbackServer; // If we are in the destination server, we send the player to the
            // fallback server
          } else {
            dest =
                portals.get(
                    portal); // If we are not in the destination server, we send the player there
          }
        }

      } else {
        dest = linked.getServer();
      }

      // We get connection to the destination server
      ServerInfo destServer = main.getProxy().getServerInfo(dest);
      if (destServer == null) return;

      // We connect the player to that server and we send the location and the portal that was used
      sender.connect(
          destServer,
          (result, error) -> {
            if (result) {
              ByteArrayDataOutput out = ByteStreams.newDataOutput();
              out.writeUTF("UsePortalSpigot");
              out.writeUTF(uuid); // player uuid
              out.writeUTF(portal); // portal used
              out.writeUTF(linkedStr); // linked portal
              out.writeUTF(location.toString()); // send the from location to return player

              // PortalGeometry
              out.writeUTF(in.readUTF());
              out.writeUTF(in.readUTF());
              // PortalGeometry

              destServer.sendData("dimensions:addons", out.toByteArray(), true);
            }
          });

    } else if (subChannel.equalsIgnoreCase(
        "UnlinkPortal")) { // We forward this message to all servers
      String portal = in.readUTF(); // portal used
      BungeeLocation location =
          BungeeLocation.parseBungeeLocation(in.readUTF()); // Parse the location of the portal
      location.setServer(sender.getServer().getInfo().getName());

      for (ServerInfo srv : main.getProxy().getServers().values()) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("UnlinkPortal");
        out.writeUTF(portal); // portal used
        out.writeUTF(location.toString()); // send the from location to return player

        srv.sendData("dimensions:addons", out.toByteArray(), true);
      }
    } else if (subChannel.equalsIgnoreCase("LinkPortal")) { // We link a portal

      BungeeLocation portal1 =
          BungeeLocation.parseBungeeLocation(in.readUTF()); // Portal being linked

      BungeeLocation destionation = BungeeLocation.parseBungeeLocation(in.readUTF()); // Destination
      destionation.setServer(sender.getServer().getInfo().getName());

      ByteArrayDataOutput out = ByteStreams.newDataOutput();
      out.writeUTF("LinkPortal");
      out.writeUTF(portal1.toString());
      out.writeUTF(destionation.toString());

      main.getProxy()
          .getServerInfo(portal1.getServer())
          .sendData("dimensions:addons", out.toByteArray(), true);
    }
  }
}
