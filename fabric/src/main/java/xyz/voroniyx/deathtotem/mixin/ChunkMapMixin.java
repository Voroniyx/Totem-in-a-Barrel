package xyz.voroniyx.deathtotem.mixin;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import xyz.voroniyx.deathtotem.mixin_interfaces.IChunkMapMixin;

@Mixin(ChunkMap.class)
public class ChunkMapMixin implements IChunkMapMixin {
    @Shadow
    private volatile Long2ObjectLinkedOpenHashMap<ChunkHolder> visibleChunkMap;

    @Unique
    public Long2ObjectLinkedOpenHashMap<ChunkHolder> deathtotemmod$GetVisibleChunkMap() {
        return visibleChunkMap;
    }
}
