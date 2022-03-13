package com.github.alexthe668.domesticationinnovation.server.item;

import com.github.alexthe668.domesticationinnovation.DomesticationMod;
import com.github.alexthe668.domesticationinnovation.server.block.DIBlockRegistry;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Objects;

@Mod.EventBusSubscriber(modid = DomesticationMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DIItemRegistry {

    public static final DeferredRegister<Item> DEF_REG = DeferredRegister.create(ForgeRegistries.ITEMS, DomesticationMod.MODID);

    public static final RegistryObject<Item> COLLAR_TAG = DEF_REG.register("collar_tag", () -> new CollarTagItem());
    public static final RegistryObject<Item> FEATHER_ON_A_STICK = DEF_REG.register("feather_on_a_stick", () -> new FeatherOnAStickItem());
    public static final RegistryObject<Item> ROTTEN_APPLE = DEF_REG.register("rotten_apple", () -> new RottenAppleItem());
    public static final RegistryObject<Item> SINISTER_CARROT = DEF_REG.register("sinister_carrot", () -> new SinisterCarrotItem());
    public static final RegistryObject<Item> DEFLECTION_SHIELD = DEF_REG.register("deflection_shield", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> MAGNET = DEF_REG.register("magnet", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> DEED_OF_OWNERSHIP = DEF_REG.register("deed_of_ownership", () -> new DeedOfOwnershipItem());

    @SubscribeEvent
    public static void registerItem(RegistryEvent.Register<Item> event) {
        DIBlockRegistry.DEF_REG.getEntries().stream()
                .map(RegistryObject::get)
                .forEach(block -> {
                    final Item.Properties properties = new Item.Properties().tab(DomesticationMod.TAB);
                    final BlockItem blockItem;
                    blockItem = new BlockItem(block, properties);
                    blockItem.setRegistryName(Objects.requireNonNull(block.getRegistryName()));
                    event.getRegistry().register(blockItem);
                });

    }
}
