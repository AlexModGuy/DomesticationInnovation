package com.github.alexthe668.domesticationinnovation.server.enchantment;

import com.github.alexthe668.domesticationinnovation.DomesticationMod;
import com.github.alexthe668.domesticationinnovation.server.item.DIItemRegistry;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Field;

@Mod.EventBusSubscriber(modid = DomesticationMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DIEnchantmentRegistry {
    public static final EnchantmentCategory CATEGORY = EnchantmentCategory.create("pet", (item -> item == DIItemRegistry.COLLAR_TAG.get()));

    public static final PetEnchantment HEALTH_BOOST = new PetEnchantment("health_boost", Enchantment.Rarity.COMMON, 3, 6);
    public static final PetEnchantment FIREPROOF = new PetEnchantment("fireproof", Enchantment.Rarity.UNCOMMON, 1, 9);
    public static final PetEnchantment IMMUNITY_FRAME = new PetEnchantment("immunity_frame", Enchantment.Rarity.UNCOMMON, 3, 10);
    public static final PetEnchantment DEFLECTION = new PetEnchantment("deflection", Enchantment.Rarity.RARE, 1, 12);
    public static final PetEnchantment POISON_RESISTANCE = new PetEnchantment("poison_resistance", Enchantment.Rarity.COMMON, 1, 6);
    public static final PetEnchantment CHAIN_LIGHTNING = new PetEnchantment("chain_lightning", Enchantment.Rarity.RARE, 2, 15);
    public static final PetEnchantment SPEEDSTER = new PetEnchantment("speedster", Enchantment.Rarity.RARE, 3, 10);
    public static final PetEnchantment FROST_FANG = new PetEnchantment("frost_fang", Enchantment.Rarity.RARE, 1, 11);
    public static final PetEnchantment MAGNETIC = new PetEnchantment("magnetic", Enchantment.Rarity.RARE, 1, 22);
    public static final PetEnchantment LINKED_INVENTORY = new PetEnchantment("linked_inventory", Enchantment.Rarity.COMMON, 1, 22);
    public static final PetEnchantment TOTAL_RECALL = new PetEnchantment("total_recall", Enchantment.Rarity.UNCOMMON, 1, 9);
    public static final PetEnchantment HEALTH_SIPHON = new PetEnchantment("health_siphon", Enchantment.Rarity.RARE, 1, 9);
    public static final PetEnchantment BUBBLING = new PetEnchantmentLootOnly("bubbling", Enchantment.Rarity.VERY_RARE, 2, 13);
    public static final PetEnchantment SHEPHERD = new PetEnchantment("herding", Enchantment.Rarity.UNCOMMON, 2, 10);
    public static final PetEnchantment AMPHIBIOUS = new PetEnchantment("amphibious", Enchantment.Rarity.UNCOMMON, 1, 6);
    public static final PetEnchantment VAMPIRE = new PetEnchantmentLootOnly("vampire", Enchantment.Rarity.VERY_RARE, 2, 20);
    public static final PetEnchantment VOID_CLOUD = new PetEnchantmentLootOnly("void_cloud", Enchantment.Rarity.VERY_RARE, 1, 14);
    public static final PetEnchantment CHARISMA = new PetEnchantmentTradeOnly("charisma", Enchantment.Rarity.VERY_RARE, 3, 9);
    public static final PetEnchantment UNDEAD_CURSE = new PetEnchantmentCurse("undead_curse", Enchantment.Rarity.VERY_RARE);
    public static final PetEnchantment INFAMY_CURSE = new PetEnchantmentCurse("infamy_curse", Enchantment.Rarity.VERY_RARE);

    @SubscribeEvent
    public static void registerEnchantments(final RegistryEvent.Register<Enchantment> event) {
        try {
            for (Field f : DIEnchantmentRegistry.class.getDeclaredFields()) {
                Object obj = f.get(null);
                if (obj instanceof Enchantment && DomesticationMod.CONFIG.isEnchantEnabled((Enchantment) obj)) {
                    event.getRegistry().register((Enchantment) obj);
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean areCompatible(PetEnchantment e1, Enchantment e2) {
        if(e1 == HEALTH_BOOST) {
            return e2 != HEALTH_SIPHON;
        }
        if(e1 == FIREPROOF){
            return e2 != POISON_RESISTANCE && e2 != FROST_FANG && e2 != AMPHIBIOUS;
        }
        if(e1 == IMMUNITY_FRAME){
            return e2 != DEFLECTION;
        }
        if(e1 == DEFLECTION){
            return e2 != IMMUNITY_FRAME;
        }
        if(e1 == POISON_RESISTANCE){
            return e2 != FIREPROOF;
        }
        if(e1 == CHAIN_LIGHTNING){
            return e2 != FROST_FANG && e2 != MAGNETIC && e2 != BUBBLING;
        }
        if(e1 == FROST_FANG){
            return e2 != CHAIN_LIGHTNING && e2 != FIREPROOF && e2 != BUBBLING;
        }
        if(e1 == MAGNETIC){
            return e2 != CHAIN_LIGHTNING && e2 != SHEPHERD && e2 != VAMPIRE;
        }
        if(e1 == TOTAL_RECALL){
            return e2 != UNDEAD_CURSE;
        }
        if(e1 == HEALTH_SIPHON){
            return e2 != HEALTH_BOOST && e2 != VAMPIRE;
        }
        if(e1 == BUBBLING){
            return e2 != CHAIN_LIGHTNING && e2 != FROST_FANG;
        }
        if(e1 == SHEPHERD){
            return e2 != MAGNETIC;
        }
        if(e1 == AMPHIBIOUS){
            return e2 != FIREPROOF;
        }
        if(e1 == VAMPIRE){
            return e2 != MAGNETIC && e2 != HEALTH_SIPHON;
        }
        if(e1 == UNDEAD_CURSE){
            return e2 != TOTAL_RECALL;
        }
        return true;
    }
}
