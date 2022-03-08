package com.github.alexthe668.domesticationinnovation.server.enchantment;

import com.github.alexthe668.domesticationinnovation.DomesticationMod;

public class PetEnchantmentCurse extends PetEnchantment {

    protected PetEnchantmentCurse(String name, Rarity r) {
        super(name, r, 1, 25);
    }

    public int getMaxCost(int cost) {
        return 50;
    }

    public int getMaxLevel() {
        return 1;
    }

    public boolean isTreasureOnly() {
        return DomesticationMod.CONFIG.isEnchantEnabled(this);
    }

    public boolean isCurse() {
        return true;
    }
}
