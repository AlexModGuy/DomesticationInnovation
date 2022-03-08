package com.github.alexthe668.domesticationinnovation.server.enchantment;

import com.github.alexthe668.domesticationinnovation.DomesticationMod;
import net.minecraft.world.item.enchantment.Enchantment;

public class PetEnchantmentTradeOnly extends PetEnchantmentLootOnly {

    protected PetEnchantmentTradeOnly(String name, Rarity r, int levels, int minXP) {
        super(name, r, levels, minXP);
    }

    public boolean isTradeable() {
        return DomesticationMod.CONFIG.isEnchantEnabled(this);
    }
}
