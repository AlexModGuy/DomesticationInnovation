package com.github.alexthe668.domesticationinnovation.client.render;

import com.github.alexthe666.citadel.client.render.LightningBoltData;
import com.github.alexthe666.citadel.client.render.LightningRender;
import com.github.alexthe668.domesticationinnovation.server.entity.ChainLightningEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector4f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class ChainLightningRender extends EntityRenderer<ChainLightningEntity> {

    private LightningRender lightningRender = new LightningRender();
    private LightningBoltData.BoltRenderInfo lightningBoltData = new LightningBoltData.BoltRenderInfo(1.3F, 0.15F, 0.5F, 0.25F, new Vector4f(0.1F, 0.3F, 0.5F, 0.5F), 0.45F);

    public ChainLightningRender(EntityRendererProvider.Context renderManagerIn) {
        super(renderManagerIn);
    }

    @Override
    public boolean shouldRender(ChainLightningEntity entity, Frustum frustum, double x, double y, double z) {
        Entity next = entity.getFromEntity();
        return next != null && frustum.isVisible(entity.getBoundingBox().minmax(next.getBoundingBox())) || super.shouldRender(entity, frustum, x, y, z);
    }

    @Override
    public void render(ChainLightningEntity entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int light) {
        super.render(entity, yaw, partialTicks, poseStack, buffer, light);
        poseStack.pushPose();
        Entity from = entity.getFromEntity();
        float x = (float)Mth.lerp(partialTicks, entity.xo, entity.getX());
        float y = (float)Mth.lerp(partialTicks, entity.yo, entity.getY());
        float z = (float)Mth.lerp(partialTicks, entity.zo, entity.getZ());
        if (from != null) {
            LightningBoltData bolt = new LightningBoltData(lightningBoltData, from.getEyePosition(), entity.position(), 5)
                    .size(0.1F)
                    .lifespan(2)
                    .spawn(LightningBoltData.SpawnFunction.NO_DELAY);
            lightningRender.update(null, bolt, partialTicks);
            poseStack.translate(-x, -y, -z);
            lightningRender.render(partialTicks, poseStack, buffer);
        }
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(ChainLightningEntity entity) {
        return null;
    }

}