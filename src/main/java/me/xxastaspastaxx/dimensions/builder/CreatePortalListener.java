package me.xxastaspastaxx.dimensions.builder;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class CreatePortalListener implements Listener {

  private CreatePortalManager manager;

  public CreatePortalListener(CreatePortalManager manager) {
    this.manager = manager;
  }

  @EventHandler(ignoreCancelled = true)
  public void onItemClick(InventoryClickEvent e) {
    if (e.getInventory() == null
        || e.getCurrentItem() == null
        || e.getClickedInventory() == null
        || e.getWhoClicked() == null
        || !(e.getWhoClicked() instanceof Player)) return;

    if (manager.click(
        (Player) e.getWhoClicked(),
        e.getClickedInventory(),
        e.getRawSlot(),
        e.isRightClick(),
        e.isShiftClick())) {
      e.setCancelled(true);
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void onchat(AsyncChatEvent e) {
    if (!manager.hasInstance(e.getPlayer())) return;

    String msg = PlainTextComponentSerializer.plainText().serialize(e.message());
    e.setCancelled(manager.getInstance(e.getPlayer()).handleChatInput(msg));
  }
}
