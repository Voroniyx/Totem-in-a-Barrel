package xyz.voroniyx.deathtotem.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.voroniyx.deathtotem.features.MarkedBarrelGhostTotems;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin {

    @Inject(
            method = "clicked",
            at = @At("HEAD"),
            cancellable = true
    )
    private void deathtotem$handleMarkedBarrelClick(
            int slotIndex,
            int button,
            ContainerInput input,
            Player player,
            CallbackInfo ci
    ) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        AbstractContainerMenu self = (AbstractContainerMenu) (Object) this;
        if (MarkedBarrelGhostTotems.handleClick(self, slotIndex, button, input, serverPlayer)) {
            ci.cancel();
        }
    }
}
