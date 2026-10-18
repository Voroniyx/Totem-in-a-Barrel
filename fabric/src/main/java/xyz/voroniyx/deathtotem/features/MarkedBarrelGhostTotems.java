package xyz.voroniyx.deathtotem.features;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import xyz.voroniyx.deathtotem.DeathTotemMod;
import xyz.voroniyx.deathtotem.config.ModConfig;

public class MarkedBarrelGhostTotems {

    private static final String GHOST_TAG = "deathtotem_ghost";
    private static final int OFFHAND_SWAP_BUTTON = 40;

    private static boolean enabled = false;
    private static Block markerBlock = Blocks.AIR;

    public static void init(ModConfig config) {
        enabled = false;
        if (!config.EnableExperimentalMarkedBarrels) {
            return;
        }

        Identifier id = Identifier.tryParse(config.ExperimentalMarkerBlock);
        Block block = id == null ? Blocks.AIR : BuiltInRegistries.BLOCK.getOptional(id).orElse(Blocks.AIR);
        if (block == Blocks.AIR) {
            DeathTotemMod.LOGGER.error(
                    "ExperimentalMarkerBlock '{}' is not a valid block, marked barrels stay disabled",
                    config.ExperimentalMarkerBlock);
            return;
        }

        markerBlock = block;
        enabled = true;
        DeathTotemMod.LOGGER.info("Experimental marked barrels enabled, marker block: {}", config.ExperimentalMarkerBlock);
    }

    public static boolean isGhost(ItemStack stack) {
        if (!stack.is(Items.TOTEM_OF_UNDYING)) {
            return false;
        }
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && data.copyTag().getBooleanOr(GHOST_TAG, false);
    }

    public static boolean handleClick(
            AbstractContainerMenu menu,
            int slotIndex,
            int button,
            ContainerInput input,
            ServerPlayer player
    ) {
        if (slotIndex < 0 || slotIndex >= menu.slots.size()) {
            return false;
        }
        if (!(menu instanceof ChestMenu chestMenu) || !(chestMenu.getContainer() instanceof BarrelBlockEntity barrel)) {
            return false;
        }

        var marked = enabled && isMarked(barrel);
        var slot = menu.slots.get(slotIndex);

        if (slot.container == barrel) {
            return handleBarrelSlotClick(menu, barrel, slot, marked, button, input, player);
        }
        if (marked && input == ContainerInput.QUICK_MOVE && isPlainTotem(slot.getItem())) {
            int target = firstEmptySlot(barrel);
            if (target != -1) {
                barrel.setItem(target, createGhost(player));
            }
            return true;
        }
        return false;
    }

    private static boolean handleBarrelSlotClick(
            AbstractContainerMenu menu,
            BarrelBlockEntity barrel,
            Slot slot,
            boolean marked,
            int button,
            ContainerInput input,
            ServerPlayer player
    ) {
        ItemStack inSlot = slot.getItem();

        if (isGhost(inSlot) || (marked && inSlot.is(Items.TOTEM_OF_UNDYING))) {
            boolean takesOut = switch (input) {
                case PICKUP -> menu.getCarried().isEmpty();
                case QUICK_MOVE, SWAP, THROW -> true;
                default -> false;
            };
            if (takesOut) {
                barrel.setItem(slot.getContainerSlot(), ItemStack.EMPTY);
            }
            return true;
        }

        if (!marked) {
            return false;
        }

        ItemStack offered = switch (input) {
            case PICKUP, QUICK_CRAFT -> menu.getCarried();
            case SWAP -> swapSourceItem(player, button);
            default -> ItemStack.EMPTY;
        };
        if (!isPlainTotem(offered)) {
            return false;
        }

        if (input != ContainerInput.QUICK_CRAFT && inSlot.isEmpty()) {
            barrel.setItem(slot.getContainerSlot(), createGhost(player));
        }
        return true;
    }

    private static ItemStack swapSourceItem(ServerPlayer player, int button) {
        if (button == OFFHAND_SWAP_BUTTON) {
            return player.getOffhandItem();
        }
        if (button >= 0 && button < 9) {
            return player.getInventory().getItem(button);
        }
        return ItemStack.EMPTY;
    }

    private static boolean isMarked(BarrelBlockEntity barrel) {
        Level level = barrel.getLevel();
        return level != null && level.getBlockState(barrel.getBlockPos().below()).is(markerBlock);
    }

    private static boolean isPlainTotem(ItemStack stack) {
        return stack.is(Items.TOTEM_OF_UNDYING) && !isGhost(stack);
    }

    private static int firstEmptySlot(BarrelBlockEntity barrel) {
        for (int i = 0; i < barrel.getContainerSize(); i++) {
            if (barrel.getItem(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    private static ItemStack createGhost(ServerPlayer player) {
        ItemStack ghost = new ItemStack(Items.TOTEM_OF_UNDYING);
        ghost.set(DataComponents.CUSTOM_NAME, Component.literal(player.getName().getString()));
        CustomData.update(DataComponents.CUSTOM_DATA, ghost, tag -> tag.putBoolean(GHOST_TAG, true));
        return ghost;
    }
}
