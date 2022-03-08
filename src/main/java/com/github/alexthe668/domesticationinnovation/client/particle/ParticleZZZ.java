package com.github.alexthe668.domesticationinnovation.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ParticleZZZ extends SimpleAnimatedParticle {

    private float sinOffset;
    private float cosOffset;
    private float rotationDir;
    private float initialX;
    private float initialZ;

    private ParticleZZZ(ClientLevel world, double x, double y, double z, double motionX, double motionY, double motionZ, SpriteSet sprites) {
        super(world, x, y, z, sprites, 0.0F);
        this.xd = (float) motionX;
        this.yd = (float) motionY;
        this.zd = (float) motionZ;
        this.quadSize = 0.1F + this.random.nextFloat() * 0.1F;
        this.lifetime = 16 + this.random.nextInt(6);
        this.gravity = -0.08F;
        this.sinOffset = this.random.nextFloat();
        this.cosOffset = this.random.nextFloat();
        this.rotationDir = this.random.nextBoolean() ? 1 : -1;
        this.initialX = (float) x;
        this.initialZ = (float) z;
        this.pickSprite(sprites);
    }

    public void tick() {
        super.tick();
        float xTarget = initialX + (float)Math.cos(this.age * 0.3F + this.cosOffset * 4) * 0.2F * rotationDir;
        float zTarget = initialZ + (float)Math.sin(this.age * 0.3F + this.sinOffset * 4) * 0.2F * rotationDir;
        this.xd = 0.3F * (xTarget - x);
        this.zd = 0.3F * (zTarget - z);
        this.setAlpha(1F - (this.age / (float) this.lifetime));
    }

    public int getLightColor(float partialTicks) {
        float f = ((float)this.age + partialTicks) / (float)this.lifetime;
        f = Mth.clamp(f, 0.0F, 1.0F);
        int i = super.getLightColor(partialTicks);
        int j = i & 255;
        int k = i >> 16 & 255;
        j += (int)(f * 15.0F * 16.0F);
        if (j > 240) {
            j = 240;
        }
        return j | k << 16;
    }


    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public Factory(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new ParticleZZZ(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, spriteSet);
        }
    }
}
