package com.github.alexthe668.domesticationinnovation.client.particle;

import com.github.alexthe668.domesticationinnovation.DomesticationMod;
import com.github.alexthe668.domesticationinnovation.server.item.DIItemRegistry;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ForgeRenderTypes;

import java.util.Random;

public class ParticleIntimidation extends Particle {

    private float xRot;
    private float yRot;
    private int entityId;

    ParticleIntimidation(ClientLevel lvl, double x, double y, double z, int entityId, float xRot, float yRot) {
        super(lvl, x, y, z);
        this.setSize(1, 1);
        this.gravity = 0.0F;
        this.lifetime = 22 + new Random().nextInt(7);
        this.entityId = entityId;
        this.xRot = xRot;
        this.yRot = yRot;
    }

    public ParticleRenderType getRenderType() {
        return ParticleRenderType.CUSTOM;
    }

    public void render(VertexConsumer vertexConsumer, Camera camera, float partialTick) {
        Entity entity = Minecraft.getInstance().level.getEntity(entityId);
        if(entity != null){
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
            float up = 0.2F + (2F + entity.getBbHeight()) * fadeIn + (float)Math.sin(lerpAge * 0.3F) * 0.03F;
            float down = -3 * fadeOut;
            float scale = (fadeIn - fadeOut) * 1.5F;
            posestack.pushPose();
            posestack.translate(f, f1 + (up + down), f2);
            posestack.mulPose(Vector3f.XP.rotationDegrees(180));
            posestack.mulPose(Vector3f.YP.rotationDegrees(yRot));
            posestack.scale(scale, scale, scale);
            renderEntity(entity, 0, 0, 0, scale, partialTick, posestack, multibuffersource$buffersource, getLightColor(partialTick));
            multibuffersource$buffersource.endBatch();
            posestack.popPose();
        }

    }


    public <E extends Entity> void renderEntity(E entityIn, double x, double y, double z, float scale, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int packedLight) {
        EntityRenderer<? super E> render = null;
        EntityRenderDispatcher manager = Minecraft.getInstance().getEntityRenderDispatcher();
        try {
            render = manager.getRenderer(entityIn);
            if (render instanceof LivingEntityRenderer livingEntityRenderer) {
                EntityModel model = livingEntityRenderer.getModel();
                ResourceLocation tex = livingEntityRenderer.getTextureLocation(entityIn);
                float alpha = Math.min(scale, 1.0F) * 0.5F;
                boolean prevSit = entityIn instanceof TamableAnimal && ((TamableAnimal) entityIn).isInSittingPose();
                if(entityIn instanceof TamableAnimal){
                    ((TamableAnimal) entityIn).setInSittingPose(false);
                }
                float bob = entityIn instanceof Wolf ? ((Wolf) entityIn).getTailAngle() : entityIn.tickCount + partialTicks;
                model.prepareMobModel(entityIn, 1, 0.0F, partialTicks);
                model.setupAnim(entityIn, 0, 0, bob, 0, xRot);
                model.renderToBuffer(matrixStack, bufferIn.getBuffer(ForgeRenderTypes.getItemLayeredTranslucent(tex)), packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, alpha);
                if(entityIn instanceof TamableAnimal){
                    ((TamableAnimal) entityIn).setInSittingPose(prevSit);
                }
            }
        } catch (Throwable throwable3) {
            DomesticationMod.LOGGER.warn("could not render intimidating effect");
            this.remove();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Factory implements ParticleProvider<SimpleParticleType> {
        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new ParticleIntimidation(worldIn, x, y, z, (int) xSpeed, (float)ySpeed, (float)zSpeed);
        }
    }
}
