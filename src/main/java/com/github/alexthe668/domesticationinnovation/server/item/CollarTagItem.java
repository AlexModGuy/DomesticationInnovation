package com.github.alexthe668.domesticationinnovation.server.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class CollarTagItem extends Item {

    public CollarTagItem() {
        super(new Item.Properties());
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
