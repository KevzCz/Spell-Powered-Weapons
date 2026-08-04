package net.pixeldreamstudios.spw.damage;

import net.minecraft.world.item.ItemStack;

public interface FiredFromWeapon {
    ItemStack spw$getFiredFrom();

    void spw$setFiredFrom(ItemStack weapon);
}
