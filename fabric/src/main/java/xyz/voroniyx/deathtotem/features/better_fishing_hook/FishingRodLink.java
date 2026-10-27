package xyz.voroniyx.deathtotem.features.better_fishing_hook;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import xyz.voroniyx.deathtotem.DeathTotemMod;

import java.util.Optional;

public record FishingRodLink(ResourceKey<Level> dimension, BlockPos pos, boolean isArmed) {

    private static final String KEY = DeathTotemMod.MOD_ID + ":fishing_rod_link";

    public static final Codec<FishingRodLink> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(FishingRodLink::dimension),
            BlockPos.CODEC.fieldOf("pos").forGetter(FishingRodLink::pos),
            Codec.BOOL.fieldOf("isArmed").forGetter(FishingRodLink::isArmed)
    ).apply(instance, FishingRodLink::new));

    public static Optional<FishingRodLink> get(ItemStack rod) {
        CustomData data = rod.get(DataComponents.CUSTOM_DATA);
        return data == null ? Optional.empty() : data.copyTag().read(KEY, CODEC);
    }

    public static void set(ItemStack rod, FishingRodLink link) {
        CustomData.update(DataComponents.CUSTOM_DATA, rod, tag -> tag.store(KEY, CODEC, link));
    }

    public static void clear(ItemStack rod) {
        CustomData data = rod.get(DataComponents.CUSTOM_DATA);
        if (data != null && data.copyTag().contains(KEY)) {
            CustomData.update(DataComponents.CUSTOM_DATA, rod, tag -> tag.remove(KEY));
        }
    }

    public FishingRodLink armed() {
        return new FishingRodLink(dimension, pos, true);
    }
}
