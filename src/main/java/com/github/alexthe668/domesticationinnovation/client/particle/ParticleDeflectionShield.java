package com.github.alexthe668.domesticationinnovation.client.particle;

import com.github.alexthe668.domesticationinnovation.server.item.DIItemRegistry;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Random;

public class ParticleDeflectionShield extends Particle {

    private float xRot;
    private float yRot;

    ParticleDeflectionShield(ClientLevel lvl, double x, double y, double z, float xRot, float yRot) {
        super(lvl, x, y, z);
        this.setSize(1, 1);
        this.gravity = 0.0F;
        this.lifetime = 15 + new Random().nextInt(7);
        this.xRot = xRot;
        this.yRot = yRot;
    }

    public ParticleRenderType getRenderType() {
        return ParticleRenderType.CUSTOM;
    }

    public void render(VertexConsumer vertexConsumer, Camera camera, float partialTick) {
        Vec3 vec3 = camera.getPosition();
        PoseStack posestack = new PoseStack();
        MultiBufferSource.BufferSource multibuffersource$buffersource = Minecraft.getInstance().renderBuffers().bufferSource();
        float f = (float)(Mth.lerp((double)partialTick, this.xo, this.x) - vec3.x());
        float f1 = (float)(Mth.lerp((double)partialTick, this.yo, this.y) - vec3.y());
        float f2 = (float)(Mth.lerp((double)partialTick, this.zo, this.z) - vec3.z());
        float lerpAge = this.age + partialTick;
        float ageProgress = lerpAge / (float) this.lifetime;
        float fadeTime = 0.15F;
        float fadeIn = Math.min(ageProgress, fadeTime) / fadeTime;
        float fadeOut = Mth.clamp(ageProgress - (1F - fadeTime), 0F, fadeTime) / fadeTime;
        float up = -1F + fadeIn + (float)Math.sin(lerpAge * 0.3F) * 0.03F;
        float down = -1F * fadeOut;
        float scale = (fadeIn - fadeOut) * 1.85F;
        posestack.pushPose();
        posestack.translate(f, f1 + up + down, f2);
        //xRot + 45 - 45 * fadeIn + 45 * fadeOut
        posestack.mulPose(Axis.YP.rotationDegrees(yRot));
        posestack.mulPose(Axis.XN.rotationDegrees(xRot));
        posestack.scale(scale, scale, scale);
        Minecraft.getInstance().getItemRenderer().renderStatic(new ItemStack(DIItemRegistry.DEFLECTION_SHIELD.get()), ItemTransforms.TransformType.GROUND, 240, OverlayTexture.NO_OVERLAY, posestack, multibuffersource$buffersource, 0);
        multibuffersource$buffersource.endBatch();
        posestack.popPose();
    }

    @OnlyIn(Dist.CLIENT)
    public static class Factory implements ParticleProvider<SimpleParticleType> {
        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new ParticleDeflectionShield(worldIn, x, y, z, (float)xSpeed, (float)ySpeed);
        }
    }
}
