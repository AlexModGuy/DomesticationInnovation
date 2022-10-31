package com.github.alexthe668.domesticationinnovation;

import com.github.alexthe668.domesticationinnovation.client.ClientProxy;
import com.github.alexthe668.domesticationinnovation.server.CommonProxy;
import com.github.alexthe668.domesticationinnovation.server.block.DIBlockRegistry;
import com.github.alexthe668.domesticationinnovation.server.block.DITileEntityRegistry;
import com.github.alexthe668.domesticationinnovation.server.enchantment.DIEnchantmentRegistry;
import com.github.alexthe668.domesticationinnovation.server.entity.DIActivityRegistry;
import com.github.alexthe668.domesticationinnovation.server.entity.DIEntityRegistry;
import com.github.alexthe668.domesticationinnovation.server.entity.DIVillagerRegistry;
import com.github.alexthe668.domesticationinnovation.server.item.DIItemRegistry;
import com.github.alexthe668.domesticationinnovation.server.misc.DICreativeModeTab;
import com.github.alexthe668.domesticationinnovation.server.misc.DIPOIRegistry;
import com.github.alexthe668.domesticationinnovation.server.misc.DIParticleRegistry;
import com.github.alexthe668.domesticationinnovation.server.misc.DISoundRegistry;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(DomesticationMod.MODID)
public class DomesticationMod {
    public static final String MODID = "domesticationinnovation";
    public static final Logger LOGGER = LogManager.getLogger();
    public static CommonProxy PROXY = DistExecutor.runForDist(() -> ClientProxy::new, () -> CommonProxy::new);
    public static final DIConfig CONFIG;
    private static final ForgeConfigSpec CONFIG_SPEC;

    static {
        final Pair<DIConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(DIConfig::new);
        CONFIG = specPair.getLeft();
        CONFIG_SPEC = specPair.getRight();
    }

    public DomesticationMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setupClient);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        final ModLoadingContext modLoadingContext = ModLoadingContext.get();
        modLoadingContext.registerConfig(ModConfig.Type.COMMON, CONFIG_SPEC, "domestication-innovation.toml");
        DIItemRegistry.DEF_REG.register(FMLJavaModLoadingContext.get().getModEventBus());
        DIBlockRegistry.DEF_REG.register(FMLJavaModLoadingContext.get().getModEventBus());
        DIEnchantmentRegistry.registerEnchantments(FMLJavaModLoadingContext.get().getModEventBus());
        DITileEntityRegistry.DEF_REG.register(FMLJavaModLoadingContext.get().getModEventBus());
        DIEntityRegistry.DEF_REG.register(FMLJavaModLoadingContext.get().getModEventBus());
        DIPOIRegistry.DEF_REG.register(FMLJavaModLoadingContext.get().getModEventBus());
        DIParticleRegistry.DEF_REG.register(FMLJavaModLoadingContext.get().getModEventBus());
        DIVillagerRegistry.DEF_REG.register(FMLJavaModLoadingContext.get().getModEventBus());
        DISoundRegistry.DEF_REG.register(FMLJavaModLoadingContext.get().getModEventBus());
        DIActivityRegistry.DEF_REG.register(FMLJavaModLoadingContext.get().getModEventBus());
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(PROXY);
        PROXY.init();
    }

    private void setupClient(FMLClientSetupEvent event) {
        PROXY.clientInit();
    }

    private void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> PROXY.serverInit());
    }


}
