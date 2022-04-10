package com.github.alexthe668.domesticationinnovation.client.render;

import com.github.alexthe668.domesticationinnovation.DomesticationMod;
import com.github.alexthe668.domesticationinnovation.client.model.HighlightedBlockModel;
import com.github.alexthe668.domesticationinnovation.client.model.RecallBallModel;
import com.github.alexthe668.domesticationinnovation.server.entity.HighlightedBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderHighlightedBlock extends EntityRenderer<HighlightedBlockEntity> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(DomesticationMod.MODID, "textures/highlighted_block.png");
    private HighlightedBlockModel highlightedBlockModel = new HighlightedBlockModel();

    public RenderHighlightedBlock(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    public void render(HighlightedBlockEntity entity, float f1, float f2, PoseStack stack, MultiBufferSource source, int packedLight) {
        stack.pushPose();
        stack.translate(0, 0.5F, 0);
        VertexConsumer vertexconsumer = source.getBuffer(RenderType.outline(this.getTextureLocation(entity)));
        this.highlightedBlockModel.renderToBuffer(stack, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        stack.popPose();
    }

    public ResourceLocation getTextureLocation(HighlightedBlockEntity block) {
        return TEXTURE;
    }
}