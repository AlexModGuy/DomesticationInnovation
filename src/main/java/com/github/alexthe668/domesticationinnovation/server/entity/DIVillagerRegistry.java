package com.github.alexthe668.domesticationinnovation.server.entity;

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

    public static final Map<String, ResourceLocation> REPLACE_POOLS = ImmutableMap.<String, ResourceLocation>builder()
            .put("minecraft:village/plains/houses", new ResourceLocation(DomesticationMod.MODID, "plains_petshop"))
            .put("minecraft:village/desert/houses", new ResourceLocation(DomesticationMod.MODID, "desert_petshop"))
            .put("minecraft:village/savanna/houses", new ResourceLocation(DomesticationMod.MODID, "savanna_petshop"))
            .put("minecraft:village/snowy/houses", new ResourceLocation(DomesticationMod.MODID, "snowy_petshop"))
            .put("minecraft:village/taiga/houses", new ResourceLocation(DomesticationMod.MODID, "taiga_petshop"))
            .build();
    public static final StructurePoolElementType<PetshopStructurePoolElement> PETSHOP_TYPE = Registry.register(Registry.STRUCTURE_POOL_ELEMENT, new ResourceLocation(DomesticationMod.MODID, "petshop"), () -> PetshopStructurePoolElement.CODEC);
    public static final VillagerProfession ANIMAL_TAMER = new AnimalTamerProfession().setRegistryName(DomesticationMod.MODID, "animal_tamer");

    @SubscribeEvent
    public static void registerVillagers(final RegistryEvent.Register<VillagerProfession> event) {
        if(DomesticationMod.CONFIG.animalTamerVillager.get()){
            event.getRegistry().register(ANIMAL_TAMER);
        }
    }

   /*  public static void registerHouses(MinecraftServer server) {
       RegistryAccess.Frozen manager = server.registryAccess();
        Registry<StructureTemplatePool> registry = manager.registryOrThrow(Registry.TEMPLATE_POOL_REGISTRY);
        registerJigsawPiece(registry, new ResourceLocation("minecraft:village/plains/houses"), new ResourceLocation(DomesticationMod.MODID, "plains_petshop"), 20);
        registerJigsawPiece(registry, new ResourceLocation("minecraft:village/desert/houses"), new ResourceLocation(DomesticationMod.MODID, "desert_petshop"), 20);
        registerJigsawPiece(registry, new ResourceLocation("minecraft:village/savanna/houses"), new ResourceLocation(DomesticationMod.MODID, "savanna_petshop"), 20);
        registerJigsawPiece(registry, new ResourceLocation("minecraft:village/snowy/houses"), new ResourceLocation(DomesticationMod.MODID, "snowy_petshop"), 20);
        registerJigsawPiece(registry, new ResourceLocation("minecraft:village/taiga/houses"), new ResourceLocation(DomesticationMod.MODID, "taiga_petshop"), 20);

    }

    private static void registerJigsawPiece(Registry<StructureTemplatePool> registry, ResourceLocation poolLocation, ResourceLocation nbtLocation, int weight) {
        StructureTemplatePool pool = registry.get(poolLocation);
        StructurePoolElement element = new PetshopStructurePoolElement(nbtLocation, ProcessorLists.EMPTY);
        if (pool != null) {
            List<StructurePoolElement> templates = new ArrayList<>(pool.templates);
            for (int i = 0; i < weight; i++) {
                templates.add(element);
            }
            List<Pair<StructurePoolElement, Integer>> rawTemplates = new ArrayList(pool.rawTemplates);
            rawTemplates.add(new Pair<>(element, weight));
            pool.templates = templates;
            pool.rawTemplates = rawTemplates;
        }
    }*/

    public static StructureTemplatePool addToPool(StructureTemplatePool pool, ResourceLocation resourceLocation) {
        int weight = DomesticationMod.CONFIG.petstoreVillageWeight.get();
        if(weight > 0){
            StructurePoolElement element = new PetshopStructurePoolElement(resourceLocation, ProcessorLists.EMPTY);
            if (pool != null) {
                List<StructurePoolElement> templates = new ArrayList<>(pool.templates);
                for (int i = 0; i < weight; i++) {
                    templates.add(element);
                }
                List<Pair<StructurePoolElement, Integer>> rawTemplates = new ArrayList(pool.rawTemplates);
                rawTemplates.add(new Pair<>(element, weight));
                pool.templates = templates;
                pool.rawTemplates = rawTemplates;
            }
        }
        return pool;
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
