package com.github.alexthe668.domesticationinnovation.server.enchantment;

import com.github.alexthe668.domesticationinnovation.DomesticationMod;
import com.github.alexthe668.domesticationinnovation.server.item.DIItemRegistry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.IModBusEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Field;

public class DIEnchantmentRegistry {
    public static final EnchantmentCategory CATEGORY = EnchantmentCategory.create("pet", (item -> item == DIItemRegistry.COLLAR_TAG.get()));
    public static final DeferredRegister<Enchantment> DEF_REG = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, DomesticationMod.MODID);

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
    public static final PetEnchantment SHADOW_HANDS = new PetEnchantment("shadow_hands", Enchantment.Rarity.VERY_RARE, 4, 10);
    public static final PetEnchantment DISK_JOCKEY = new PetEnchantment("disc_jockey", Enchantment.Rarity.RARE, 1, 8);
    public static final PetEnchantment DEFUSAL = new PetEnchantment("defusal", Enchantment.Rarity.UNCOMMON, 3, 14);
    public static final PetEnchantment WARPING_BITE = new PetEnchantment("warping_bite", Enchantment.Rarity.RARE, 1, 17);
    public static final PetEnchantment ORE_SCENTING = new PetEnchantmentLootOnly("ore_scenting", Enchantment.Rarity.VERY_RARE, 3, 13);
    public static final PetEnchantment GLUTTONOUS = new PetEnchantment("gluttonous", Enchantment.Rarity.COMMON, 1, 9);
    public static final PetEnchantment PSYCHIC_WALL = new PetEnchantment("psychic_wall", Enchantment.Rarity.VERY_RARE, 3, 12);
    public static final PetEnchantment INTIMIDATION = new PetEnchantment("intimidation", Enchantment.Rarity.UNCOMMON, 2, 12);
    public static final PetEnchantment TETHERED_TELEPORT = new PetEnchantment("tethered_teleport", Enchantment.Rarity.COMMON, 1, 6);
    public static final PetEnchantment UNDEAD_CURSE = new PetEnchantmentCurse("undead_curse", Enchantment.Rarity.VERY_RARE);
    public static final PetEnchantment INFAMY_CURSE = new PetEnchantmentCurse("infamy_curse", Enchantment.Rarity.VERY_RARE);
    public static final PetEnchantment BLIGHT_CURSE = new PetEnchantmentCurse("blight_curse", Enchantment.Rarity.VERY_RARE);
    public static final PetEnchantment IMMATURITY_CURSE = new PetEnchantmentCurse("immaturity_curse", Enchantment.Rarity.VERY_RARE);

    public static void registerEnchantments(IEventBus bus) {
        try {
            for (Field f : DIEnchantmentRegistry.class.getDeclaredFields()) {
                Object obj = f.get(null);
                if (obj instanceof PetEnchantment petEnchantment && DomesticationMod.CONFIG.isEnchantEnabled((Enchantment) obj)) {
                    DEF_REG.register(petEnchantment.getName(), () -> petEnchantment);
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        DEF_REG.register(bus);
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
            return e2 != IMMUNITY_FRAME && e2 != DEFUSAL && e2 != PSYCHIC_WALL;
        }
        if(e1 == POISON_RESISTANCE){
            return e2 != FIREPROOF;
        }
        if(e1 == CHAIN_LIGHTNING){
            return e2 != FROST_FANG && e2 != MAGNETIC && e2 != BUBBLING && e2 != SHADOW_HANDS;
        }
        if(e1 == FROST_FANG){
            return e2 != CHAIN_LIGHTNING && e2 != FIREPROOF && e2 != BUBBLING && e2 != WARPING_BITE;
        }
        if(e1 == MAGNETIC){
            return e2 != CHAIN_LIGHTNING && e2 != SHEPHERD && e2 != VAMPIRE && e2 != SHADOW_HANDS && e2 != PSYCHIC_WALL && e2 != INTIMIDATION;
        }
        if(e1 == TOTAL_RECALL){
            return e2 != UNDEAD_CURSE;
        }
        if(e1 == HEALTH_SIPHON){
            return e2 != HEALTH_BOOST && e2 != VAMPIRE && e2 != GLUTTONOUS;
        }
        if(e1 == BUBBLING){
            return e2 != CHAIN_LIGHTNING && e2 != FROST_FANG && e2 != SHADOW_HANDS && e2 != WARPING_BITE;
        }
        if(e1 == SHEPHERD){
            return e2 != MAGNETIC && e2 != ORE_SCENTING && e2 != PSYCHIC_WALL && e2 != BLIGHT_CURSE;
        }
        if(e1 == AMPHIBIOUS){
            return e2 != FIREPROOF;
        }
        if(e1 == VAMPIRE){
            return e2 != MAGNETIC && e2 != HEALTH_SIPHON && e2 != GLUTTONOUS;
        }
        if(e1 == UNDEAD_CURSE){
            return e2 != TOTAL_RECALL;
        }
        if(e1 == SHADOW_HANDS){
            return e2 != CHAIN_LIGHTNING && e2 != MAGNETIC && e2 != BUBBLING && e2 != DISK_JOCKEY;
        }
        if(e1 == DISK_JOCKEY){
            return e2 != SHADOW_HANDS;
        }
        if(e1 == DEFUSAL){
            return e2 != DEFLECTION;
        }
        if(e1 == WARPING_BITE){
            return e2 != FROST_FANG && e2 != BUBBLING;
        }
        if(e1 == ORE_SCENTING){
            return e2 != SHEPHERD;
        }
        if(e1 == GLUTTONOUS){
            return e2 != VAMPIRE && e2 != HEALTH_SIPHON;
        }
        if(e1 == PSYCHIC_WALL){
            return e2 != MAGNETIC && e2 != DEFLECTION && e2 != SHEPHERD;
        }
        if(e1 == INTIMIDATION){
            return e2 != MAGNETIC && e2 != WARPING_BITE;
        }
        if(e1 == BLIGHT_CURSE){
            return e2 != SHEPHERD;
        }
        return true;
    }
}
