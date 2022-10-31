package com.github.alexthe668.domesticationinnovation.server.misc;

import com.github.alexthe668.domesticationinnovation.DomesticationMod;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.lang.reflect.Field;

public class DIParticleRegistry {

    public static final DeferredRegister<ParticleType<?>> DEF_REG = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, DomesticationMod.MODID);
    public static final RegistryObject<SimpleParticleType> DEFLECTION_SHIELD = DEF_REG.register("deflection_shield", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> MAGNET = DEF_REG.register("magnet", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> ZZZ = DEF_REG.register("zzz", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> GIANT_POP = DEF_REG.register("giant_pop", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> SIMPLE_BUBBLE = DEF_REG.register("simple_bubble", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> VAMPIRE = DEF_REG.register("vampire", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> SNIFF = DEF_REG.register("sniff", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> PSYCHIC_WALL = DEF_REG.register("psychic_wall", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> INTIMIDATION = DEF_REG.register("intimidation", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> BLIGHT = DEF_REG.register("blight", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> LANTERN_BUGS = DEF_REG.register("lantern_bugs", () -> new SimpleParticleType(false));

}
