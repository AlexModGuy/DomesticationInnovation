package com.github.alexthe668.domesticationinnovation.server.misc;

import com.github.alexthe668.domesticationinnovation.DomesticationMod;
import com.github.alexthe668.domesticationinnovation.server.enchantment.DIEnchantmentRegistry;
import com.github.alexthe668.domesticationinnovation.server.item.DIItemRegistry;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.lang.reflect.Field;

public class DICreativeModeTab extends CreativeModeTab {
    public static final CreativeModeTab INSTANCE = new DICreativeModeTab();

    public DICreativeModeTab() {
        super(DomesticationMod.MODID);
    }

    @Override
    public ItemStack makeIcon() {
        return new ItemStack(DIItemRegistry.COLLAR_TAG.get());
    }

    public void fillItemList(NonNullList<ItemStack> items) {
        super.fillItemList(items);
        try {
            for (Field f : DIEnchantmentRegistry.class.getDeclaredFields()) {
                Object obj = f.get(null);
                if (obj instanceof Enchantment) {
                    Enchantment enchant = (Enchantment)obj;
                    if(enchant.isAllowedOnBooks() && DomesticationMod.CONFIG.isEnchantEnabled(enchant)){
                        items.add(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchant, enchant.getMaxLevel())));
                    }
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
