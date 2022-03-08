package com.github.alexthe668.domesticationinnovation.client.render;

import com.github.alexthe668.domesticationinnovation.DomesticationMod;
import com.github.alexthe668.domesticationinnovation.client.model.RecallBallModel;
import com.github.alexthe668.domesticationinnovation.server.entity.RecallBallEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class RecallBallRender extends EntityRenderer<RecallBallEntity> {

    private RecallBallModel recallBallModel = new RecallBallModel();
    private static final ResourceLocation TEXTURE = new ResourceLocation(DomesticationMod.MODID, "textures/recall_ball.png");

    public RecallBallRender(EntityRendererProvider.Context renderManagerIn) {
        super(renderManagerIn);
    }

    @Override
    public void render(RecallBallEntity entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int light) {
        super.render(entity, yaw, partialTicks, poseStack, buffer, light);
        poseStack.pushPose();
        poseStack.mulPose(new Quaternion(Vector3f.XP, 180F, true));
        poseStack.mulPose(Vector3f.YP.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot())));
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(Mth.lerp(partialTicks, entity.xRotO, entity.getXRot())));
        VertexConsumer vertexconsumer = buffer.getBuffer(this.recallBallModel.renderType(this.getTextureLocation(entity)));
        poseStack.translate(0, -1.65F, 0);
        this.recallBallModel.animateBall(entity, partialTicks);
        this.recallBallModel.renderToBuffer(poseStack, vertexconsumer, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();

    }

    @Override
    public ResourceLocation getTextureLocation(RecallBallEntity entity) {
        return TEXTURE;
    }

}