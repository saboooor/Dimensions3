package me.xxastaspastaxx.dimensions.addons.forcelink;

import java.util.ArrayList;
import java.util.List;
import me.xxastaspastaxx.dimensions.Dimensions;
import me.xxastaspastaxx.dimensions.commands.DimensionsCommand;
import me.xxastaspastaxx.dimensions.completePortal.CompletePortal;
import me.xxastaspastaxx.dimensions.settings.DimensionsSettings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LinkPortalsCommand extends DimensionsCommand {

  DimensionsForceLink main;

  public LinkPortalsCommand(
      String command,
      String args,
      String[] aliases,
      String description,
      String permission,
      boolean adminCommand,
      DimensionsForceLink main) {
    super(command, args, aliases, description, permission, adminCommand);
    this.main = main;
  }

  @Override
  public void execute(CommandSender sender, String[] args) {

    if (!(sender instanceof Player)) return;

    if (args.length != 2) {
      sender.sendMessage(
          DimensionsSettings.getPrefix()
              .append(Component.text("Please use (while looking at a portal) ", NamedTextColor.RED))
              .append(
                  Component.text("/dim forceLink select", NamedTextColor.RED)
                      .decorate(TextDecoration.UNDERLINED))
              .append(Component.text(" to select a portal or ", NamedTextColor.RED))
              .append(
                  Component.text("/dim forceLink set", NamedTextColor.RED)
                      .decorate(TextDecoration.UNDERLINED))
              .append(Component.text(" to link with selected portal", NamedTextColor.RED)));
      return;
    }
    boolean select = args[1].equalsIgnoreCase("select");
    if (!select && !main.savedPortal.containsKey(sender)) {
      sender.sendMessage(
          DimensionsSettings.getPrefix()
              .append(
                  Component.text(
                      "You cannot link the portals because you have not selected one yet.",
                      NamedTextColor.RED)));
      return;
    }

    CompletePortal compl = null;
    List<Block> los = ((Player) sender).getLineOfSight(null, 5);
    for (Block block : los) {
      if ((compl =
              Dimensions.getCompletePortalManager()
                  .getCompletePortal(block.getLocation(), false, false))
          != null) break;
    }
    if (compl == null) {
      sender.sendMessage(
          DimensionsSettings.getPrefix()
              .append(Component.text("No portal found", NamedTextColor.RED)));
      return;
    }

    if (select) {
      main.savedPortal.put((Player) sender, compl);
      sender.sendMessage(
          DimensionsSettings.getPrefix()
              .append(Component.text("Selected portal!", NamedTextColor.GREEN)));
    } else {
      CompletePortal selected = main.savedPortal.remove(sender);
      selected.setLinkedPortal(compl);
      compl.setLinkedPortal(selected);
      sender.sendMessage(
          DimensionsSettings.getPrefix()
              .append(Component.text("Portals have been linked.", NamedTextColor.GREEN)));
    }
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, String[] args) {
    ArrayList<String> res = new ArrayList<String>();

    if (args.length != 2) return res;

    res.add("select");
    res.add("set");

    return res;
  }
}
