package com.github.alexthe668.domesticationinnovation.server.enchantment;

import net.minecraft.world.item.enchantment.Enchantment;

public class PetEnchantmentLootOnly extends PetEnchantment {

    protected PetEnchantmentLootOnly(String name, Rarity r, int levels, int minXP) {
        super(name, r, levels, minXP);
    }

    public boolean isTreasureOnly() {
        return true;
    }

    public boolean isTradeable() {
        return false;
    }

    public boolean isDiscoverable() {
        return false;
    }

    protected boolean checkCompatibility(Enchantment enchantment) {
        return this != enchantment && DIEnchantmentRegistry.areCompatible(this, enchantment);
    }

}
