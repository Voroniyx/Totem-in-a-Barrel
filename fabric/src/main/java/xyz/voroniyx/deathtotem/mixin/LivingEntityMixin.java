package xyz.voroniyx.deathtotem.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.voroniyx.deathtotem.features.PlayerTotemPop;


@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Unique
    private ItemStack deathtotem$poppedTotem = ItemStack.EMPTY;

    @Inject(
            method = "checkTotemDeathProtection",
            at = @At("HEAD")
    )
    private void deathtotem$captureTotem(
            DamageSource killingDamage,
            CallbackInfoReturnable<Boolean> cir
    ) {
        deathtotem$poppedTotem = ItemStack.EMPTY;

        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof ServerPlayer)) {
            return;
        }

        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = self.getItemInHand(hand);
            if (stack.is(Items.TOTEM_OF_UNDYING)) {
                deathtotem$poppedTotem = stack.copy();
                break;
            }
        }
    }

    @Inject(
            method = "checkTotemDeathProtection",
            at = @At("RETURN")
    )
    private void deathtotem$onTotemPop(
            DamageSource killingDamage,
            CallbackInfoReturnable<Boolean> cir
    ) {
        ItemStack popped = deathtotem$poppedTotem;
        deathtotem$poppedTotem = ItemStack.EMPTY;

        if (!cir.getReturnValueZ()) {
            return;
        }

        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof ServerPlayer player)) {
            return;
        }

        PlayerTotemPop.handle(player, popped);
    }
}
