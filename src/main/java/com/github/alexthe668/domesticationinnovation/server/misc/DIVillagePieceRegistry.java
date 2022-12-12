package com.github.alexthe668.domesticationinnovation.server.misc;

import com.github.alexthe666.citadel.server.generation.VillageHouseManager;
import com.github.alexthe668.domesticationinnovation.DomesticationMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class DIVillagePieceRegistry {

    public static final DeferredRegister<StructurePoolElementType<?>> DEF_REG = DeferredRegister.create(Registries.STRUCTURE_POOL_ELEMENT, DomesticationMod.MODID);

    public static final RegistryObject<StructurePoolElementType<PetshopStructurePoolElement>> PETSHOP = DEF_REG.register("petshop", () -> () -> PetshopStructurePoolElement.CODEC);

    public static void registerHouses() {
        int weight = 17;
        StructurePoolElement plains = new PetshopStructurePoolElement(new ResourceLocation(DomesticationMod.MODID, "plains_petshop"), StructurePoolElement.EMPTY);
        VillageHouseManager.register(new ResourceLocation("minecraft:village/plains/houses"), (pool) -> VillageHouseManager.addToPool(pool, plains, weight));
        StructurePoolElement desert = new PetshopStructurePoolElement(new ResourceLocation(DomesticationMod.MODID, "desert_petshop"), StructurePoolElement.EMPTY);
        VillageHouseManager.register(new ResourceLocation("minecraft:village/desert/houses"), (pool) -> VillageHouseManager.addToPool(pool, desert, weight));
        StructurePoolElement savanna = new PetshopStructurePoolElement(new ResourceLocation(DomesticationMod.MODID, "savanna_petshop"), StructurePoolElement.EMPTY);
        VillageHouseManager.register(new ResourceLocation("minecraft:village/savanna/houses"), (pool) -> VillageHouseManager.addToPool(pool, savanna, weight));
        StructurePoolElement snowy = new PetshopStructurePoolElement(new ResourceLocation(DomesticationMod.MODID, "snowy_petshop"), StructurePoolElement.EMPTY);
        VillageHouseManager.register(new ResourceLocation("minecraft:village/snowy/houses"), (pool) -> VillageHouseManager.addToPool(pool, snowy, weight));
        StructurePoolElement taiga = new PetshopStructurePoolElement(new ResourceLocation(DomesticationMod.MODID, "taiga_petshop"), StructurePoolElement.EMPTY);
        VillageHouseManager.register(new ResourceLocation("minecraft:village/taiga/houses"), (pool) -> VillageHouseManager.addToPool(pool, taiga, weight));
    }

}
