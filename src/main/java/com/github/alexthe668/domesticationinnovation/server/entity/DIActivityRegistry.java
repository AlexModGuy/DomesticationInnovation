package com.github.alexthe668.domesticationinnovation.server.entity;

import com.github.alexthe668.domesticationinnovation.DomesticationMod;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DomesticationMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DIActivityRegistry {

    public static final Activity AXOLOTL_FOLLOW = new Activity("axolotl_follow");
    public static final Activity AXOLOTL_STAY = new Activity("axolotl_stay");

    @SubscribeEvent
    public static void registerActivities(RegistryEvent.Register<Activity> event) {
        event.getRegistry().register(AXOLOTL_FOLLOW.setRegistryName(DomesticationMod.MODID + ":axolotl_follow"));
        event.getRegistry().register(AXOLOTL_STAY.setRegistryName(DomesticationMod.MODID + ":axolotl_stay"));
    }

}
