package net.pixeldreamstudios.spw.roll;

import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

public final class RollReloadListener extends SimplePreparableReloadListener<Void> {

    public static final RollReloadListener INSTANCE = new RollReloadListener();

    private RollReloadListener() {}

    @Override
    protected Void prepare(ResourceManager manager, ProfilerFiller profiler) {
        return null;
    }

    @Override
    protected void apply(Void unused, ResourceManager manager, ProfilerFiller profiler) {
        RollTables.reload(manager);
    }
}
