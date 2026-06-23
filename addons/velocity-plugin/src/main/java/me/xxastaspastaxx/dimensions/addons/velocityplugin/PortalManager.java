package me.xxastaspastaxx.dimensions.addons.velocityplugin;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import java.util.HashMap;

public class PortalManager {

  // portal -> server
  private HashMap<String, String> portals = new HashMap<String, String>();
  private HashMap<String, HashMap<String, String>> overridenPortals =
      new HashMap<String, HashMap<String, String>>();
  private String fallbackServer;

  private DimensionsVelocityPlugin main;

  public PortalManager(
      DimensionsVelocityPlugin main,
      String fallbackServer,
      HashMap<String, String> portals,
      HashMap<String, HashMap<String, String>> overridenPortals) {
    this.main = main;
    this.fallbackServer = fallbackServer;
    this.portals = portals;
    this.overridenPortals = overridenPortals;
  }

  @Subscribe
  public void onPluginMessage(PluginMessageEvent event) {
    main.logger.debug("MESSAGE");
    main.logger.debug("Source: " + event.getSource().getClass().getName());
    if (!(event.getSource() instanceof ServerConnection)) {
      main.logger.debug(event.getSource().getClass().getName());
      return;
    }

    main.logger.debug("ID: " + event.getIdentifier().getId());
    if (!event.getIdentifier().getId().equalsIgnoreCase("dimensions:addons")) return;

    ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
    String subChannel = in.readUTF();
    ServerConnection serverSender = (ServerConnection) event.getSource();
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
      location.setServer(serverSender.getServerInfo().getName());

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
      RegisteredServer destServer = main.server.getServer(dest).get();
      if (destServer == null) return;

      serverSender
          .getPlayer()
          .createConnectionRequest(destServer)
          .connect()
          .whenComplete(
              (a, b) -> {
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

                destServer.sendPluginMessage(main.identifier, out.toByteArray());
              });

    } else if (subChannel.equalsIgnoreCase(
        "UnlinkPortal")) { // We forward this message to all servers
      String portal = in.readUTF(); // portal used
      BungeeLocation location =
          BungeeLocation.parseBungeeLocation(in.readUTF()); // Parse the location of the portal
      location.setServer(serverSender.getServerInfo().getName());

      for (RegisteredServer srv : main.server.getAllServers()) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("UnlinkPortal");
        out.writeUTF(portal); // portal used
        out.writeUTF(location.toString()); // send the from location to return player

        srv.sendPluginMessage(main.identifier, out.toByteArray());
      }
    } else if (subChannel.equalsIgnoreCase("LinkPortal")) { // We link a portal

      BungeeLocation portal1 =
          BungeeLocation.parseBungeeLocation(in.readUTF()); // Portal being linked

      BungeeLocation destionation = BungeeLocation.parseBungeeLocation(in.readUTF()); // Destination
      destionation.setServer(serverSender.getServerInfo().getName());

      ByteArrayDataOutput out = ByteStreams.newDataOutput();
      out.writeUTF("LinkPortal");
      out.writeUTF(portal1.toString());
      out.writeUTF(destionation.toString());

      main.server
          .getServer(portal1.getServer())
          .get()
          .sendPluginMessage(main.identifier, out.toByteArray());
    }
  }
}
