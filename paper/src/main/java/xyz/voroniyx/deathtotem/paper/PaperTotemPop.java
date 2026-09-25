package xyz.voroniyx.deathtotem.paper;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Barrel;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.voroniyx.deathtotem.config.ModConfig;

import java.util.Objects;
import java.util.UUID;

public final class PaperTotemPop {

    private PaperTotemPop() {
    }

    public static void handle(DeathTotemPlugin plugin, Player player, ItemStack triggeringTotem) {
        ModConfig config = plugin.getConfigManager().getData();
        UUID playerUUID = player.getUniqueId();

        boolean hasEnableOverride = config.HasActiveEnableTotemConsumeOverrideThatIsTrue(playerUUID);
        boolean globalEnable = config.EnableTotemConsume;
        if (!hasEnableOverride && !globalEnable) {
            return;
        }

        String requiredTriggerName = config.GetNameOfTriggeringTotemOverride(playerUUID);
        if (requiredTriggerName != null && !requiredTriggerName.isBlank()
                && !triggeringTotemMatches(triggeringTotem, requiredTriggerName)) {
            return;
        }

        Boolean consumeWhenLastOverride = config.GetTotemConsumeOnlyWhenLastTotemUsedOverride(playerUUID);
        boolean finalConsumeWhenLastOnly = (consumeWhenLastOverride != null)
                ? consumeWhenLastOverride
                : config.TotemConsumeOnlyWhenLastTotemUsed;

        if (finalConsumeWhenLastOnly && hasTotemInInventory(player)) {
            return;
        }

        String playerName = player.getName();
        World level = player.getWorld();

        double playerX = player.getLocation().getX();
        double playerY = player.getLocation().getY();
        double playerZ = player.getLocation().getZ();

        Barrel bestBarrel = null;
        int bestIndex = -1;
        double bestDistSq = Double.MAX_VALUE;
        long bestPacked = Long.MAX_VALUE;

        for (Chunk chunk : level.getLoadedChunks()) {
            for (BlockState blockState : chunk.getTileEntities(false)) {
                if (!(blockState instanceof Barrel barrel)) {
                    continue;
                }

                int totemIndex = matchingTotemIndex(barrel.getInventory(), playerName);
                if (totemIndex == -1) {
                    continue;
                }

                double dx = (barrel.getX() + 0.5D) - playerX;
                double dy = (barrel.getY() + 0.5D) - playerY;
                double dz = (barrel.getZ() + 0.5D) - playerZ;
                double distSq = dx * dx + dy * dy + dz * dz;
                long packed = packPosition(barrel.getX(), barrel.getY(), barrel.getZ());

                if (distSq < bestDistSq || (distSq == bestDistSq && packed < bestPacked)) {
                    bestBarrel = barrel;
                    bestIndex = totemIndex;
                    bestDistSq = distSq;
                    bestPacked = packed;
                }
            }
        }

        if (bestBarrel == null) {
            return;
        }

        bestBarrel.getInventory().setItem(bestIndex, null);

        bestBarrel.update(false, true);
    }

    private static boolean triggeringTotemMatches(ItemStack triggeringTotem, String requiredName) {
        if (triggeringTotem == null || triggeringTotem.getType().isAir()) {
            return false;
        }
        ItemMeta meta = triggeringTotem.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return false;
        }
        String actualName = PlainTextComponentSerializer.plainText().serialize(meta.displayName());
        return actualName.trim().equalsIgnoreCase(requiredName.trim());
    }

    private static int matchingTotemIndex(Inventory inventory, String playerName) {
        int totemIndex = indexOfOnlySingleTotem(inventory);
        if (totemIndex == -1) {
            return -1;
        }

        ItemStack totemStack = inventory.getItem(totemIndex);
        if (totemStack == null) {
            return -1;
        }

        ItemMeta meta = totemStack.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return -1;
        }

        String totemName = PlainTextComponentSerializer.plainText().serialize(Objects.requireNonNull(meta.displayName()));
        return totemName.equals(playerName) ? totemIndex : -1;
    }

    private static boolean hasTotemInInventory(Player player) {
        for (ItemStack stack : player.getInventory().getStorageContents()) {
            if (stack != null && stack.getType() == Material.TOTEM_OF_UNDYING) {
                return true;
            }
        }
        return player.getInventory().getItemInOffHand().getType() == Material.TOTEM_OF_UNDYING;
    }

    private static int indexOfOnlySingleTotem(Inventory inventory) {
        int totemIndex = -1;
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack == null || stack.getType().isAir()) {
                continue;
            }
            if (totemIndex != -1 || stack.getType() != Material.TOTEM_OF_UNDYING || stack.getAmount() != 1) {
                return -1;
            }
            totemIndex = i;
        }
        return totemIndex;
    }

    private static long packPosition(int x, int y, int z) {
        return ((long) x & 0x3FFFFFFL) << 38
                | ((long) z & 0x3FFFFFFL) << 12
                | ((long) y & 0xFFFL);
    }
}