package me.xxastaspastaxx.dimensions.addons.proxy;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import me.xxastaspastaxx.dimensions.Dimensions;
import me.xxastaspastaxx.dimensions.DimensionsDebbuger;
import me.xxastaspastaxx.dimensions.addons.DimensionsAddon;
import me.xxastaspastaxx.dimensions.addons.DimensionsAddonPriority;
import me.xxastaspastaxx.dimensions.completePortal.CompletePortal;
import me.xxastaspastaxx.dimensions.completePortal.PortalGeometry;
import me.xxastaspastaxx.dimensions.customportal.CustomPortal;
import me.xxastaspastaxx.dimensions.customportal.CustomPortalIgniteCause;
import me.xxastaspastaxx.dimensions.events.CustomPortalBreakEvent;
import me.xxastaspastaxx.dimensions.events.CustomPortalIgniteEvent;
import me.xxastaspastaxx.dimensions.events.CustomPortalUseEvent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class DimensionsProxy extends DimensionsAddon implements PluginMessageListener, Listener {

  private Plugin pl;
  // private String serverName; //We need this because bungee needs the server's name as configured
  // in the bungee's config.yml
  private HashMap<UUID, CompletePortal> queue = new HashMap<UUID, CompletePortal>();

  public DimensionsProxy() {
    super("DimensionsProxyAddon", "4.0.0", "Bungee portals", DimensionsAddonPriority.NORMAL);
  }

  @Override
  public void onEnable(Dimensions pl) {
    this.pl = pl;

    Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(pl, "dimensions:addons");
    Bukkit.getServer().getMessenger().registerIncomingPluginChannel(pl, "dimensions:addons", this);

    Bukkit.getPluginManager().registerEvents(this, pl);
  }

  @Override
  public void onDisable() {
    Bukkit.getServer().getMessenger().unregisterOutgoingPluginChannel(pl, "dimensions:addons");
    Bukkit.getServer()
        .getMessenger()
        .unregisterIncomingPluginChannel(pl, "dimensions:addons", this);
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent e) {
    DimensionsDebbuger.DEBUG.print("joined" + e.getPlayer().getUniqueId());
    DimensionsDebbuger.DEBUG.print("In queue: " + queue.containsKey(e.getPlayer().getUniqueId()));
    CompletePortal portal = queue.remove(e.getPlayer().getUniqueId());
    DimensionsDebbuger.DEBUG.print(portal);
    if (portal != null) {
      handleTp(e.getPlayer(), portal);
    }
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void onPortalIgnite(CustomPortalIgniteEvent e) {
    if (e.getCause() == CustomPortalIgniteCause.LOAD_PORTAL
        && e.getCompletePortal().getTag("bungeeLink") != null)
      e.getCompletePortal().setLinkedPortal(e.getCompletePortal()); // bungee fix
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void onPortalDestroy(CustomPortalBreakEvent e) {
    if (!(e.getDestroyer() instanceof Player)) return;
    CompletePortal complete = e.getCompletePortal();
    CustomPortal portal = complete.getCustomPortal();
    if (getOption(complete, "bungeeServer") == null)
      return; // If the portal does not have the addon enable, we skip

    ByteArrayDataOutput out = ByteStreams.newDataOutput();
    out.writeUTF("UnlinkPortal");
    out.writeUTF(portal.getPortalId()); // portal name
    out.writeUTF(
        (new BungeeLocation(
                "fill", complete.getWorld().getName(), complete.getPortalGeometry().getCenter()))
            .toString());

    ((Player) e.getDestroyer()).sendPluginMessage(pl, "dimensions:addons", out.toByteArray());
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
  public void onPortalUse(CustomPortalUseEvent e) {
    if (!(e.getEntity() instanceof Player)) return;
    CompletePortal complete = e.getCompletePortal();
    if (getOption(complete, "bungeeServer") == null)
      return; // If the portal does not have the addon enable, we skip

    e.setDestinationPortal(complete);

    Object bungeeTemp = complete.getTag("bungeeTemp");
    if (bungeeTemp
        != null) { // If the portal has the bungeeTemp tag, it means that we just came from another
      // server, so we dont teleport the player, instead we try to calculate the
      // teleport location\

      DimensionsDebbuger.DEBUG.print("awawdf");
      Object bungeeForce = complete.getTag("bungeeForce");
      Location force = null;
      if (bungeeForce != null) {
        BungeeLocation temp = (BungeeLocation) bungeeForce;
        force = temp.toLocation(Bukkit.getWorld(temp.getWorld()));
        DimensionsDebbuger.DEBUG.print(force);
        try {
          complete.setLinkedPortal(
              Dimensions.getCompletePortalManager().getCompletePortal(force, true, true));
        } catch (NullPointerException e1) {
          DimensionsDebbuger.DEBUG.print("NOT FOUND");
        }
      }

      CompletePortal dest = complete.getDestinationPortal(true, force, complete.getWorld());
      if (dest.getTag("bungeeLink") == null) {
        dest.setTag("bungeeLink", bungeeTemp);

        // Link portals
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("LinkPortal");
        out.writeUTF(bungeeTemp.toString()); // Portal being linked
        out.writeUTF(
            (new BungeeLocation(
                    "fill", dest.getWorld().getName(), dest.getPortalGeometry().getCenter()))
                .toString()); // Destination portal

        ((Player) e.getEntity()).sendPluginMessage(pl, "dimensions:addons", out.toByteArray());
      }
      DimensionsDebbuger.DEBUG.print(dest.getCenter());
      complete.setTag("bungeeForce", null);
      // dest.setTag("bungeeTemp", true);
      //			dest.setLinkedPortal(dest);
      //
      //	DimensionsDebbuger.DEBUG.print(Dimensions.getCompletePortalManager().getCompletePortals().size());
      e.setDestinationPortal(dest);
    }
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void postPortalUse(CustomPortalUseEvent e) {
    if (!(e.getEntity() instanceof Player)) return;
    CompletePortal complete = e.getCompletePortal();
    CustomPortal portal = complete.getCustomPortal();
    if (getOption(complete, "bungeeServer") == null)
      return; // If the portal does not have the addon enable, we skip

    Object bungeeTemp = complete.getTag("bungeeTemp");
    if (bungeeTemp
        != null) { // If the portal has the bungeeTemp tag, it means that we just came from another
      // server, so we dont teleport the player
      complete.setTag("bungeeTemp", null);
      return;
    }

    Player player = (Player) e.getEntity();

    PotionEffect savedEffect =
        player.getActivePotionEffects().stream()
            .filter(effect -> effect.getType() == PotionEffectType.BLINDNESS)
            .findFirst()
            .orElse(PotionEffectType.BLINDNESS.createEffect(5, 255));
    player.addPotionEffect(savedEffect);
    // If the portal has a tag telling it in what server to travel, travel there, if not, go to the
    // destination server.
    BungeeLocation link = null;
    Object destOBJ2 = complete.getTag("bungeeLink");
    if (destOBJ2 != null) link = BungeeLocation.parseBungeeLocation((String) destOBJ2);

    // Tell the bungee plugin to figure the destination server and send the player at the portal
    // position.
    ByteArrayDataOutput out = ByteStreams.newDataOutput();
    out.writeUTF("UsePortal");
    PortalGeometry v = complete.getPortalGeometry();
    out.writeUTF(player.getUniqueId().toString()); // player uuid
    out.writeUTF(portal.getPortalId()); // portal name
    out.writeUTF(link + ""); // Overwrite destination server
    out.writeUTF(
        (new BungeeLocation("fill", complete.getWorld().getName(), v.getCenter())).toString());

    // We send the portal geometry data
    out.writeUTF(v.getMin().toString());
    out.writeUTF(v.getMax().toString());
    // portal geometry data ^^

    (player).sendPluginMessage(pl, "dimensions:addons", out.toByteArray());

    // complete.setLinkedPortal(complete);

    PotionEffectType.BLINDNESS.createEffect(5, 1).apply(player);
  }

  @Override
  public void onPluginMessageReceived(String channel, Player player, byte[] message) {
    DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));
    try {
      String action = in.readUTF();
      if (action.equals("UsePortalSpigot")) {
        // We just
        UUID uuid = UUID.fromString(in.readUTF());
        Player p = Bukkit.getPlayer(uuid);

        DimensionsDebbuger.DEBUG.print("GOT MESSAGE FROM ", uuid, "through", player.getUniqueId());
        CustomPortal portal = Dimensions.getCustomPortalManager().getCustomPortal(in.readUTF());
        String forceStr = in.readUTF();
        BungeeLocation force = null;
        if (!forceStr.equals("null")) {
          force = BungeeLocation.parseBungeeLocation(forceStr);
        }

        BungeeLocation loc = BungeeLocation.parseBungeeLocation(in.readUTF());

        // Portal geometry
        String[] spl = in.readUTF().split(",");
        Vector min =
            new Vector(
                Double.parseDouble(spl[0]), Double.parseDouble(spl[1]), Double.parseDouble(spl[2]));

        spl = in.readUTF().split(",");
        Vector max =
            new Vector(
                Double.parseDouble(spl[0]), Double.parseDouble(spl[1]), Double.parseDouble(spl[2]));

        PortalGeometry geom = PortalGeometry.getPortalGeometry(portal).createGeometry(min, max);
        // Portal geometry

        // DimensionsDebbuger.debug("Player "+player.getName()+" was sent to ["+loc+"]",
        // DimensionsDebbuger.VERY_HIGH);
        World tpWorld =
            Bukkit.getWorld(
                force == null ? ((String) getOption(portal, "bungeeTPWorld")) : force.getWorld());

        CompletePortal tempCompl = new CompletePortal(portal, tpWorld, geom);
        tempCompl.setTag("bungeeTemp", loc.toString());
        tempCompl.setTag("bungeeForce", force);

        if (p != null && p.isOnline()) {
          handleTp(p, tempCompl);
        } else {
          queue.put(uuid, tempCompl);
        }
      } else if (action.equals("UnlinkPortal")) {
        CustomPortal portal = Dimensions.getCustomPortalManager().getCustomPortal(in.readUTF());
        String loc = in.readUTF();
        for (CompletePortal compl :
            Dimensions.getCompletePortalManager().getCompletePortals(portal)) {
          Object obj = compl.getTag("bungeeLink");
          if (obj != null && loc.equals((String) obj)) {
            compl.setTag("bungeeLink", null);
            compl.unlinkPortal();
            DimensionsDebbuger.DEBUG.print("UNLINK:", compl.getCenter());
          }
        }
      } else if (action.equals("LinkPortal")) {
        DimensionsDebbuger.DEBUG.print("Attemmping to link ");
        BungeeLocation loc =
            BungeeLocation.parseBungeeLocation(in.readUTF()); // portal being linked
        BungeeLocation destination =
            BungeeLocation.parseBungeeLocation(in.readUTF()); // destination

        DimensionsDebbuger.DEBUG.print("Attemmping to link ", loc, destination);
        CompletePortal complete =
            Dimensions.getCompletePortalManager()
                .getCompletePortal(loc.toLocation(Bukkit.getWorld(loc.getWorld())), true, true);
        complete.setTag("bungeeLink", destination.toString());
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void handleTp(Player player, CompletePortal portal) {
    DimensionsDebbuger.DEBUG.print("HANDLING PLAYER");
    GameMode temp = player.getGameMode();
    List<PotionEffect> savedEffects =
        player.getActivePotionEffects().stream()
            .filter(effect -> effect.getType() == PotionEffectType.BLINDNESS)
            .toList();
    player.removePotionEffect(PotionEffectType.BLINDNESS);
    PotionEffectType.BLINDNESS.createEffect(Integer.MAX_VALUE, 1).apply(player);
    player.setGameMode(GameMode.SPECTATOR);
    player.teleport(new Location(portal.getWorld(), 0, 0, 0));
    try {
      portal.handleEntity(player);
    } catch (Exception e1) {
      e1.printStackTrace();
    }
    player.setGameMode(temp);
    player.removePotionEffect(PotionEffectType.BLINDNESS);
    if (!savedEffects.isEmpty()) player.addPotionEffects(savedEffects);
  }

  @Override
  public void registerPortal(YamlConfiguration portalConfig, CustomPortal portal) {

    if (!portalConfig.getBoolean("Addon.Bungee.Enable", false)) return;

    setOption(portal, "bungeeServer", true);
    setOption(portal, "bungeeTPWorld", portalConfig.getString("Addon.Bungee.DestWorld", "world"));

    return;
  }
}
