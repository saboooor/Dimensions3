package me.xxastaspastaxx.dimensions.addons.betterportals;

import java.util.List;
import me.xxastaspastaxx.dimensions.Dimensions;
import me.xxastaspastaxx.dimensions.DimensionsUtils;
import me.xxastaspastaxx.dimensions.commands.DimensionsCommand;
import me.xxastaspastaxx.dimensions.completePortal.CompletePortal;
import me.xxastaspastaxx.dimensions.settings.DimensionsSettings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MirrorPortalCommand extends DimensionsCommand {

  DimensionsBetterPortals main;

  public MirrorPortalCommand(
      String command,
      String args,
      String[] aliases,
      String description,
      String permission,
      boolean adminCommand,
      DimensionsBetterPortals main) {
    super(command, args, aliases, description, permission, adminCommand);
    this.main = main;
  }

  @Override
  public void execute(CommandSender sender, String[] args) {

    if (!(sender instanceof Player)) return;

    List<Block> los = ((Player) sender).getLineOfSight(null, 5);
    for (Block block : los) {
      if (!DimensionsUtils.isAir(block)) break;
      CompletePortal compl =
          Dimensions.getCompletePortalManager()
              .getCompletePortal(block.getLocation(), false, false);
      if (compl != null) {
        CompletePortal link = compl.getLinkedPortal();

        main.unlink(compl);
        main.link(
            compl,
            link,
            !(compl.getTag("mirrored") != null && ((boolean) compl.getTag("mirrored"))));

        sender.sendMessage(
            DimensionsSettings.getPrefix()
                .append(Component.text("The portal has been mirrored", NamedTextColor.GREEN)));
        return;
      }
    }
  }
}
