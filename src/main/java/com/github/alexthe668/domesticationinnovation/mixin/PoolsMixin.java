package com.github.alexthe668.domesticationinnovation.mixin;

import com.github.alexthe668.domesticationinnovation.DomesticationMod;
import com.github.alexthe668.domesticationinnovation.server.entity.DIVillagerRegistry;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.data.worldgen.ProcessorLists;
import net.minecraft.data.worldgen.VillagePools;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import org.apache.logging.log4j.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(Pools.class)
public class PoolsMixin {

    @Inject(
            method = {"Lnet/minecraft/data/worldgen/Pools;register(Lnet/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool;)Lnet/minecraft/core/Holder;"},
            remap = true,
            at = @At("HEAD"),
            cancellable = true
    )
    private static void di_register(StructureTemplatePool poolIn, CallbackInfoReturnable<Holder<StructureTemplatePool>> cir) {
        if (!DIVillagerRegistry.registeredHouses) {
            DIVillagerRegistry.registerHouses();
        }
    }
}