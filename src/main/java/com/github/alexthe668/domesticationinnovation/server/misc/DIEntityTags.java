package com.github.alexthe668.domesticationinnovation.server.misc;

import com.github.alexthe668.domesticationinnovation.DomesticationMod;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public class DIEntityTags {
    public static final TagKey<EntityType<?>> PETSTORE_FISHTANK = register("petstore_fishtank");
    public static final TagKey<EntityType<?>> PETSTORE_CAGE_0 = register("petstore_cage_0");
    public static final TagKey<EntityType<?>> PETSTORE_CAGE_1 = register("petstore_cage_1");
    public static final TagKey<EntityType<?>> PETSTORE_CAGE_2 = register("petstore_cage_2");
    public static final TagKey<EntityType<?>> PETSTORE_CAGE_3 = register("petstore_cage_3");

    private static TagKey<EntityType<?>> register(String name) {
        return TagKey.create(Registry.ENTITY_TYPE_REGISTRY, new ResourceLocation(DomesticationMod.MODID, name));
    }
}