package com.github.alexthe668.domesticationinnovation.server.item;

import com.github.alexthe668.domesticationinnovation.DomesticationMod;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class CollarTagItem extends Item {

    public CollarTagItem() {
        super(new Item.Properties().tab(DomesticationMod.TAB));
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantmentValue() {
        return 1;
    }
}
