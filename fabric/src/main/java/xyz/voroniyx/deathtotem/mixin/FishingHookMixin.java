package xyz.voroniyx.deathtotem.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.ticks.ScheduledTick;
import net.minecraft.world.ticks.TickPriority;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.voroniyx.deathtotem.DeathTotemMod;
import xyz.voroniyx.deathtotem.features.better_fishing_hook.FishingRedstoneData;

import java.util.List;

@Mixin(FishingHook.class)
public class FishingHookMixin {
    @Inject(method = "onHitBlock", at = @At("TAIL"))
    private void onTargetBlockHit(BlockHitResult hitResult, CallbackInfo ci) {
        if (!DeathTotemMod.CONFIG.getData().EnableBetterFishingHookFeature) {
            return;
        }

        FishingHook bobber = (FishingHook) (Object) this;

        if (!bobber.level().isClientSide() && bobber.getOwner() instanceof Player player) {
            BlockPos pos = hitResult.getBlockPos();
            ServerLevel world = (ServerLevel) bobber.level();

            if (world.getBlockState(pos).is(Blocks.TARGET)) {
                FishingRedstoneData data = FishingRedstoneData.getServerState(world);
                data.setTarget(player.getUUID(), pos);

                ItemStack mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
                if (mainHand.is(net.minecraft.world.item.Items.FISHING_ROD)) {
                    mainHand.remove(DataComponents.LORE);
                }
            }
        }
    }

    @Inject(method = "retrieve", at = @At("HEAD"))
    private void onReelIn(ItemStack rod, CallbackInfoReturnable<Integer> cir) {
        if (!DeathTotemMod.CONFIG.getData().EnableBetterFishingHookFeature) {
            return;
        }

        FishingHook bobber = (FishingHook) (Object) this;
        Entity owner = bobber.getOwner();

        if (owner instanceof Player player && !bobber.level().isClientSide()) {
            ServerLevel world = (ServerLevel) bobber.level();
            FishingRedstoneData data = FishingRedstoneData.getServerState(world);
            FishingRedstoneData.TargetState targetState = data.getTarget(player.getUUID());

            if (targetState != null) {
                // Arming it
                if (!targetState.isArmed) {
                    data.markArmed(player.getUUID());

                    BlockPos pos = targetState.pos;
                    Component targetLine = Component.literal("Target: X: " + pos.getX() + " Y: " + pos.getY() + " Z: " + pos.getZ());

                    ItemLore lore = new ItemLore(List.of(targetLine));
                    rod.set(DataComponents.LORE, lore);
                }
                // Trigger
                else {
                    BlockPos targetPos = targetState.pos;

                    if (world.getBlockState(targetPos).is(Blocks.TARGET)) {
                        world.setBlock(targetPos, Blocks.TARGET.defaultBlockState().setValue(BlockStateProperties.POWER, 15), 3);
                        world.getBlockTicks().schedule(
                                new ScheduledTick<>(
                                        Blocks.TARGET,
                                        targetPos,
                                        world.getGameTime() + 20,
                                        TickPriority.NORMAL,
                                        0
                                )
                        );
                    }

                    InteractionHand activeHand = player.getUsedItemHand();
                    rod.hurtAndBreak(
                            1,
                            world,
                            player instanceof ServerPlayer sp ?
                                    sp
                                    : null,
                            (item) -> player.onEquippedItemBroken(item, activeHand.asEquipmentSlot())
                    );

                    rod.remove(DataComponents.LORE);

                    data.removeTarget(player.getUUID());
                }
            }
        }
    }
}
