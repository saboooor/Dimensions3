package me.xxastaspastaxx.dimensions.addons.portalbleportals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import me.xxastaspastaxx.dimensions.Dimensions;
import me.xxastaspastaxx.dimensions.DimensionsUtils;
import me.xxastaspastaxx.dimensions.commands.DimensionsCommand;
import me.xxastaspastaxx.dimensions.customportal.CustomPortal;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnPortalCommand extends DimensionsCommand {

  DimensionsPortablePortals main;

  HashMap<String, String> defArgs = new HashMap<String, String>();

  public SpawnPortalCommand(
      String command,
      String args,
      String[] aliases,
      String description,
      String permission,
      boolean adminCommand,
      DimensionsPortablePortals main) {
    super(command, args, aliases, description, permission, adminCommand);
    this.main = main;

    defArgs.put("portal", "null");
    defArgs.put("player", "null");
    defArgs.put("width", "4");
    defArgs.put("height", "5");

    defArgs.put("public", "true");

    defArgs.put("time", "0"); // TimedPortals depend
    defArgs.put("timeEffects", ""); // TimedPortals depend

    defArgs.put("uses", "0"); // LimitedUses depend

    defArgs.put("cost", "1");
    defArgs.put("cooldown", "60");

    defArgs.put("location", "null");
    defArgs.put("world", "null");
  }

  @Override
  public void execute(CommandSender sender, String[] args) {

    @SuppressWarnings("unchecked")
    HashMap<String, String> argsMap = (HashMap<String, String>) defArgs.clone();

    for (int i = 0; i < args.length; i++) {
      String arg = args[i];
      if (arg.startsWith("-")) {
        argsMap.put(arg.substring(1), args[i + 1]);
      }
    }

    Player target = null;
    if (!argsMap.get("player").equals("null")) {
      target = Bukkit.getPlayer(argsMap.get("player"));
    } else if (sender instanceof Player) {
      target = (Player) sender;
    }

    if (target == null) {
      sender.sendMessage("§7[§cDimensions§7] §cCould not find the player to execute the command");
      return;
    }

    CustomPortal portal =
        Dimensions.getCustomPortalManager().getCustomPortal(argsMap.get("portal"));
    if (portal == null) {
      sender.sendMessage("§7[§cDimensions§7] §cCould not find the portal to execute the command");
      return;
    }

    List<Block> los = target.getLineOfSight(null, 5);
    Block targetBlock = null;
    for (Block block : los) {
      targetBlock = block;
      if (!DimensionsUtils.isAir(block)) {
        break;
      }
    }

    int time = 0;
    if ((Dimensions.getAddonManager().getAddonByName("DimensionsTimedPortalsAddon") != null
        && !argsMap.get("time").equals("0"))) {
      time = Integer.parseInt(argsMap.get("time")) * 1000;
    }

    int maxUses = 0;
    if ((Dimensions.getAddonManager().getAddonByName("DimensionsTimedPortalsAddon") != null
        && !argsMap.get("uses").equals("0"))) {
      maxUses = Integer.parseInt(argsMap.get("uses"));
    }

    Location forceLocation = null;
    if (!argsMap.get("location").equals("null")) {
      String[] spl = argsMap.get("location").split(",");
      forceLocation =
          new Location(
              target.getWorld(),
              Double.parseDouble(spl[0]),
              Double.parseDouble(spl[1]),
              Double.parseDouble(spl[2]));
    }

    String forceWorld = null;
    if (!argsMap.get("world").equals("null")) {
      forceWorld = argsMap.get("world");
    }

    main.buildPortal(
        portal,
        target,
        targetBlock.getLocation(),
        DimensionsUtils.isBlockFacezAxis(DimensionsUtils.yawToFace(target.getLocation().getYaw())),
        Integer.parseInt(argsMap.get("width")) / 2,
        Integer.parseInt(argsMap.get("height")) - 1,
        argsMap.get("public").equals("true"),
        time,
        Arrays.asList(argsMap.get("timeEffects").split(",")),
        maxUses,
        forceLocation,
        forceWorld);
  }
}
