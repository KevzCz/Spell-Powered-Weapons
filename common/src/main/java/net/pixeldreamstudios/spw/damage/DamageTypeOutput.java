package net.pixeldreamstudios.spw.damage;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;

public final class DamageTypeOutput {
    private DamageTypeOutput() {}

    public static DamageSource source(LivingEntity attacker, String typeId) {
        if (attacker == null) {
            return null;
        }
        var registry = attacker.level().registryAccess().registry(Registries.DAMAGE_TYPE).orElse(null);
        if (registry == null) {
            return null;
        }

        Holder<DamageType> type = null;
        if (typeId != null && !typeId.isBlank()) {
            ResourceLocation id = ResourceLocation.tryParse(typeId);
            if (id != null) {
                type = registry.getHolder(ResourceKey.create(Registries.DAMAGE_TYPE, id)).orElse(null);
            }
        }
        if (type == null) {
            type = registry.getHolderOrThrow(DamageTypes.GENERIC);
        }
        return new DamageSource(type, attacker, attacker);
    }
}
