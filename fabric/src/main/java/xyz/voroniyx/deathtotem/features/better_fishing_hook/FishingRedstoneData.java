package xyz.voroniyx.deathtotem.features.better_fishing_hook;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import xyz.voroniyx.deathtotem.DeathTotemMod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.mojang.serialization.codecs.RecordCodecBuilder;

public class FishingRedstoneData extends SavedData {

    public static class TargetState {
        public final BlockPos pos;
        public boolean isArmed;

        public TargetState(BlockPos pos, boolean isArmed) {
            this.pos = pos;
            this.isArmed = isArmed;
        }

        public static final Codec<TargetState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BlockPos.CODEC.fieldOf("pos").forGetter(ts -> ts.pos),
                Codec.BOOL.fieldOf("isArmed").forGetter(ts -> ts.isArmed)
        ).apply(instance, TargetState::new));
    }

    private final Map<UUID, TargetState> activeTargets;

    private static final Codec<FishingRedstoneData> CODEC = Codec.unboundedMap(
            Codec.STRING.xmap(UUID::fromString, UUID::toString),
            TargetState.CODEC
    ).xmap(FishingRedstoneData::new, data -> data.activeTargets);

    private static final SavedDataType<FishingRedstoneData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(DeathTotemMod.MOD_ID, "fishing_redstone_tracker"),
            FishingRedstoneData::new,
            CODEC,
            DataFixTypes.PLAYER
    );

    public FishingRedstoneData() {
        this.activeTargets = new HashMap<>();
    }

    public FishingRedstoneData(Map<UUID, TargetState> map) {
        this.activeTargets = new HashMap<>(map);
    }

    public static FishingRedstoneData getServerState(ServerLevel world) {
        return world.getServer().overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public void setTarget(UUID playerUuid, BlockPos pos) {
        activeTargets.put(playerUuid, new TargetState(pos, false));
        setDirty();
    }

    public TargetState getTarget(UUID playerUuid) {
        return activeTargets.get(playerUuid);
    }

    public void removeTarget(UUID playerUuid) {
        if (activeTargets.remove(playerUuid) != null) {
            setDirty();
        }
    }

    public void markArmed(UUID playerUuid) {
        TargetState state = activeTargets.get(playerUuid);
        if (state != null) {
            state.isArmed = true;
            setDirty();
        }
    }
}