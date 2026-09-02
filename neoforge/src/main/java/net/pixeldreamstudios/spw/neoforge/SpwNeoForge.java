package net.pixeldreamstudios.spw.neoforge;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.pixeldreamstudios.spw.SpellPoweredWeapons;
import net.pixeldreamstudios.spw.component.DamageConversion;
import net.pixeldreamstudios.spw.component.SpwComponents;

@Mod(SpellPoweredWeapons.MOD_ID)
public final class SpwNeoForge {

    private static final DeferredRegister<DataComponentType<?>> COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, SpellPoweredWeapons.MOD_ID);

    private static final DeferredHolder<DataComponentType<?>, DataComponentType<DamageConversion>>
            DAMAGE_CONVERSION = COMPONENTS.register(
                    SpwComponents.DAMAGE_CONVERSION_ID, SpwComponents::buildDamageConversion);

    private static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>>
            HIDE_DAMAGE_LINE = COMPONENTS.register(
                    SpwComponents.HIDE_DAMAGE_LINE_ID, SpwComponents::buildFlag);

    private static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>>
            SUPPRESS_PHYSICAL = COMPONENTS.register(
                    SpwComponents.SUPPRESS_PHYSICAL_ID, SpwComponents::buildFlag);

    public SpwNeoForge(IEventBus modBus) {
        COMPONENTS.register(modBus);

        modBus.addListener(RegisterEvent.class, event -> {
            if (event.getRegistryKey().equals(Registries.DATA_COMPONENT_TYPE)) {
                SpwComponents.DAMAGE_CONVERSION = DAMAGE_CONVERSION.get();
                SpwComponents.HIDE_DAMAGE_LINE = HIDE_DAMAGE_LINE.get();
                SpwComponents.SUPPRESS_PHYSICAL = SUPPRESS_PHYSICAL.get();
            }
        });

        SpellPoweredWeapons.init();
    }
}
