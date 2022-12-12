package com.github.alexthe668.domesticationinnovation.client;

import com.github.alexthe666.citadel.client.event.EventGetOutlineColor;
import com.github.alexthe668.domesticationinnovation.DomesticationMod;
import com.github.alexthe668.domesticationinnovation.client.particle.*;
import com.github.alexthe668.domesticationinnovation.client.render.*;
import com.github.alexthe668.domesticationinnovation.server.CommonProxy;
import com.github.alexthe668.domesticationinnovation.server.entity.*;
import com.github.alexthe668.domesticationinnovation.server.item.DIItemRegistry;
import com.github.alexthe668.domesticationinnovation.server.item.DeedOfOwnershipItem;
import com.github.alexthe668.domesticationinnovation.server.item.FeatherOnAStickItem;
import com.github.alexthe668.domesticationinnovation.server.misc.DIParticleRegistry;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.*;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientProxy extends CommonProxy {

    public static final Map<Integer, DiscJockeySound> DISC_JOCKEY_SOUND_MAP = new HashMap<>();
    public static Map<Entity, int[]> shadowPunchRenderData = new HashMap<>();

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        List<EntityType<? extends LivingEntity>> entityTypes = ImmutableList.copyOf(
                ForgeRegistries.ENTITY_TYPES.getValues().stream()
                        .filter(LayerManager::canApply)
                        .filter(DefaultAttributes::hasSupplier)
                        .map(entityType -> (EntityType<? extends LivingEntity>) entityType)
                        .collect(Collectors.toList()));
        entityTypes.forEach((entityType -> {
            LayerManager.addLayerIfApplicable(entityType, event);
        }));
    }

    public static float getNametagOffset() {
        return ModList.get().isLoaded("neat") ? 0.5F : 0;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void init() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientProxy::onAddLayers);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientProxy::setupParticles);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void clientInit() {
        EntityRenderers.register(DIEntityRegistry.CHAIN_LIGHTNING.get(), ChainLightningRender::new);
        EntityRenderers.register(DIEntityRegistry.RECALL_BALL.get(), RecallBallRender::new);
        EntityRenderers.register(DIEntityRegistry.FEATHER.get(), RenderFeather::new);
        EntityRenderers.register(DIEntityRegistry.GIANT_BUBBLE.get(), RenderGiantBubble::new);
        EntityRenderers.register(DIEntityRegistry.FOLLOWING_JUKEBOX.get(), RenderJukeboxFollower::new);
        EntityRenderers.register(DIEntityRegistry.HIGHLIGHTED_BLOCK.get(), RenderHighlightedBlock::new);
        EntityRenderers.register(DIEntityRegistry.PSYCHIC_WALL.get(), RenderPsychicWall::new);
        ItemProperties.register(DIItemRegistry.FEATHER_ON_A_STICK.get(), new ResourceLocation("cast"), (stack, lvl, holder, i) -> {
            if (holder == null) {
                return 0.0F;
            } else {
                boolean flag = holder.getMainHandItem() == stack;
                boolean flag1 = holder.getOffhandItem() == stack;
                if (holder.getMainHandItem().getItem() instanceof FeatherOnAStickItem) {
                    flag1 = false;
                }
                return (flag || flag1) && holder instanceof Player && ((Player) holder).fishing instanceof FeatherEntity ? 1.0F : 0.0F;
            }
        });
        ItemProperties.register(DIItemRegistry.DEED_OF_OWNERSHIP.get(), new ResourceLocation("bound"), (stack, lvl, holder, i) -> {
            return DeedOfOwnershipItem.isBound(stack) ? 1 : 0;
        });
    }

    public static void setupParticles(RegisterParticleProvidersEvent event) {
        DomesticationMod.LOGGER.debug("Registered particle factories");
        event.register(DIParticleRegistry.DEFLECTION_SHIELD.get(), new ParticleDeflectionShield.Factory());
        event.register(DIParticleRegistry.MAGNET.get(), ParticleMagnet.Factory::new);
        event.register(DIParticleRegistry.ZZZ.get(), ParticleZZZ.Factory::new);
        event.register(DIParticleRegistry.GIANT_POP.get(), ParticleGiantPop.Factory::new);
        event.register(DIParticleRegistry.SIMPLE_BUBBLE.get(), ParticleSimpleBubble.Factory::new);
        event.register(DIParticleRegistry.VAMPIRE.get(), ParticleVampire.Factory::new);
        event.register(DIParticleRegistry.SNIFF.get(), ParticleSniff.Factory::new);
        event.register(DIParticleRegistry.PSYCHIC_WALL.get(), ParticlePsychicWall.Factory::new);
        event.register(DIParticleRegistry.INTIMIDATION.get(), new ParticleIntimidation.Factory());
        event.register(DIParticleRegistry.BLIGHT.get(), ParticleBlight.Factory::new);
        event.register(DIParticleRegistry.LANTERN_BUGS.get(), ParticleLanternBugs.Factory::new);
    }

    @SubscribeEvent
    public void onOutlineColor(EventGetOutlineColor event) {
        if(event.getEntityIn() instanceof HighlightedBlockEntity){
            event.setColor(OreColorRegistry.getBlockColor(((HighlightedBlockEntity) event.getEntityIn()).getBlockState()));
            event.setResult(Event.Result.ALLOW);
        }
    }

    @SubscribeEvent
    public void renderNametagEvent(RenderNameTagEvent event) {
        //Component colored = event.getOriginalContent().copy().withStyle(ChatFormatting.BLUE);
        //event.setContent(colored);
        if (TameableUtils.isTamed(event.getEntity()) && TameableUtils.isPetOf(Minecraft.getInstance().player, event.getEntity()) && TameableUtils.hasAnyEnchants((LivingEntity) event.getEntity()) && Minecraft.getInstance().player.isShiftKeyDown()) {
            event.setResult(Event.Result.DENY);
            renderNametagEnchantments(event.getEntity(), event.getContent(), event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight());
        }
    }

    @SubscribeEvent
    public void onAttackEntityFromClientEvent(InputEvent.InteractionKeyMappingTriggered event) {
        if (event.isAttack() && DomesticationMod.CONFIG.swingThroughPets.get() && !Minecraft.getInstance().player.isShiftKeyDown() && Minecraft.getInstance().hitResult instanceof EntityHitResult && TameableUtils.isPetOf(Minecraft.getInstance().player, ((EntityHitResult) Minecraft.getInstance().hitResult).getEntity())) {
            event.setCanceled(true);
            event.setSwingHand(true);
            Player player = Minecraft.getInstance().player;
            Vec3 vec3 = player.getEyePosition(1.0F);
            Vec3 vec31 = player.getViewVector(1.0F);
            double d0 = (double) Minecraft.getInstance().gameMode.getPickRange() + 1.5D;
            double d1 = Minecraft.getInstance().hitResult.getLocation().distanceToSqr(vec3) + 8.0D;
            Vec3 vec32 = vec3.add(vec31.x * d0, vec31.y * d0, vec31.z * d0);
            AABB aabb = player.getBoundingBox().expandTowards(vec31.scale(d0)).inflate(1.0D, 1.0D, 1.0D);
            EntityHitResult entityhitresult = ProjectileUtil.getEntityHitResult(player, vec3, vec32, aabb, (entity) -> {
                return !entity.isSpectator() && entity.isPickable() && !TameableUtils.isPetOf(player, entity);
            }, d1);
            if (entityhitresult != null) {
                Minecraft.getInstance().gameMode.attack(player, entityhitresult.getEntity());
            }
        }
    }

    private void renderNametagEnchantments(Entity entity, Component nameTag, PoseStack pose, MultiBufferSource buffer, int lightIn) {
        if (Minecraft.getInstance().player.isShiftKeyDown() && TameableUtils.isTamed(entity) && TameableUtils.hasAnyEnchants((LivingEntity) entity)) {
            LivingEntity living = (LivingEntity) entity;
            List<Component> list = TameableUtils.getEnchantDescriptions(living);
            double d0 = Minecraft.getInstance().getEntityRenderDispatcher().distanceToSqr(entity);
            if (net.minecraftforge.client.ForgeHooksClient.isNameplateInRenderDistance(entity, d0)) {
                if (nameTag instanceof MutableComponent) {
                    int health = Math.round(living.getHealth());
                    int maxHealth = Math.round(living.getMaxHealth());
                    nameTag = ((MutableComponent) nameTag).append(" (" + health + "/" + maxHealth + ")");
                }
                Font font = Minecraft.getInstance().font;
                boolean flag = !entity.isDiscrete();
                float f = entity.getBbHeight() + 0.5F;
                int i = -10 * list.size();
                pose.pushPose();
                pose.translate(0.0D, f + getNametagOffset(), 0.0D);
                pose.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
                pose.scale(-0.025F, -0.025F, 0.025F);

                float f3 = !list.isEmpty() ? (float) (-font.width(list.get(0)) / 2) : (float) (-font.width(nameTag) / 2);
                pose.pushPose();
                pose.translate(f3 + 12, (-10 * list.size()) + 16, 0);
                pose.mulPose(Axis.XP.rotationDegrees(180.0F));
                pose.scale(22F, 22F, 22F);
                Minecraft.getInstance().getItemRenderer().renderStatic(new ItemStack(DIItemRegistry.COLLAR_TAG.get()), ItemTransforms.TransformType.GROUND, lightIn, OverlayTexture.NO_OVERLAY, pose, buffer, entity.getId());
                pose.popPose();

                Matrix4f matrix4f = pose.last().pose();
                float f1 = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
                int j = (int) (f1 * 255.0F) << 24;
                float f2 = (float) (-font.width(nameTag) / 2);
                font.drawInBatch(nameTag, f2, (float) i - 0.25F, 553648127, false, matrix4f, buffer, flag, j, lightIn);
                if (flag) {
                    font.drawInBatch(nameTag, f2, (float) i - 0.25F, -1, false, matrix4f, buffer, false, 0, lightIn);
                }
                pose.pushPose();
                pose.scale(0.8F, 0.8F, 0.8F);
                matrix4f = pose.last().pose();
                for (int k = 0; k < list.size(); k++) {
                    float f4 = (float) (-font.width(list.get(k)) / 2);
                    font.drawInBatch(list.get(k), f4, i * 1.25F + k * 10 + 12, 553648127, false, matrix4f, buffer, flag, j, lightIn);
                    if (flag) {
                        font.drawInBatch(list.get(k), f4, i * 1.25F + k * 10 + 12, -1, false, matrix4f, buffer, false, 0, lightIn);
                    }
                }
                pose.popPose();
                pose.popPose();
            }
        }
    }

    public void updateVisualDataForMob(Entity entity, int[] arr) {
        shadowPunchRenderData.put(entity, arr);
    }

    @OnlyIn(Dist.CLIENT)
    public void updateEntityStatus(Entity entity, byte updateKind) {
        if (entity instanceof FollowingJukeboxEntity) {
            SoundEvent record = ((FollowingJukeboxEntity) entity).getRecordSound();
            if (entity.isAlive() && updateKind == 66) {
                DiscJockeySound sound;
                if (record != null && (DISC_JOCKEY_SOUND_MAP.get(entity.getId()) == null || DISC_JOCKEY_SOUND_MAP.get(entity.getId()).getRecordSound() != record)) {
                    sound = new DiscJockeySound(record, (FollowingJukeboxEntity) entity);
                    DISC_JOCKEY_SOUND_MAP.put(entity.getId(), sound);
                } else {
                    sound = DISC_JOCKEY_SOUND_MAP.get(entity.getId());
                }
                if (sound != null && !Minecraft.getInstance().getSoundManager().isActive(sound) && sound.canPlaySound() && sound.isNearest()) {
                    Minecraft.getInstance().getSoundManager().play(sound);
                }
            }
            if (updateKind == 67 || record == null) {
                if (DISC_JOCKEY_SOUND_MAP.containsKey(entity.getId())) {
                    DiscJockeySound sound = DISC_JOCKEY_SOUND_MAP.get(entity.getId());
                    DISC_JOCKEY_SOUND_MAP.remove(entity.getId());
                    Minecraft.getInstance().getSoundManager().stop(sound);
                }
            }
        }
    }


}