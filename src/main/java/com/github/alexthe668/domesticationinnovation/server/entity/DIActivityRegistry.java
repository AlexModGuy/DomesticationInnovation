package com.github.alexthe668.domesticationinnovation.server.entity;

import com.github.alexthe668.domesticationinnovation.DomesticationMod;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class DIActivityRegistry {

    public static final DeferredRegister<Activity> DEF_REG = DeferredRegister.create(ForgeRegistries.ACTIVITIES, DomesticationMod.MODID);
    public static final RegistryObject<Activity> AXOLOTL_FOLLOW = DEF_REG.register("axolotl_follow", () -> new Activity("axolotl_follow"));
    public static final RegistryObject<Activity> AXOLOTL_STAY = DEF_REG.register("axolotl_stay", () -> new Activity("axolotl_stay"));
}
