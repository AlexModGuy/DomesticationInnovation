package com.github.alexthe668.domesticationinnovation.server.item;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

public class InventoryOnlyItem extends Item implements CustomTabBehavior {

    public InventoryOnlyItem(Properties properties) {
        super(properties);
    }

    @Override
    public void fillItemCategory(CreativeModeTab.Output contents) {

    }
}
