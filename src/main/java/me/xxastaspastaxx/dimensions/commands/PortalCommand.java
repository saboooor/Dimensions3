package me.xxastaspastaxx.dimensions.commands;

import java.util.ArrayList;
import java.util.List;
import me.xxastaspastaxx.dimensions.Dimensions;
import me.xxastaspastaxx.dimensions.DimensionsUtils;
import me.xxastaspastaxx.dimensions.completePortal.CompletePortal;
import me.xxastaspastaxx.dimensions.customportal.CustomPortal;
import me.xxastaspastaxx.dimensions.settings.DimensionsSettings;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PortalCommand extends DimensionsCommand {

  public PortalCommand(
      String command,
      String args,
      String[] aliases,
      String description,
      String permission,
      boolean adminCommand) {
    super(command, args, aliases, description, permission, adminCommand);
  }

  @Override
  public void execute(CommandSender sender, String[] args) {

    if (args.length == 1) {
      if (!(sender instanceof Player)) {
        sender.sendMessage(
            DimensionsSettings.getPrefix()
                + "This command without arguments can only be used from players.");
        return;
      }
      List<Block> los = ((Player) sender).getLineOfSight(null, 5);
      for (Block block : los) {
        if (!DimensionsUtils.isAir(block)) break;
        CompletePortal compl =
            Dimensions.getCompletePortalManager()
                .getCompletePortal(block.getLocation(), false, false);
        if (compl != null) {
          CustomPortal portal = compl.getCustomPortal();
          sender.sendMessage(
              DimensionsSettings.getPrefix()
                  + portal.getDisplayName()
                  + ":"
                  + ChatColor.GRAY
                  + " Is built from "
                  + ChatColor.RED
                  + portal.getOutsideMaterial()
                  + ChatColor.GRAY
                  + ", is ignited using "
                  + ChatColor.RED
                  + portal.getLighterMaterial()
                  + ChatColor.GRAY
                  + " and this specific portal goes to "
                  + ChatColor.RED
                  + (compl.getLinkedPortal() == null
                      ? portal.getWorld().getName()
                      : compl.getLinkedPortal().getWorld().getName())
                  + ChatColor.GRAY
                  + ".");
          return;
        }
      }

      sender.sendMessage(
          DimensionsSettings.getPrefix() + "Could not find a portal where you look at.");
    } else if (args.length == 2) {
      CustomPortal portal = Dimensions.getCustomPortalManager().getCustomPortal(args[1]);
      if (portal != null) {
        sender.sendMessage(
            DimensionsSettings.getPrefix()
                + portal.getDisplayName()
                + ":"
                + ChatColor.GRAY
                + " Is built from "
                + ChatColor.RED
                + portal.getOutsideMaterial()
                + ChatColor.GRAY
                + ", is ignited using "
                + ChatColor.RED
                + portal.getLighterMaterial()
                + ChatColor.GRAY
                + " and goes to "
                + ChatColor.RED
                + portal.getWorld().getName()
                + ChatColor.GRAY
                + ".");
      } else {
        sender.sendMessage(DimensionsSettings.getPrefix() + "Could not find specified portal.");
      }
    } else {
      sender.sendMessage(
          DimensionsSettings.getPrefix()
              + "Missing argument. Please use /dim "
              + this.getCommand()
              + " "
              + this.getArgs());
    }
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, String[] args) {
    ArrayList<String> res = new ArrayList<String>();

    if (args.length != 2) return res;

    Dimensions.getCustomPortalManager().getCustomPortals().forEach(p -> res.add(p.getPortalId()));

    return res;
  }
}
