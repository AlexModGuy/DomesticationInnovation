package com.github.alexthe668.domesticationinnovation.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ParticleLanternBugs extends SimpleAnimatedParticle {
    private float targetX = 0;
    private float targetY = 0;
    private float targetZ = 0;

    private ParticleLanternBugs(ClientLevel world, double x, double y, double z, double motionX, double motionY, double motionZ, SpriteSet sprites) {
        super(world, x, y, z, sprites, 0.0F);
        this.xd = 0;
        this.yd = 0;
        this.zd = 0;
        this.rCol = 0.96F + random.nextFloat() * 0.03F;
        this.gCol = 0.96F + random.nextFloat() * 0.03F;
        this.bCol = 0.495F + random.nextFloat() * 0.03F;
        this.targetX = (float) motionX;
        this.targetY = (float) motionY;
        this.targetZ = (float) motionZ;
        this.quadSize = 0.3F;
        this.lifetime = 25 + random.nextInt(15);
        this.pickSprite(sprites);
    }

    public void tick() {
        super.tick();
        float speed = 0.01F;
        this.quadSize = 0.3F - 0.2F * (age / (float) lifetime);
        double moveX = (targetX + random.nextGaussian()  * 1.5D) - x;
        double moveY = (targetY + random.nextGaussian()  * 1.5D) - y;
        double moveZ = (targetZ + random.nextGaussian()  * 1.5D) - z;
        this.setAlpha(1F - (age / (float) lifetime));
        this.xd += moveX * speed;
        this.yd += moveY * speed;
        this.zd += moveZ * speed;
        this.xd *= 0.8;
        this.yd *= 0.8;
        this.zd *= 0.8;
    }

    public int getLightColor(float p_107249_) {
        BlockPos blockpos = new BlockPos(this.x, this.y, this.z);
        return this.level.hasChunkAt(blockpos) ? LevelRenderer.getLightColor(this.level, blockpos) : 0;
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
            ParticleLanternBugs p = new ParticleLanternBugs(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, spriteSet);
            return p;
        }
    }
}
