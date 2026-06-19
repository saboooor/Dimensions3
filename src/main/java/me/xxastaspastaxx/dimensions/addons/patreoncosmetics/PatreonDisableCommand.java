package me.xxastaspastaxx.dimensions.addons.patreoncosmetics;

import me.xxastaspastaxx.dimensions.commands.DimensionsCommand;
import me.xxastaspastaxx.dimensions.settings.DimensionsSettings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PatreonDisableCommand extends DimensionsCommand {

  DimensionsPatreonCosmetics main;

  public PatreonDisableCommand(
      String command,
      String args,
      String[] aliases,
      String description,
      String permission,
      boolean adminCommand,
      DimensionsPatreonCosmetics main) {
    super(command, args, aliases, description, permission, adminCommand);
    this.main = main;
  }

  @Override
  public void execute(CommandSender sender, String[] args) {

    if (!(sender instanceof Player)) return;

    main.getUsers().remove(((Player) sender).getUniqueId());
    sender.sendMessage(
        DimensionsSettings.getPrefix()
            .append(
                Component.text(
                    "Succesfully disabled portal effects for the current session.",
                    NamedTextColor.GREEN)));
  }
}
