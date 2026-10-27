package xyz.voroniyx.deathtotem.paper;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Barrel;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import xyz.voroniyx.deathtotem.config.ModConfig;

public class MarkedBarrelListener implements Listener {

    private final NamespacedKey ghostKey;
    private final Material markerBlock;

    public MarkedBarrelListener(DeathTotemPlugin plugin) {
        this.ghostKey = new NamespacedKey(plugin, "ghost");

        ModConfig config = plugin.getConfigManager().getData();
        Material marker = null;
        if (config.EnableExperimentalMarkedBarrels) {
            Material parsed = config.ExperimentalMarkerBlock == null
                    ? null
                    : Material.matchMaterial(config.ExperimentalMarkerBlock);
            if (parsed == null || !parsed.isBlock() || parsed.isAir()) {
                plugin.getLogger().severe("ExperimentalMarkerBlock '" + config.ExperimentalMarkerBlock
                        + "' is not a valid block, marked barrels stay disabled");
            } else {
                marker = parsed;
                plugin.getLogger().info("Experimental marked barrels enabled, marker block: " + parsed.getKey());
            }
        }
        this.markerBlock = marker;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder(false) instanceof Barrel barrel)) {
            return;
        }
        int rawSlot = event.getRawSlot();
        if (rawSlot < 0) {
            return;
        }

        boolean marked = isMarked(barrel);
        ClickType click = event.getClick();
        ItemStack inSlot = event.getCurrentItem();

        if (rawSlot >= top.getSize()) {
            if (marked && click.isShiftClick() && isPlainTotem(inSlot)) {
                event.setCancelled(true);
                int target = top.firstEmpty();
                if (target != -1) {
                    top.setItem(target, createGhost(player));
                }
            }
            return;
        }

        if (isGhost(inSlot) || (marked && isTotem(inSlot))) {
            event.setCancelled(true);
            boolean takesOut = switch (click) {
                case LEFT, RIGHT -> isEmpty(event.getCursor());
                case SHIFT_LEFT, SHIFT_RIGHT, NUMBER_KEY, SWAP_OFFHAND, DROP, CONTROL_DROP -> true;
                default -> false;
            };
            if (takesOut) {
                top.setItem(rawSlot, null);
            }
            return;
        }

        if (!marked) {
            return;
        }

        ItemStack offered = switch (click) {
            case LEFT, RIGHT -> event.getCursor();
            case NUMBER_KEY -> player.getInventory().getItem(event.getHotbarButton());
            case SWAP_OFFHAND -> player.getInventory().getItemInOffHand();
            default -> null;
        };
        if (!isPlainTotem(offered)) {
            return;
        }

        event.setCancelled(true);
        if (isEmpty(inSlot)) {
            top.setItem(rawSlot, createGhost(player));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDrag(InventoryDragEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder(false) instanceof Barrel barrel) || !isMarked(barrel)) {
            return;
        }
        if (!isTotem(event.getOldCursor())) {
            return;
        }
        for (int rawSlot : event.getRawSlots()) {
            if (rawSlot < top.getSize()) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMoveItem(InventoryMoveItemEvent event) {
        if (!isGhost(event.getItem())) {
            return;
        }
        Inventory source = event.getSource();
        if (!(source.getHolder(false) instanceof Barrel)) {
            return;
        }

        event.setCancelled(true);
        for (int i = 0; i < source.getSize(); i++) {
            if (isGhost(source.getItem(i))) {
                source.setItem(i, null);
                break;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onItemSpawn(ItemSpawnEvent event) {
        if (isGhost(event.getEntity().getItemStack())) {
            event.setCancelled(true);
        }
    }

    private boolean isMarked(Barrel barrel) {
        return markerBlock != null && barrel.getBlock().getRelative(BlockFace.DOWN).getType() == markerBlock;
    }

    private boolean isGhost(ItemStack stack) {
        if (!isTotem(stack) || !stack.hasItemMeta()) {
            return false;
        }
        return stack.getItemMeta().getPersistentDataContainer().has(ghostKey, PersistentDataType.BYTE);
    }

    private boolean isPlainTotem(ItemStack stack) {
        return isTotem(stack) && !isGhost(stack);
    }

    private static boolean isTotem(ItemStack stack) {
        return stack != null && stack.getType() == Material.TOTEM_OF_UNDYING;
    }

    private static boolean isEmpty(ItemStack stack) {
        return stack == null || stack.getType().isAir();
    }

    private ItemStack createGhost(Player player) {
        ItemStack ghost = new ItemStack(Material.TOTEM_OF_UNDYING);
        ghost.editMeta(meta -> {
            meta.displayName(Component.text(player.getName()));
            meta.getPersistentDataContainer().set(ghostKey, PersistentDataType.BYTE, (byte) 1);
        });
        return ghost;
    }
}
