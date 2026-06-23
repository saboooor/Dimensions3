package me.xxastaspastaxx.dimensions.addons.hubworld;

import java.io.File;
import java.io.IOException;
import java.util.List;
import me.xxastaspastaxx.dimensions.Dimensions;
import me.xxastaspastaxx.dimensions.DimensionsUtils;
import me.xxastaspastaxx.dimensions.commands.DimensionsCommand;
import me.xxastaspastaxx.dimensions.completePortal.CompletePortal;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class AddHubPortalCommand extends DimensionsCommand {

  DimensionsHubWorld main;

  public AddHubPortalCommand(
      String command,
      String args,
      String[] aliases,
      String description,
      String permission,
      boolean adminCommand,
      DimensionsHubWorld main) {
    super(command, args, aliases, description, permission, adminCommand);
    this.main = main;
  }

  @Override
  public void execute(CommandSender sender, String[] args) {

    if (!(sender instanceof Player)) return;

    CompletePortal compl = null;
    List<Block> los = ((Player) sender).getLineOfSight(null, 5);
    for (Block block : los) {
      if ((compl =
              Dimensions.getCompletePortalManager()
                  .getCompletePortal(block.getLocation(), false, false))
          != null) break;
    }
    if (compl == null) {
      sender.sendMessage("§7[§cDimensions§7]§a No portal found");
      return;
    }

    sender.sendMessage(
        "§7[§cDimensions§7]§a The portal has been set as a hub portal. Please use §n/dim reload§a"
            + " to apply changes");

    YamlConfiguration conf = main.getPortalConfig(compl.getCustomPortal());
    List<String> l = conf.getStringList("Addon.Hub.Portals.List");
    l.add(DimensionsUtils.locationToString(compl.getCenter(), ","));
    conf.set("Addon.Hub.Portals.List", l);

    try {
      conf.save(
          new File("plugins/Dimensions/Portals/" + compl.getCustomPortal().getPortalId() + ".yml"));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
