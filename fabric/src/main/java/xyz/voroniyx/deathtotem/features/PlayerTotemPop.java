package xyz.voroniyx.deathtotem.features;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import xyz.voroniyx.deathtotem.DeathTotemMod;
import xyz.voroniyx.deathtotem.mixin_interfaces.IChunkMapMixin;

import java.util.UUID;

public class PlayerTotemPop {

    public static void handle(ServerPlayer player, ItemStack triggeringTotem) {
        UUID playerUUID = player.getUUID();

        boolean hasEnableOverride = DeathTotemMod.CONFIG.getData()
                .HasActiveEnableTotemConsumeOverrideThatIsTrue(playerUUID);
        boolean globalEnable = DeathTotemMod.CONFIG.getData().EnableTotemConsume;
        if (!hasEnableOverride && !globalEnable) {
            return;
        }

        String requiredTriggerName = DeathTotemMod.CONFIG.getData().GetNameOfTriggeringTotemOverride(playerUUID);
        if (requiredTriggerName != null && !requiredTriggerName.isBlank()
                && !triggeringTotemMatches(triggeringTotem, requiredTriggerName)) {
            return;
        }

        Boolean consumeWhenLastTotemUsedPlayerOverride = DeathTotemMod.CONFIG.getData()
                .GetTotemConsumeOnlyWhenLastTotemUsedOverride(playerUUID);
        boolean finalConsumeWhenLastOnly = (consumeWhenLastTotemUsedPlayerOverride != null)
                ? consumeWhenLastTotemUsedPlayerOverride
                : DeathTotemMod.CONFIG.getData().TotemConsumeOnlyWhenLastTotemUsed;

        if (finalConsumeWhenLastOnly && player.getInventory().contains(x -> x.is(Items.TOTEM_OF_UNDYING))) {
            return;
        }

        ServerLevel level = player.level();
        String playerName = player.getName().getString();

        double playerX = player.getX();
        double playerY = player.getY();
        double playerZ = player.getZ();

        var chunkMap = ((IChunkMapMixin) level.getChunkSource().chunkMap).deathtotemmod$GetVisibleChunkMap();

        BarrelBlockEntity bestBarrel = null;
        int bestIndex = -1;
        double bestDistSq = Double.MAX_VALUE;
        long bestPacked = Long.MAX_VALUE;

        for (var chunkHolder : chunkMap.values()) {
            LevelChunk chunk = chunkHolder.getTickingChunk();
            if (chunk == null) {
                continue;
            }

            for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                if (!(blockEntity instanceof BarrelBlockEntity barrel)) {
                    continue;
                }

                int totemIndex = matchingTotemIndex(barrel, playerName);
                if (totemIndex == -1) {
                    continue;
                }

                BlockPos pos = barrel.getBlockPos();
                double distSq = pos.distToCenterSqr(playerX, playerY, playerZ);
                long packed = pos.asLong();

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

        bestBarrel.removeItem(bestIndex, 1);
        bestBarrel.setChanged();

        level.updateNeighbourForOutputSignal(bestBarrel.getBlockPos(), bestBarrel.getBlockState().getBlock());
    }

    private static boolean triggeringTotemMatches(ItemStack triggeringTotem, String requiredName) {
        if (triggeringTotem == null || triggeringTotem.isEmpty()) {
            return false;
        }
        if (!triggeringTotem.has(DataComponents.CUSTOM_NAME)) {
            return false;
        }
        return triggeringTotem.getHoverName().getString().trim().equalsIgnoreCase(requiredName.trim());
    }

    private static int matchingTotemIndex(BarrelBlockEntity barrel, String playerName) {
        int totemIndex = hasOnlySingleTotem(barrel);
        if (totemIndex == -1) {
            return -1;
        }

        ItemStack totemStack = barrel.getItem(totemIndex);
        if (!totemStack.has(DataComponents.CUSTOM_NAME)) {
            return -1;
        }
        if (!totemStack.getHoverName().getString().equals(playerName)) {
            return -1;
        }

        return totemIndex;
    }

    private static int hasOnlySingleTotem(BarrelBlockEntity barrel) {
        int totemIndex = -1;
        for (int i = 0; i < barrel.getContainerSize(); i++) {
            ItemStack stack = barrel.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (totemIndex != -1 || !stack.is(Items.TOTEM_OF_UNDYING) || stack.getCount() != 1) {
                return -1;
            }
            totemIndex = i;
        }
        return totemIndex;
    }
}