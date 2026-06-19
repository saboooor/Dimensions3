package me.xxastaspastaxx.dimensions.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
    sender.sendMessage(ChatColor.GRAY + "Available worlds: ");
    for (World world : Bukkit.getWorlds()) {
      sender.sendMessage(ChatColor.GRAY + "- " + ChatColor.RED + world.getName());
    }
  }
}
