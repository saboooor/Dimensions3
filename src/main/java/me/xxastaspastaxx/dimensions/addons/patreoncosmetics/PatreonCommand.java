package me.xxastaspastaxx.dimensions.addons.patreoncosmetics;

import java.util.ArrayList;
import java.util.List;
import me.xxastaspastaxx.dimensions.commands.DimensionsCommand;
import me.xxastaspastaxx.dimensions.settings.DimensionsSettings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PatreonCommand extends DimensionsCommand {

  DimensionsPatreonCosmetics main;

  public PatreonCommand(
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

    Player p = (Player) sender;

    if (args.length >= 2) {
      Player p2;
      if ((p2 = Bukkit.getPlayer(args[1])) != null) {
        p.sendMessage(DimensionsSettings.getPrefix().append(getStatusString(p2)));
      } else {
        p.sendMessage(
            DimensionsSettings.getPrefix()
                .append(Component.text("Could not find player " + args[1], NamedTextColor.RED)));
      }
    } else {
      p.sendMessage(DimensionsSettings.getPrefix().append(getStatusString(p)));
    }
  }

  public Component getStatusString(Player p) {
    TextComponent.Builder builder =
        Component.text()
            .append(Component.text("Player: ", NamedTextColor.GRAY))
            .append(Component.text(p.getName(), NamedTextColor.GREEN))
            .append(Component.newline())
            .append(Component.text("UUID: ", NamedTextColor.GRAY))
            .append(Component.text(p.getUniqueId().toString(), NamedTextColor.GREEN))
            .append(Component.newline())
            .append(Component.text("Supporter: ", NamedTextColor.GRAY));

    if (main.getUsers().containsKey(p.getUniqueId())) {
      builder.append(Component.text("true", NamedTextColor.GREEN));
    } else {
      builder.append(Component.text("false", NamedTextColor.RED));
    }
    builder.append(Component.newline());
    builder.append(Component.text("Effects:\n", NamedTextColor.GRAY));

    if (main.getUsers().containsKey(p.getUniqueId())) {
      for (String s : main.getUsers().get(p.getUniqueId()).keySet()) {
        builder.append(
            Component.text(
                "  " + s + ": " + main.getUsers().get(p.getUniqueId()).get(s) + "\n",
                NamedTextColor.GREEN));
      }
    } else {
      builder.append(Component.text("  No active effects", NamedTextColor.RED));
    }
    return builder.build();
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, String[] args) {
    ArrayList<String> res = new ArrayList<String>();

    if (args.length != 2) return res;

    Bukkit.getOnlinePlayers().forEach(p -> res.add(p.getName()));

    return res;
  }
}
