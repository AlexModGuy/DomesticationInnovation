package com.github.alexthe668.domesticationinnovation.client.render;

import com.github.alexthe668.domesticationinnovation.DomesticationMod;
import com.github.alexthe668.domesticationinnovation.client.model.RecallBallModel;
import com.github.alexthe668.domesticationinnovation.server.entity.FollowingJukeboxEntity;
import com.github.alexthe668.domesticationinnovation.server.entity.RecallBallEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;

public class RenderJukeboxFollower extends EntityRenderer<FollowingJukeboxEntity> {

    private ItemStack jukebox = new ItemStack(Items.JUKEBOX);

    public RenderJukeboxFollower(EntityRendererProvider.Context renderManagerIn) {
        super(renderManagerIn);
        jukebox.enchant(Enchantments.VANISHING_CURSE, 1);
    }

    @Override
    public void render(FollowingJukeboxEntity entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int light) {
        super.render(entity, yaw, partialTicks, poseStack, buffer, light);
        poseStack.pushPose();
        poseStack.mulPose(Vector3f.YP.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot())));
        poseStack.translate(0, 0.1F, 0);
        poseStack.scale(1.8F, 1.8F, 1.8F);
        Minecraft.getInstance().getItemRenderer().renderStatic(jukebox, ItemTransforms.TransformType.GROUND, light, OverlayTexture.NO_OVERLAY, poseStack, buffer, entity.getId());
        poseStack.popPose();

    }

    @Override
    public ResourceLocation getTextureLocation(FollowingJukeboxEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }

}