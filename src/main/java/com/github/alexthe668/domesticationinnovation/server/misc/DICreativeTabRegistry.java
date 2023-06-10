package com.github.alexthe668.domesticationinnovation.server.misc;

import com.github.alexthe668.domesticationinnovation.DomesticationMod;
import com.github.alexthe668.domesticationinnovation.server.enchantment.DIEnchantmentRegistry;
import com.github.alexthe668.domesticationinnovation.server.item.CustomTabBehavior;
import com.github.alexthe668.domesticationinnovation.server.item.DIItemRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.lang.reflect.Field;

public class DICreativeTabRegistry {

    public static final DeferredRegister<CreativeModeTab> DEF_REG = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, DomesticationMod.MODID);

    public static final RegistryObject<CreativeModeTab> TAB = DEF_REG.register(DomesticationMod.MODID, () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup." + DomesticationMod.MODID))
            .icon(() -> new ItemStack(DIItemRegistry.COLLAR_TAG.get()))
            .displayItems((enabledFeatures, output) -> {
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
            })
            .build());

}
