package net.pixeldreamstudios.spw.component;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.pixeldreamstudios.spw.SpellPoweredWeapons;

public final class SpwComponents {
    private SpwComponents() {}

    public static final String DAMAGE_CONVERSION_ID = "damage_conversion";

    public static final String HIDE_DAMAGE_LINE_ID = "hide_damage_line";

    public static final String SUPPRESS_PHYSICAL_ID = "suppress_physical";

    public static DataComponentType<DamageConversion> DAMAGE_CONVERSION;

    public static DataComponentType<Boolean> HIDE_DAMAGE_LINE;

    public static DataComponentType<Boolean> SUPPRESS_PHYSICAL;

    public static DataComponentType<DamageConversion> buildDamageConversion() {
        return DataComponentType.<DamageConversion>builder()
                .persistent(DamageConversion.CODEC)
                .networkSynchronized(DamageConversion.STREAM_CODEC)
                .build();
    }

    public static DataComponentType<Boolean> buildFlag() {
        return DataComponentType.<Boolean>builder()
                .persistent(Codec.BOOL)
                .networkSynchronized(ByteBufCodecs.BOOL)
                .build();
    }

    public static void register() {
        DAMAGE_CONVERSION = Registry.register(
                BuiltInRegistries.DATA_COMPONENT_TYPE,
                SpellPoweredWeapons.id(DAMAGE_CONVERSION_ID),
                buildDamageConversion());
        HIDE_DAMAGE_LINE = Registry.register(
                BuiltInRegistries.DATA_COMPONENT_TYPE,
                SpellPoweredWeapons.id(HIDE_DAMAGE_LINE_ID),
                buildFlag());
        SUPPRESS_PHYSICAL = Registry.register(
                BuiltInRegistries.DATA_COMPONENT_TYPE,
                SpellPoweredWeapons.id(SUPPRESS_PHYSICAL_ID),
                buildFlag());
    }
}
