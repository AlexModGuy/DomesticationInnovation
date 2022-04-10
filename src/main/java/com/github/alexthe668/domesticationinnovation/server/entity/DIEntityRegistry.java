package com.github.alexthe668.domesticationinnovation.server.entity;

import com.github.alexthe668.domesticationinnovation.DomesticationMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.lang.reflect.Field;

public class DIEntityRegistry {

    public static final DeferredRegister<EntityType<?>> DEF_REG = DeferredRegister.create(ForgeRegistries.ENTITIES, DomesticationMod.MODID);
    public static final RegistryObject<EntityType<ChainLightningEntity>> CHAIN_LIGHTNING = DEF_REG.register("chain_lightning", () -> build(EntityType.Builder.of(ChainLightningEntity::new, MobCategory.MISC).sized(0.5F, 0.5F).setCustomClientFactory(ChainLightningEntity::new).fireImmune(), "chain_lightning"));
    public static final RegistryObject<EntityType<RecallBallEntity>> RECALL_BALL = DEF_REG.register("recall_ball", () -> build(EntityType.Builder.of(RecallBallEntity::new, MobCategory.MISC).sized(0.8F, 0.8F).setCustomClientFactory(RecallBallEntity::new).fireImmune(), "recall_ball"));
    public static final RegistryObject<EntityType<FeatherEntity>> FEATHER = DEF_REG.register("feather", () -> build(EntityType.Builder.of(FeatherEntity::new, MobCategory.MISC).sized(0.2F, 0.2F).setCustomClientFactory(FeatherEntity::new).fireImmune(), "feather"));
    public static final RegistryObject<EntityType<GiantBubbleEntity>> GIANT_BUBBLE = DEF_REG.register("giant_bubble", () -> build(EntityType.Builder.of(GiantBubbleEntity::new, MobCategory.MISC).sized(1.2F, 1.8F).setCustomClientFactory(GiantBubbleEntity::new).fireImmune(), "giant_bubble"));
    public static final RegistryObject<EntityType<FollowingJukeboxEntity>> FOLLOWING_JUKEBOX = DEF_REG.register("following_jukebox", () -> build(EntityType.Builder.of(FollowingJukeboxEntity::new, MobCategory.MISC).sized(0.65F, 0.65F).setCustomClientFactory(FollowingJukeboxEntity::new).fireImmune(), "following_jukebox"));
    public static final RegistryObject<EntityType<HighlightedBlockEntity>> HIGHLIGHTED_BLOCK = DEF_REG.register("highlighted_block", () -> build(EntityType.Builder.of(HighlightedBlockEntity::new, MobCategory.MISC).sized(1.0F, 1.0F).setCustomClientFactory(HighlightedBlockEntity::new).fireImmune(), "highlighted_block"));

    private static final EntityType build(EntityType.Builder builder, String entityName) {
        ResourceLocation nameLoc = new ResourceLocation(DomesticationMod.MODID, entityName);
        return (EntityType) builder.build(entityName);
    }
}
