package xyz.voroniyx.deathtotem.mixin_interfaces;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.server.level.ChunkHolder;

public interface IChunkMapMixin {
    Long2ObjectLinkedOpenHashMap<ChunkHolder> deathtotemmod$GetVisibleChunkMap();
}

