package com.github.alexthe668.domesticationinnovation.server.entity;

import com.github.alexthe666.citadel.server.generation.VillageHouseManager;
import com.github.alexthe668.domesticationinnovation.DomesticationMod;
import com.github.alexthe668.domesticationinnovation.server.misc.DIPOIRegistry;
import com.github.alexthe668.domesticationinnovation.server.misc.DISoundRegistry;
import com.github.alexthe668.domesticationinnovation.server.misc.PetshopStructurePoolElement;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.ProcessorLists;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import java.util.function.Predicate;

public class DIVillagerRegistry {

    public static final DeferredRegister<VillagerProfession> DEF_REG = DeferredRegister.create(ForgeRegistries.VILLAGER_PROFESSIONS, DomesticationMod.MODID);
    public static final StructurePoolElementType<PetshopStructurePoolElement> PETSHOP_TYPE = Registry.register(Registry.STRUCTURE_POOL_ELEMENT, new ResourceLocation(DomesticationMod.MODID, "petshop"), () -> PetshopStructurePoolElement.CODEC);

    public static final RegistryObject<VillagerProfession> ANIMAL_TAMER = DEF_REG.register("animal_tamer", () -> buildVillagerProfession());
    public static boolean registeredHouses = false;

    private static VillagerProfession buildVillagerProfession() {
        Predicate<Holder<PoiType>> heldJobSite = (poiType) -> {
            return poiType.is(DIPOIRegistry.PET_BED.getKey());
        };
        Predicate<Holder<PoiType>> acquirableJobSite = (poiType) -> {
            return poiType.is(DIPOIRegistry.PET_BED.getKey());
        };
        return new VillagerProfession("animal_tamer", heldJobSite, acquirableJobSite, ImmutableSet.of(), ImmutableSet.of(), DISoundRegistry.PET_BED_USE.get());
    }

    public static void registerHouses() {
        registeredHouses = true;
        final int weight = 17;
        try{
            //weight = DomesticationMod.CONFIG.petstoreVillageWeight.get();
        }catch (Exception e){

        }

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
}
