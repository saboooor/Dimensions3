package me.xxastaspastaxx.dimensions.commands;

import me.xxastaspastaxx.dimensions.Dimensions;
import me.xxastaspastaxx.dimensions.settings.DimensionsSettings;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class ReloadCommand extends DimensionsCommand {

  public ReloadCommand(
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

    try {
      Dimensions.getInstance().reload();
      sender.sendMessage(DimensionsSettings.getPrefix() + ChatColor.GREEN + "Reload complete");
    } catch (Exception e) {
      sender.sendMessage(
          DimensionsSettings.getPrefix()
              + ChatColor.RED
              + "There was a problem while trying to reload Dimensions. Please check console for"
              + " more information");
      e.printStackTrace();
    }
  }
}
