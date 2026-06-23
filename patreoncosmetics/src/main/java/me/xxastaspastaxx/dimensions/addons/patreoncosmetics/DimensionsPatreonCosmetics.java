package me.xxastaspastaxx.dimensions.addons.patreoncosmetics;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.UUID;
import me.xxastaspastaxx.dimensions.Dimensions;
import me.xxastaspastaxx.dimensions.addons.DimensionsAddon;
import me.xxastaspastaxx.dimensions.addons.DimensionsAddonPriority;
import me.xxastaspastaxx.dimensions.addons.patreoncosmetics.cosmetics.CosmeticEffect;
import me.xxastaspastaxx.dimensions.completePortal.CompletePortal;
import me.xxastaspastaxx.dimensions.events.CustomPortalBreakEvent;
import me.xxastaspastaxx.dimensions.events.CustomPortalIgniteEvent;
import me.xxastaspastaxx.dimensions.events.CustomPortalUseEvent;
import me.xxastaspastaxx.dimensions.settings.DimensionsSettings;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class DimensionsPatreonCosmetics extends DimensionsAddon implements Listener {

  private Dimensions pl;
  private HashMap<CompletePortal, Integer> tasks = new HashMap<CompletePortal, Integer>();
  private HashMap<UUID, HashMap<String, CosmeticEffect>> users =
      new HashMap<UUID, HashMap<String, CosmeticEffect>>();
  private HashMap<UUID, Long> lastUpdate = new HashMap<UUID, Long>();

  private UUID localUUID;
  private HashMap<String, CosmeticEffect> localCosmeticsMap;

  public DimensionsPatreonCosmetics() {
    super("PatreonCosmetics", "1.0.0", "Cosmetics for Patreons", DimensionsAddonPriority.NORMAL);
  }

  @Override
  public boolean onLoad(Dimensions pl) {
    this.pl = pl;
    return DimensionsSettings.enablePatreonCosmetics;
  }

  @Override
  public void onEnable(Dimensions pl) {
    this.pl = pl;

    for (Player p : Bukkit.getOnlinePlayers()) {
      verifyPlayer(p);
    }

    String tmp = DimensionsSettings.getConfig().getString("CosmeticsAddon.PlayerUUID", "");
    if (!tmp.equals("")) localUUID = UUID.fromString((String) tmp);

    localCosmeticsMap = new HashMap<String, CosmeticEffect>();
    localCosmeticsMap.put(
        "postIgnitePortal",
        CosmeticEffect.valueOf(
            DimensionsSettings.getConfig().getString("CosmeticsAddon.IgnitePortal", "NOTHING")));
    localCosmeticsMap.put(
        "postDestroyPortal",
        CosmeticEffect.valueOf(
            DimensionsSettings.getConfig().getString("CosmeticsAddon.DestroyPortal", "NOTHING")));
    localCosmeticsMap.put(
        "postUsePortal",
        CosmeticEffect.valueOf(
            DimensionsSettings.getConfig().getString("CosmeticsAddon.UsePortal", "NOTHING")));
    localCosmeticsMap.put(
        "onPortalTick",
        CosmeticEffect.valueOf(
            DimensionsSettings.getConfig().getString("CosmeticsAddon.PortalTick", "NOTHING")));

    Dimensions.getCommandManager()
        .registerCommand(
            new PatreonCommand(
                "patreon",
                "[player]",
                new String[0],
                "Check your or a player's patreon status",
                "none",
                false,
                this));
    Dimensions.getCommandManager()
        .registerCommand(
            new PatreonDisableCommand(
                "disablePatreon",
                "",
                new String[0],
                "Disable your patreon effects for this session",
                "none",
                false,
                this));

    Bukkit.getServer().getPluginManager().registerEvents(this, pl);
  }

  @Override
  public void onDisable() {
    tasks.values().stream().forEach(id -> Bukkit.getScheduler().cancelTask(id));
  }

  public HashMap<UUID, HashMap<String, CosmeticEffect>> getUsers() {
    return users;
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void postIgnitePortal(CustomPortalIgniteEvent e) {

    CompletePortal complete = e.getCompletePortal();
    tasks.put(
        complete,
        Bukkit.getScheduler()
            .scheduleSyncRepeatingTask(
                pl,
                new Runnable() {

                  @Override
                  public void run() {

                    for (Entity entity :
                        complete.getWorld().getNearbyEntities(complete.getCenter(), 16, 16, 16)) {
                      if (users.containsKey(entity.getUniqueId())) {
                        users
                            .get(entity.getUniqueId())
                            .get("onPortalTick")
                            .play(complete, (Player) entity);
                        break;
                      }
                    }
                  }
                },
                0,
                20));

    if (!(e.getEntity() instanceof Player)) return;
    Player p = (Player) e.getEntity();
    if (users.containsKey(p.getUniqueId())) {
      users.get(p.getUniqueId()).get("postIgnitePortal").play(e.getCompletePortal(), p);
    }
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void postDestroyPortal(CustomPortalBreakEvent e) {
    if (tasks.containsKey(e.getCompletePortal()))
      Bukkit.getScheduler().cancelTask(tasks.get(e.getCompletePortal()));

    if (!(e.getDestroyer() instanceof Player)) return;
    Player p = (Player) e.getDestroyer();
    if (users.containsKey(p.getUniqueId())) {
      users.get(p.getUniqueId()).get("postDestroyPortal").play(e.getCompletePortal(), p);
    }
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void postUsePortal(CustomPortalUseEvent e) {
    if (!(e.getEntity() instanceof Player)) return;
    Player p = (Player) e.getEntity();
    if (users.containsKey(p.getUniqueId())) {
      users.get(p.getUniqueId()).get("postUsePortal").play(e.getCompletePortal(), p);
    }
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent e) {

    verifyPlayer(e.getPlayer());
  }

  private void verifyPlayer(Player player) {
    if (lastUpdate.containsKey(player.getUniqueId())
        && lastUpdate.get(player.getUniqueId()) >= System.currentTimeMillis()) {
      return;
    }

    lastUpdate.put(player.getUniqueId(), System.currentTimeMillis() + 1800000);

    if (player.getUniqueId().equals(localUUID)) {
      users.put(player.getUniqueId(), localCosmeticsMap);
      return;
    }

    Bukkit.getScheduler()
        .runTaskAsynchronously(
            pl,
            new Runnable() {

              @Override
              public void run() {
                try {
                  URL url = URI.create("https://astaspasta.alwaysdata.net/linkPatreon.php").toURL();
                  URLConnection con = url.openConnection();

                  HttpURLConnection http = (HttpURLConnection) con;
                  http.setRequestMethod("POST");
                  http.setDoOutput(true);

                  String uuid = player.getUniqueId().toString().replace("-", "");
                  Map<String, String> arguments = new HashMap<>();
                  arguments.put("verifySimple", uuid);
                  StringJoiner sj = new StringJoiner("&");
                  for (Map.Entry<String, String> entry : arguments.entrySet())
                    sj.add(
                        URLEncoder.encode(entry.getKey(), "UTF-8")
                            + "="
                            + URLEncoder.encode(entry.getValue(), "UTF-8"));
                  byte[] out = sj.toString().getBytes(StandardCharsets.UTF_8);
                  int length = out.length;

                  http.setFixedLengthStreamingMode(length);
                  http.setRequestProperty(
                      "Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                  http.connect();
                  try (OutputStream os = http.getOutputStream()) {
                    os.write(out);
                  }

                  BufferedReader reader =
                      new BufferedReader(new InputStreamReader(http.getInputStream()));

                  BufferedReader in =
                      new BufferedReader(
                          new InputStreamReader(
                              URI.create(
                                      "https://astaspasta.alwaysdata.net/api/userData.php?ingameCosmetics="
                                          + uuid)
                                  .toURL()
                                  .openStream(),
                              "UTF-8"));

                  Gson gson = new Gson();

                  try {
                    HashMap<String, String> mapString =
                        gson.fromJson(
                            in.readLine(), new TypeToken<HashMap<String, String>>() {}.getType());
                    HashMap<String, CosmeticEffect> map = new HashMap<String, CosmeticEffect>();
                    map.put(
                        "postIgnitePortal",
                        CosmeticEffect.valueOf(mapString.get("postIgnitePortal")));
                    map.put(
                        "postDestroyPortal",
                        CosmeticEffect.valueOf(mapString.get("postDestroyPortal")));
                    map.put(
                        "postUsePortal", CosmeticEffect.valueOf(mapString.get("postUsePortal")));
                    map.put("onPortalTick", CosmeticEffect.valueOf(mapString.get("onPortalTick")));

                    String line = reader.readLine();
                    if (line.contains("premium=1")) users.put(player.getUniqueId(), map);
                  } catch (Exception e) {
                    e.printStackTrace();
                  }

                } catch (IOException e1) {
                  e1.printStackTrace();
                }
              }
            });
  }
}
