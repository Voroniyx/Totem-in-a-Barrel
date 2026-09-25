package xyz.voroniyx.deathtotem.paper;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class TotemResurrectListener implements Listener {

    private final DeathTotemPlugin plugin;

    public TotemResurrectListener(DeathTotemPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onResurrect(EntityResurrectEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        ItemStack triggeringTotem = captureTriggeringTotem(player);

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (!player.isOnline()) {
                return;
            }
            PaperTotemPop.handle(plugin, player, triggeringTotem);
        });
    }

    private static ItemStack captureTriggeringTotem(Player player) {
        PlayerInventory inventory = player.getInventory();

        ItemStack mainHand = inventory.getItemInMainHand();
        if (mainHand.getType() == Material.TOTEM_OF_UNDYING) {
            return mainHand.clone();
        }

        ItemStack offHand = inventory.getItemInOffHand();
        if (offHand.getType() == Material.TOTEM_OF_UNDYING) {
            return offHand.clone();
        }

        return null;
    }
}