package me.xxastaspastaxx.dimensions.addons.stylishportals;

import me.xxastaspastaxx.dimensions.commands.DimensionsCommand;
import me.xxastaspastaxx.dimensions.settings.DimensionsSettings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StylishPortalCreateCommand extends DimensionsCommand {

  DimensionsStylishPortals main;

  public StylishPortalCreateCommand(
      String command,
      String args,
      String[] aliases,
      String description,
      String permission,
      boolean adminCommand,
      DimensionsStylishPortals main) {
    super(command, args, aliases, description, permission, adminCommand);
    this.main = main;
  }

  @Override
  public void execute(CommandSender sender, String[] args) {

    if (!(sender instanceof Player)) return;

    Player p = (Player) sender;

    Block block = p.getTargetBlockExact(5);
    if (block == null || block.getBlockData() == null) {
      p.sendMessage(
          DimensionsSettings.getPrefix()
              .append(Component.text("Block not found", NamedTextColor.RED)));
      return;
    }

    String str = block.getBlockData().getAsString();

    Component message =
        Component.text("Click here to copy block data", NamedTextColor.GREEN)
            .clickEvent(ClickEvent.copyToClipboard(str))
            .hoverEvent(HoverEvent.showText(Component.text(str)));
    p.sendMessage(DimensionsSettings.getPrefix().append(message));
  }
}
