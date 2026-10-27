package xyz.voroniyx.deathtotem.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
import xyz.voroniyx.deathtotem.features.better_fishing_hook.FishingRodLink;

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
                ItemStack rod = player.getMainHandItem().is(Items.FISHING_ROD)
                        ? player.getMainHandItem()
                        : player.getOffhandItem();
                if (rod.is(Items.FISHING_ROD)) {
                    FishingRodLink.set(rod, new FishingRodLink(world.dimension(), pos, false));
                    rod.remove(DataComponents.LORE);
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

        if (bobber.getOwner() instanceof Player player && !bobber.level().isClientSide()) {
            ServerLevel world = (ServerLevel) bobber.level();
            FishingRodLink link = FishingRodLink.get(rod).orElse(null);

            if (link == null) {
                return;
            }

            // Arming it
            if (!link.isArmed()) {
                FishingRodLink.set(rod, link.armed());

                BlockPos pos = link.pos();
                Component targetLine = Component.literal("Target: " + link.dimension().identifier()
                        + " X: " + pos.getX() + " Y: " + pos.getY() + " Z: " + pos.getZ());
                rod.set(DataComponents.LORE, new ItemLore(List.of(targetLine)));
            }
            // Trigger
            else {
                BlockPos targetPos = link.pos();
                ServerLevel targetWorld = world.getServer().getLevel(link.dimension());

                if (targetWorld != null && targetWorld.getBlockState(targetPos).is(Blocks.TARGET)) {
                    targetWorld.setBlock(targetPos, Blocks.TARGET.defaultBlockState().setValue(BlockStateProperties.POWER, 15), 3);
                    targetWorld.getBlockTicks().schedule(
                            new ScheduledTick<>(
                                    Blocks.TARGET,
                                    targetPos,
                                    targetWorld.getGameTime() + 20,
                                    TickPriority.NORMAL,
                                    0
                            )
                    );
                }

                InteractionHand rodHand = rod == player.getOffhandItem() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
                rod.hurtAndBreak(
                        1,
                        world,
                        player instanceof ServerPlayer sp ? sp : null,
                        (item) -> player.onEquippedItemBroken(item, rodHand.asEquipmentSlot())
                );

                FishingRodLink.clear(rod);
                rod.remove(DataComponents.LORE);
            }
        }
    }
}
