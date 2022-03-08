package com.github.alexthe668.domesticationinnovation.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ParticleMagnet extends SimpleAnimatedParticle {

    private ParticleMagnet(ClientLevel world, double x, double y, double z, double motionX, double motionY, double motionZ, SpriteSet sprites) {
        super(world, x, y, z, sprites, 0.0F);
        this.xd = (float) motionX;
        this.yd = (float) motionY;
        this.zd = (float) motionZ;
        this.quadSize = 0.1F + this.random.nextFloat() * 0.1F;
        this.lifetime = 1 + this.random.nextInt(2);
        this.gravity = 0;
        this.pickSprite(sprites);
    }

    public int getLightColor(float f) {
        return 240;
    }

    public void tick() {
        super.tick();
        this.xd *= 0.2D;
        this.yd *= 0.2D;
        this.zd *= 0.2D;
        this.setAlpha(1F - (this.age / (float) this.lifetime));
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
            ParticleMagnet p = new ParticleMagnet(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, spriteSet);
            return p;
        }
    }
}
