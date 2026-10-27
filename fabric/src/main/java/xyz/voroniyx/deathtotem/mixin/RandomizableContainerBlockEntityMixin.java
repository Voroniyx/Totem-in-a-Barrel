package xyz.voroniyx.deathtotem.mixin;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.voroniyx.deathtotem.features.MarkedBarrelGhostTotems;

@Mixin(RandomizableContainerBlockEntity.class)
public abstract class RandomizableContainerBlockEntityMixin {

    @Inject(
            method = {"removeItem", "removeItemNoUpdate"},
            at = @At("RETURN"),
            cancellable = true
    )
    private void deathtotem$vanishGhostTotem(CallbackInfoReturnable<ItemStack> cir) {
        if (MarkedBarrelGhostTotems.isGhost(cir.getReturnValue())) {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }
}
