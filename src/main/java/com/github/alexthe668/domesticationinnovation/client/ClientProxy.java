package com.github.alexthe668.domesticationinnovation.client;

import com.github.alexthe668.domesticationinnovation.DomesticationMod;
import com.github.alexthe668.domesticationinnovation.client.particle.*;
import com.github.alexthe668.domesticationinnovation.client.render.*;
import com.github.alexthe668.domesticationinnovation.server.CommonProxy;
import com.github.alexthe668.domesticationinnovation.server.entity.DIEntityRegistry;
import com.github.alexthe668.domesticationinnovation.server.entity.FeatherEntity;
import com.github.alexthe668.domesticationinnovation.server.entity.TameableUtils;
import com.github.alexthe668.domesticationinnovation.server.item.DIItemRegistry;
import com.github.alexthe668.domesticationinnovation.server.item.DeedOfOwnershipItem;
import com.github.alexthe668.domesticationinnovation.server.item.FeatherOnAStickItem;
import com.github.alexthe668.domesticationinnovation.server.misc.DIParticleRegistry;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
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
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderNameplateEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.stream.Collectors;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientProxy extends CommonProxy {

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        List<EntityType<? extends LivingEntity>> entityTypes = ImmutableList.copyOf(
                ForgeRegistries.ENTITIES.getValues().stream()
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
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void clientInit() {
        EntityRenderers.register(DIEntityRegistry.CHAIN_LIGHTNING.get(), ChainLightningRender::new);
        EntityRenderers.register(DIEntityRegistry.RECALL_BALL.get(), RecallBallRender::new);
        EntityRenderers.register(DIEntityRegistry.FEATHER.get(), RenderFeather::new);
        EntityRenderers.register(DIEntityRegistry.GIANT_BUBBLE.get(), RenderGiantBubble::new);
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

    @Override
    public void setupParticles() {
        DomesticationMod.LOGGER.debug("Registered particle factories");
        Minecraft.getInstance().particleEngine.register(DIParticleRegistry.DEFLECTION_SHIELD, new ParticleDeflectionShield.Factory());
        Minecraft.getInstance().particleEngine.register(DIParticleRegistry.MAGNET, ParticleMagnet.Factory::new);
        Minecraft.getInstance().particleEngine.register(DIParticleRegistry.ZZZ, ParticleZZZ.Factory::new);
        Minecraft.getInstance().particleEngine.register(DIParticleRegistry.GIANT_POP, ParticleGiantPop.Factory::new);
        Minecraft.getInstance().particleEngine.register(DIParticleRegistry.SIMPLE_BUBBLE, ParticleSimpleBubble.Factory::new);
        Minecraft.getInstance().particleEngine.register(DIParticleRegistry.VAMPIRE, ParticleVampire.Factory::new);
    }

    @SubscribeEvent
    public void renderNametagEvent(RenderNameplateEvent event) {
        if (TameableUtils.isTamed(event.getEntity()) && TameableUtils.isPetOf(Minecraft.getInstance().player, event.getEntity()) && TameableUtils.hasAnyEnchants((LivingEntity) event.getEntity()) && Minecraft.getInstance().player.isShiftKeyDown()) {
            event.setResult(Event.Result.DENY);
            renderNameTagStuffFor(event.getEntity(), event.getContent(), event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight());
        }
    }

    @SubscribeEvent
    public void onAttackEntityFromClientEvent(InputEvent.ClickInputEvent event) {
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

    private void renderNameTagStuffFor(Entity entity, Component nameTag, PoseStack pose, MultiBufferSource buffer, int lightIn) {
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
                pose.mulPose(Vector3f.XP.rotationDegrees(180.0F));
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
}
