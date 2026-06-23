package me.xxastaspastaxx.dimensions.addons.dimensionsportaltp;

import me.xxastaspastaxx.dimensions.Dimensions;
import me.xxastaspastaxx.dimensions.commands.DimensionsCommand;
import me.xxastaspastaxx.dimensions.completePortal.CompletePortal;
import me.xxastaspastaxx.dimensions.customportal.CustomPortal;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TpToClosestCommand extends DimensionsCommand {

  DimensionsPortalTp main;

  public TpToClosestCommand(
      String command,
      String args,
      String[] aliases,
      String description,
      String permission,
      boolean adminCommand,
      DimensionsPortalTp main) {
    super(command, args, aliases, description, permission, adminCommand);
    this.main = main;
  }

  @Override
  public void execute(CommandSender sender, String[] args) {
    if (args.length <= 1) {
      sender.sendMessage("§7[§cDimensions§7] §cPlease use /dim TpToClosest <portalName> [player]");
      return;
    }
    CustomPortal customPortal = Dimensions.getCustomPortalManager().getCustomPortal(args[1]);
    if (customPortal == null) {
      sender.sendMessage("§7[§cDimensions§7] §cCould not find specified portal");
      return;
    }

    Player targetPlayer =
        args.length == 3
            ? Bukkit.getPlayer(args[2])
            : (sender instanceof Player ? (Player) sender : null);
    if (targetPlayer == null) {
      sender.sendMessage("§7[§cDimensions§7] §cCould not find player");
      return;
    }

    CompletePortal completePortal =
        Dimensions.getCompletePortalManager()
            .getNearestPortal(
                targetPlayer.getLocation(),
                new CompletePortal(customPortal, null, null),
                1,
                false,
                false);
    if (completePortal == null) {
      sender.sendMessage("§7[§cDimensions§7] §cCould not find a portal");
      return;
    }

    targetPlayer.teleport(
        completePortal
            .getCenter()
            .clone()
            .add(
                completePortal.getPortalGeometry().iszAxis() ? 5 : 0,
                0,
                !completePortal.getPortalGeometry().iszAxis() ? 5 : 0));
  }
}
