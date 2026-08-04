package net.pixeldreamstudios.spw.roll;

import com.mojang.serialization.Codec;
import net.pixeldreamstudios.spw.component.DamageConversion;

import java.util.Locale;

public enum RollMode {
    PLAIN(DamageConversion.Mode.SPLIT),
    SPLIT(DamageConversion.Mode.SPLIT),
    ADDITIVE(DamageConversion.Mode.ADDITIVE),
    FULL_ELEMENTAL(DamageConversion.Mode.SPLIT),
    PROPORTIONAL(DamageConversion.Mode.PROPORTIONAL);

    public static final Codec<RollMode> CODEC = Codec.STRING.xmap(
            raw -> {
                try {
                    return valueOf(raw.toUpperCase(Locale.ROOT));
                } catch (IllegalArgumentException unknown) {
                    return PLAIN;
                }
            },
            mode -> mode.name().toLowerCase(Locale.ROOT));

    private final DamageConversion.Mode componentMode;

    RollMode(DamageConversion.Mode componentMode) {
        this.componentMode = componentMode;
    }

    public DamageConversion.Mode componentMode() {
        return componentMode;
    }

    public boolean convertsPhysical() {
        return this == SPLIT || this == FULL_ELEMENTAL;
    }

    public boolean suppressesPhysical() {
        return this == FULL_ELEMENTAL;
    }

    public boolean producesConversion() {
        return this != PLAIN;
    }
}
