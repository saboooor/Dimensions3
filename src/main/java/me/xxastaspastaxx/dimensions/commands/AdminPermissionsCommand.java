package me.xxastaspastaxx.dimensions.commands;

import java.util.ArrayList;
import java.util.List;
import me.xxastaspastaxx.dimensions.Dimensions;
import me.xxastaspastaxx.dimensions.DimensionsUtils;
import me.xxastaspastaxx.dimensions.settings.DimensionsSettings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

public class AdminPermissionsCommand extends DimensionsCommand {

  private float commandsPerPage = 5;

  public AdminPermissionsCommand(
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

    TextComponent.Builder head =
        Component.text()
            .append(DimensionsSettings.getPrefix())
            .append(Component.text("Commands list:"));
    int page = 0;
    if (args.length > 1 && DimensionsUtils.isInt(args[1]) && !args[1].equals("0"))
      page = Integer.parseInt(args[1]) - 1;
    ArrayList<DimensionsCommand> commandList = Dimensions.getCommandManager().getAdminCommands();
    for (int i = (int) Math.max(page * commandsPerPage, 0);
        i
            < Math.min(
                page * commandsPerPage + (commandList.size() - commandsPerPage * page),
                commandsPerPage * (1 + page));
        i++) {
      DimensionsCommand cmd = (DimensionsCommand) commandList.toArray()[i];
      head.append(Component.newline())
          .append(Component.text("/dim " + cmd.getCommand() + " " + cmd.getArgs() + " "))
          .append(Component.text("-", NamedTextColor.RED))
          .append(Component.text(" " + cmd.getPermission(), NamedTextColor.GRAY));
    }
    if (Math.min(commandList.size() - (1 + page) * commandsPerPage, commandsPerPage * (2 + page))
            > 0
        || page != 0)
      head.append(
          Component.text(
              "\n\n**Page "
                  + (page + 1)
                  + "/"
                  + ((int) Math.ceil(commandList.size() / commandsPerPage))
                  + "**"));

    sender.sendMessage(head.build());
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, String[] args) {
    ArrayList<String> res = new ArrayList<String>();

    if (args.length != 2) return res;

    for (int i = 1;
        i
            <= ((int)
                Math.ceil(
                    Dimensions.getCommandManager().getAdminCommands().size() / commandsPerPage));
        i++) res.add(i + "");

    return res;
  }
}
