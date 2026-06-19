package me.xxastaspastaxx.dimensions.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

public class WorldsCommand extends DimensionsCommand {

  public WorldsCommand(
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
    sender.sendMessage(Component.text("Available worlds: ", NamedTextColor.GRAY));
    for (World world : Bukkit.getWorlds()) {
      sender.sendMessage(
          Component.text("- ", NamedTextColor.GRAY)
              .append(Component.text(world.getName(), NamedTextColor.RED)));
    }
  }
}
