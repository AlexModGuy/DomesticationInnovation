package com.github.alexthe668.domesticationinnovation.client.render;

import com.github.alexthe666.citadel.client.render.LightningBoltData;
import com.github.alexthe666.citadel.client.render.LightningRender;
import com.github.alexthe668.domesticationinnovation.server.enchantment.DIEnchantmentRegistry;
import com.github.alexthe668.domesticationinnovation.server.entity.TameableUtils;
import com.github.alexthe668.domesticationinnovation.server.item.DIItemRegistry;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class LayerPetOverlays extends RenderLayer {

    private static final ItemStack MAGNET = new ItemStack(DIItemRegistry.MAGNET.get());
    private final RenderLayerParent parent;
    private final LightningRender lightningRender = new LightningRender();
    private final LightningBoltData.BoltRenderInfo healthBoltData = new LightningBoltData.BoltRenderInfo(0.3F, 0.0F, 0.0F, 0.0F, new Vector4f(0.4F, 0, 0, 0.4F), 0.2F);
    private static final int CLOUD_COUNT = 14;
    private static final Vec3[] CLOUD_OFFSETS = new Vec3[CLOUD_COUNT];
    private static final Vec3[] CLOUD_SCALES = new Vec3[CLOUD_COUNT];
    private static Map<ResourceLocation, Integer> MODELS_TO_XSIZE = new HashMap<>();
    private static Map<ResourceLocation, Integer> MODELS_TO_YSIZE = new HashMap<>();

    static{
        Random random = new Random(500);
        for (int i = 0; i < CLOUD_COUNT; i++){
            CLOUD_OFFSETS[i] = new Vec3(random.nextFloat() - 0.5F, 0.2F * (random.nextFloat() - 0.5F), random.nextFloat() - 0.5F).scale(1.2F);
            CLOUD_SCALES[i] = new Vec3(0.6F + random.nextFloat() * 0.2F, 0.4F + random.nextFloat() * 0.2F, 0.4F + random.nextFloat() * 0.2F);
        }
    }
    public LayerPetOverlays(RenderLayerParent parent) {
        super(parent);
        this.parent = parent;
    }

    @Override
    public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, Entity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (TameableUtils.couldBeTamed(entity)) {
            LivingEntity living = (LivingEntity) entity;
            float f = Mth.rotLerp(partialTicks, living.yBodyRotO, living.yBodyRot);
            float realAge = living.tickCount + partialTicks;
            if (TameableUtils.hasEnchant(living, DIEnchantmentRegistry.IMMUNITY_FRAME) && TameableUtils.getImmuneTime((LivingEntity) entity) > 0) {
                VertexConsumer ivertexbuilder = bufferIn.getBuffer(DIRenderTypes.IFRAME_GLINT);
                float alpha = 0.5F;
                matrixStackIn.pushPose();
                this.getParentModel().renderToBuffer(matrixStackIn, ivertexbuilder, packedLightIn, LivingEntityRenderer.getOverlayCoords((LivingEntity) entity, 0), 1, 1, 1, alpha);
                matrixStackIn.popPose();
            }
            if (TameableUtils.hasEnchant(living, DIEnchantmentRegistry.MAGNETIC)) {
                Entity suck = TameableUtils.getMagnetSuctionEntity(living);
                if (suck != null) {
                    double d0 = Mth.lerp(partialTicks, suck.xo, suck.getX()) - Mth.lerp(partialTicks, living.xo, living.getX());
                    double d1 = Mth.lerp(partialTicks, suck.yo, suck.getY()) - Mth.lerp(partialTicks, living.yo, living.getY());
                    double d2 = Mth.lerp(partialTicks, suck.zo, suck.getZ()) - Mth.lerp(partialTicks, living.zo, living.getZ());
                    double d3 = d0 * d0 + d2 * d2 + d1 * d1;
                    double d4 = Math.sqrt(d0 * d0 + d2 * d2);
                    float f1 = (float) (Mth.atan2(d2, d0) * (double) (180F / (float) Math.PI)) - 90.0F;
                    float f2 = (float) (-(Mth.atan2(d1, d4) * (double) (180F / (float) Math.PI)));
                    matrixStackIn.pushPose();
                    matrixStackIn.mulPose(Vector3f.YN.rotationDegrees(f));
                    matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(f1));
                    matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(f2));
                    matrixStackIn.pushPose();
                    float bob1 = (float) Math.sin(realAge * 0.5F) * 0.05F;
                    float bob2 = (float) Math.sin(realAge * 0.3F) * 0.09F - 0.03F;
                    float bob3 = (float) Math.cos(realAge * 0.1F) * 0.05F;
                    matrixStackIn.translate(bob1, 1.25F - entity.getBbHeight() * 0.5F - bob2, -entity.getBbWidth() - 0.125F - bob3);
                    matrixStackIn.mulPose(Vector3f.XN.rotationDegrees(90));
                    matrixStackIn.scale(1.6F, 1.6F, 3F);
                    Minecraft.getInstance().getItemRenderer().renderStatic(MAGNET, ItemTransforms.TransformType.GROUND, packedLightIn, OverlayTexture.NO_OVERLAY, matrixStackIn, bufferIn, entity.getId());
                    matrixStackIn.popPose();
                    matrixStackIn.popPose();
                }
            }
            if (TameableUtils.hasEnchant(living, DIEnchantmentRegistry.HEALTH_SIPHON)) {
                Entity owner = TameableUtils.getOwnerOf(living);
                if (owner != null && owner.isAlive() && owner.distanceTo(living) < 100) {
                    float x = (float) Mth.lerp(partialTicks, entity.xo, entity.getX());
                    float y = (float) Mth.lerp(partialTicks, entity.yo, entity.getY());
                    float z = (float) Mth.lerp(partialTicks, entity.zo, entity.getZ());
                    if (living.hurtTime > 0 && living.hurtTime == living.hurtDuration - 1) {
                        float height = -2 + entity.getBbHeight() * 0.8F;
                        float ownerHeight = -2 + owner.getBbHeight() * 0.6F;
                        LightningBoltData bolt = new LightningBoltData(healthBoltData, new Vec3(x, y + height, z), owner.position().add(0, ownerHeight, 0), 3)
                                .size(0.5F)
                                .lifespan(5)
                                .spawn(LightningBoltData.SpawnFunction.NO_DELAY);
                        lightningRender.update(living, bolt, partialTicks);
                    }
                    matrixStackIn.pushPose();
                    matrixStackIn.mulPose(Vector3f.YN.rotationDegrees(f));
                    matrixStackIn.mulPose(Vector3f.XN.rotationDegrees(180));
                    matrixStackIn.pushPose();
                    matrixStackIn.translate(-x, -y, -z);
                    lightningRender.render(partialTicks, matrixStackIn, bufferIn);
                    matrixStackIn.popPose();
                    matrixStackIn.popPose();
                }
            }
            if (TameableUtils.hasEnchant(living, DIEnchantmentRegistry.VOID_CLOUD) && !living.isInWaterOrBubble() && !living.isOnGround() && TameableUtils.getFallDistance(living) >= 3.0F) {
                matrixStackIn.pushPose();
                matrixStackIn.mulPose(Vector3f.YN.rotationDegrees(f));
                matrixStackIn.mulPose(Vector3f.XN.rotationDegrees(180));
                matrixStackIn.translate(-0.5F, -2F, -0.25F);

                for(int i = 0; i < CLOUD_COUNT; i++){
                    float xSin = (float)Math.sin(realAge * 0.05F + i * 2F) * 0.1F;
                    float ySin = (float)Math.cos(realAge * 0.05F + i * 2F) * 0.1F;
                    float zSin = (float)Math.sin(realAge * 0.05F + i * 2F - 2F) * 0.1F;
                    matrixStackIn.pushPose();
                    matrixStackIn.translate(CLOUD_OFFSETS[i].x + xSin, CLOUD_OFFSETS[i].y + ySin, CLOUD_OFFSETS[i].z + zSin);
                    matrixStackIn.scale((float)CLOUD_SCALES[i].x + xSin, (float)CLOUD_SCALES[i].y + ySin, (float)CLOUD_SCALES[i].z + xSin);
                    renderVoidCloudCube(entity, matrixStackIn, bufferIn.getBuffer(DIRenderTypes.VOID_CLOUD));
                    matrixStackIn.popPose();

                }
                matrixStackIn.popPose();
            }
            if (TameableUtils.isZombiePet(living)) {
                ResourceLocation mobTexture = getTextureLocation(living);
                Pair<Integer, Integer> xandyDimensions = TextureSizer.getTextureWidth(mobTexture);
                VertexConsumer zombieBuffer = bufferIn.getBuffer(DIRenderTypes.getZombieOverlay(mobTexture, xandyDimensions.getFirst(), xandyDimensions.getSecond()));
                float alpha = 0.1F;
                matrixStackIn.pushPose();
                this.getParentModel().renderToBuffer(matrixStackIn, zombieBuffer, packedLightIn, LivingEntityRenderer.getOverlayCoords((LivingEntity) entity, 0), 0, 0.5F, 0, alpha);
                matrixStackIn.popPose();
            }
        }
    }

    private void renderVoidCloudCube(Entity entity, PoseStack poseStack, VertexConsumer consumer) {
        Matrix4f cubeAt = poseStack.last().pose();
        this.renderVoidCloudFace(entity, cubeAt, consumer, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, Direction.SOUTH);
        this.renderVoidCloudFace(entity, cubeAt, consumer, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, Direction.NORTH);
        this.renderVoidCloudFace(entity, cubeAt, consumer, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, Direction.EAST);
        this.renderVoidCloudFace(entity, cubeAt, consumer, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, Direction.WEST);
        this.renderVoidCloudFace(entity, cubeAt, consumer, 0.0F, 1.0F, 0, 0, 0.0F, 0.0F, 1.0F, 1.0F, Direction.DOWN);
        this.renderVoidCloudFace(entity, cubeAt, consumer, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, Direction.UP);
    }

    private void renderVoidCloudFace(Entity entity, Matrix4f p_173696_, VertexConsumer p_173697_, float p_173698_, float p_173699_, float p_173700_, float p_173701_, float p_173702_, float p_173703_, float p_173704_, float p_173705_, Direction p_173706_) {
        p_173697_.vertex(p_173696_, p_173698_, p_173700_, p_173702_).overlayCoords(240).endVertex();
        p_173697_.vertex(p_173696_, p_173699_, p_173700_, p_173703_).overlayCoords(240).endVertex();
        p_173697_.vertex(p_173696_, p_173699_, p_173701_, p_173704_).overlayCoords(240).endVertex();
        p_173697_.vertex(p_173696_, p_173698_, p_173701_, p_173705_).overlayCoords(240).endVertex();
    }
}
