package net.pixeldreamstudios.spw.damage;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface LaunchRecord {
    ItemStack spw$launcher();

    List<ItemStack> spw$hands();

    boolean spw$claimHit(Entity target);
}
