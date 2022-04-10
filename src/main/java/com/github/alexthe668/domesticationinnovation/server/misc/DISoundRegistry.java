package com.github.alexthe668.domesticationinnovation.server.misc;

import com.github.alexthe668.domesticationinnovation.DomesticationMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Field;

@Mod.EventBusSubscriber(modid = DomesticationMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DISoundRegistry {

    public static final SoundEvent COLLAR_TAG = createSoundEvent("collar_tag");
    public static final SoundEvent MAGNET_LOOP = createSoundEvent("magnet_loop");
    public static final SoundEvent CHAIN_LIGHTNING = createSoundEvent("chain_lightning");
    public static final SoundEvent GIANT_BUBBLE_INFLATE = createSoundEvent("giant_bubble_inflate");
    public static final SoundEvent GIANT_BUBBLE_POP = createSoundEvent("giant_bubble_pop");
    public static final SoundEvent PET_BED_USE = createSoundEvent("pet_bed_use");
    public static final SoundEvent DRUM = createSoundEvent("drum");

    private static SoundEvent createSoundEvent(final String soundName) {
        final ResourceLocation soundID = new ResourceLocation(DomesticationMod.MODID, soundName);
        return new SoundEvent(soundID).setRegistryName(soundID);
    }

    @SubscribeEvent
    public static void registerSoundEvents(final RegistryEvent.Register<SoundEvent> event) {
        try {
            for (Field f : DISoundRegistry.class.getDeclaredFields()) {
                Object obj = f.get(null);
                if (obj instanceof SoundEvent) {
                    event.getRegistry().register((SoundEvent) obj);
                } else if (obj instanceof SoundEvent[]) {
                    for (SoundEvent soundEvent : (SoundEvent[]) obj) {
                        event.getRegistry().register(soundEvent);
                    }
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
