package com.github.alexthe668.domesticationinnovation.server.misc;

import com.github.alexthe668.domesticationinnovation.DomesticationMod;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Field;

@Mod.EventBusSubscriber(modid = DomesticationMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DIParticleRegistry {

    public static final SimpleParticleType DEFLECTION_SHIELD = (SimpleParticleType) new SimpleParticleType(false).setRegistryName(DomesticationMod.MODID + ":deflection_shield");
    public static final SimpleParticleType MAGNET = (SimpleParticleType) new SimpleParticleType(false).setRegistryName(DomesticationMod.MODID + ":magnet");
    public static final SimpleParticleType ZZZ = (SimpleParticleType) new SimpleParticleType(false).setRegistryName(DomesticationMod.MODID + ":zzz");
    public static final SimpleParticleType GIANT_POP = (SimpleParticleType) new SimpleParticleType(false).setRegistryName(DomesticationMod.MODID + ":giant_pop");
    public static final SimpleParticleType SIMPLE_BUBBLE = (SimpleParticleType) new SimpleParticleType(false).setRegistryName(DomesticationMod.MODID + ":simple_bubble");
    public static final SimpleParticleType VAMPIRE = (SimpleParticleType) new SimpleParticleType(false).setRegistryName(DomesticationMod.MODID + ":vampire");

    @SubscribeEvent
    public static void registerParticles(RegistryEvent.Register<ParticleType<?>> event) {
        try {
            for (Field f : DIParticleRegistry.class.getDeclaredFields()) {
                Object obj = f.get(null);
                if (obj instanceof ParticleType) {
                    event.getRegistry().register((ParticleType) obj);
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
