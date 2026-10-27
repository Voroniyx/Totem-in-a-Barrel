package xyz.voroniyx.deathtotem.mixin;

import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.voroniyx.deathtotem.features.MarkedBarrelGhostTotems;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {

    @Inject(
            method = "tick",
            at = @At("HEAD"),
            cancellable = true
    )
    private void deathtotem$discardGhostTotem(CallbackInfo ci) {
        ItemEntity self = (ItemEntity) (Object) this;
        if (MarkedBarrelGhostTotems.isGhost(self.getItem())) {
            self.discard();
            ci.cancel();
        }
    }
}
