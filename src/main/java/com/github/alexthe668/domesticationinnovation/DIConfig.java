package com.github.alexthe668.domesticationinnovation;

import com.github.alexthe668.domesticationinnovation.server.enchantment.DIEnchantmentRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.common.ForgeConfigSpec;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class DIConfig {

    public final ForgeConfigSpec.BooleanValue trinaryCommandSystem;
    public final ForgeConfigSpec.BooleanValue tameableAxolotl;
    public final ForgeConfigSpec.BooleanValue tameableHorse;
    public final ForgeConfigSpec.BooleanValue tameableFox;
    public final ForgeConfigSpec.BooleanValue tameableRabbit;
    public final ForgeConfigSpec.BooleanValue swingThroughPets;
    public final ForgeConfigSpec.BooleanValue rottenApple;
    public final ForgeConfigSpec.BooleanValue petBedRespawns;
    public final ForgeConfigSpec.BooleanValue collarTag;
    public final ForgeConfigSpec.BooleanValue rabbitsScareRavagers;
    public final ForgeConfigSpec.BooleanValue animalTamerVillager;
    public final ForgeConfigSpec.IntValue petstoreVillageWeight;
    public final ForgeConfigSpec.DoubleValue sinisterCarrotLootChance;
    public final ForgeConfigSpec.DoubleValue bubblingLootChance;
    public final ForgeConfigSpec.DoubleValue vampirismLootChance;
    public final ForgeConfigSpec.DoubleValue voidCloudLootChance;
    public final ForgeConfigSpec.DoubleValue oreScentingLootChance;

    private final Map<ResourceLocation, ForgeConfigSpec.BooleanValue> enabledEnchantments = new HashMap<>();

    public DIConfig(final ForgeConfigSpec.Builder builder) {
        builder.push("general");
        trinaryCommandSystem = builder.comment("true if wolves, cats, parrots, foxes, axolotls, etc can be set to wander, sit or follow").translation("trinary_command_system").define("trinary_command_system", true);
        tameableAxolotl = builder.comment("true if axolotls are fully tameable (axolotl must be tamed with tropical fish)").translation("tameable_axolotls").define("tameable_axolotls", true);
        tameableHorse = builder.comment("true if horses, donkeys, llamas, etc can be given enchants, beds, etc").translation("tameable_horse").define("tameable_horse", true);
        tameableFox = builder.comment("true if foxes are fully tameable (fox must be tamed via breeding)").translation("tameable_fox").define("tameable_fox", true);
        tameableRabbit = builder.comment("true if rabbits are fully tameable (rabbit must be tamed with carrots)").translation("tameable_rabbit").define("tameable_rabbit", true);
        swingThroughPets = builder.comment("true if attacks do not register on pets from their owners and go through them to attack a mob behind them").translation("swing_through_pets").define("swing_through_pets", true);
        rottenApple = builder.comment("true if apples can turn into rotten apples if they despawn").translation("rotten_apple").define("rotten_apple", true);
        petBedRespawns = builder.comment("true if mobs can respawn in pet beds the next morning after they die").translation("pet_bed_respawns").define("pet_bed_respawns", true);
        collarTag = builder.comment("true if collar tag functionality are enabled. If this is disabled, there is no way to enchant mobs!").translation("collar_tags").define("collar_tags", true);
        rabbitsScareRavagers = builder.comment("true if rabbits scare ravagers like they used to do").translation("rabbits_scare_ravagers").define("rabbits_scare_ravagers", true);
        animalTamerVillager = builder.comment("true if animal tamer villagers are enabled. Their work station is a pet bed").translation("animal_tamer_villager").define("animal_tamer_villager", true);
        petstoreVillageWeight = builder.comment("the spawn weight of the pet store in villages, set to 0 to disable it entirely").translation("petstore_village_weight").defineInRange("petstore_village_weight", 17, 0, 1000);
        builder.pop();
        builder.push("loot");
        sinisterCarrotLootChance = builder.comment("percent chance of woodland mansion loot table containing sinister carrot:").translation("sinister_carrot_loot_chance").defineInRange("sinister_carrot_loot_chance", 0.11D, 0.0, 1.0D);
        bubblingLootChance = builder.comment("percent chance of burried treasure loot table containing Bubbling book:").translation("bubbling_loot_chance").defineInRange("bubbling_loot_chance", 0.65D, 0.0, 1.0D);
        vampirismLootChance = builder.comment("percent chance of woodland mansion loot table containing Vampire book:").translation("vampirism_loot_chance").defineInRange("vampirism_loot_chance", 0.13D, 0.0, 1.0D);
        voidCloudLootChance = builder.comment("percent chance of end city loot table containing Void Cloud book:").translation("void_cloud_loot_chance").defineInRange("void_cloud_loot_chance", 0.13D, 0.0, 1.0D);
        oreScentingLootChance = builder.comment("percent chance of mineshaft loot table containing Ore Scenting book:").translation("ore_scenting_loot_chance").defineInRange("ore_scenting_loot_chance", 0.1D, 0.0, 1.0D);
        builder.pop();
        builder.push("enchantments");
        try {
            for (Field f : DIEnchantmentRegistry.class.getDeclaredFields()) {
                Object obj = f.get(null);
                if (obj instanceof Enchantment) {
                    ResourceLocation registryName = ((Enchantment) obj).getRegistryName();
                    String name = registryName.getPath() + "_enabled";
                    enabledEnchantments.put(registryName, builder.comment("true if " + registryName.getPath().replace("_", " ") + " enchant is enabled, false if disabled").translation(name).define(name, true));
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        builder.pop();
    }

    public boolean isEnchantEnabled(Enchantment enchantment){
        return isEnchantEnabled(enchantment.getRegistryName());
    }

    public boolean isEnchantEnabled(ResourceLocation enchantment){
        ForgeConfigSpec.BooleanValue entry = enabledEnchantments.get(enchantment);
        return entry == null || entry.get();
    }
}
