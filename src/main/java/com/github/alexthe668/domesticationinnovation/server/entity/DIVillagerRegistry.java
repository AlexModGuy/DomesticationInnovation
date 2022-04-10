package com.github.alexthe668.domesticationinnovation.server.entity;

import com.github.alexthe666.citadel.server.generation.VillageHouseManager;
import com.github.alexthe668.domesticationinnovation.DomesticationMod;
import com.github.alexthe668.domesticationinnovation.server.misc.DIPOIRegistry;
import com.github.alexthe668.domesticationinnovation.server.misc.DISoundRegistry;
import com.github.alexthe668.domesticationinnovation.server.misc.PetshopStructurePoolElement;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.worldgen.ProcessorLists;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.levelgen.structure.pools.LegacySinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = DomesticationMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DIVillagerRegistry {

    public static final StructurePoolElementType<PetshopStructurePoolElement> PETSHOP_TYPE = Registry.register(Registry.STRUCTURE_POOL_ELEMENT, new ResourceLocation(DomesticationMod.MODID, "petshop"), () -> PetshopStructurePoolElement.CODEC);
    public static final VillagerProfession ANIMAL_TAMER = new AnimalTamerProfession().setRegistryName(DomesticationMod.MODID, "animal_tamer");
    public static boolean registeredHouses = false;

    @SubscribeEvent
    public static void registerVillagers(final RegistryEvent.Register<VillagerProfession> event) {
        if(DomesticationMod.CONFIG.animalTamerVillager.get()){
            event.getRegistry().register(ANIMAL_TAMER);
        }
    }

    public static void registerHouses() {
        registeredHouses = true;
        int weight = DomesticationMod.CONFIG.petstoreVillageWeight.get();
        StructurePoolElement plains = new PetshopStructurePoolElement(new ResourceLocation(DomesticationMod.MODID, "plains_petshop"), ProcessorLists.EMPTY);
        VillageHouseManager.register("minecraft:village/plains/houses", (pool) -> VillageHouseManager.addToPool(pool, plains, weight));
        StructurePoolElement desert = new PetshopStructurePoolElement(new ResourceLocation(DomesticationMod.MODID, "desert_petshop"), ProcessorLists.EMPTY);
        VillageHouseManager.register("minecraft:village/desert/houses", (pool) -> VillageHouseManager.addToPool(pool, desert, weight));
        StructurePoolElement savanna = new PetshopStructurePoolElement(new ResourceLocation(DomesticationMod.MODID, "savanna_petshop"), ProcessorLists.EMPTY);
        VillageHouseManager.register("minecraft:village/savanna/houses", (pool) -> VillageHouseManager.addToPool(pool, savanna, weight));
        StructurePoolElement snowy = new PetshopStructurePoolElement(new ResourceLocation(DomesticationMod.MODID, "snowy_petshop"), ProcessorLists.EMPTY);
        VillageHouseManager.register("minecraft:village/snowy/houses", (pool) -> VillageHouseManager.addToPool(pool, snowy, weight));
        StructurePoolElement taiga = new PetshopStructurePoolElement(new ResourceLocation(DomesticationMod.MODID, "taiga_petshop"), ProcessorLists.EMPTY);
        VillageHouseManager.register("minecraft:village/taiga/houses", (pool) -> VillageHouseManager.addToPool(pool, taiga, weight));
    }

    private static class AnimalTamerProfession extends VillagerProfession {

        public AnimalTamerProfession() {
            super("animal_tamer", null, ImmutableSet.of(), ImmutableSet.of(), DISoundRegistry.PET_BED_USE);
        }

        public PoiType getJobPoiType() {
            return DIPOIRegistry.PET_BED.get();
        }

    }

}
