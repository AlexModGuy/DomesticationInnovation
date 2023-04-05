package com.github.alexthe668.domesticationinnovation.server.misc;

import com.github.alexthe668.domesticationinnovation.DomesticationMod;
import com.github.alexthe668.domesticationinnovation.server.enchantment.DIEnchantmentRegistry;
import com.github.alexthe668.domesticationinnovation.server.item.CustomTabBehavior;
import com.github.alexthe668.domesticationinnovation.server.item.DIItemRegistry;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.registries.RegistryObject;

import java.lang.reflect.Field;

public class DICreativeModeTab {

    public static final ResourceLocation TAB = new ResourceLocation("domesticationinnovation:domesticationinnovation");

    private static ItemStack makeIcon() {
        return new ItemStack(DIItemRegistry.COLLAR_TAG.get());
    }

    public static void registerTab(CreativeModeTabEvent.Register event) {
        event.registerCreativeModeTab(TAB, builder -> builder.title(Component.translatable("itemGroup.domesticationinnovation")).icon(DICreativeModeTab::makeIcon).displayItems((parameters, output) -> {
            for (RegistryObject<Item> item : DIItemRegistry.DEF_REG.getEntries()) {
                if (item.get() instanceof CustomTabBehavior customTabBehavior) {
                    customTabBehavior.fillItemCategory(output);
                } else {
                    output.accept(item.get());
                }
            }
            try {
                for (Field f : DIEnchantmentRegistry.class.getDeclaredFields()) {
                    Object obj = null;
                    obj = f.get(null);
                    if (obj instanceof Enchantment) {
                        Enchantment enchant = (Enchantment) obj;
                        if (enchant.isAllowedOnBooks() && DomesticationMod.CONFIG.isEnchantEnabled(enchant)) {
                            output.accept(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchant, enchant.getMaxLevel())));
                        }
                    }
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }));

    }
}
