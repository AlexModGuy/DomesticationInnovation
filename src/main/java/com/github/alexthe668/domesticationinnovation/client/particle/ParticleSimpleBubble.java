package com.github.alexthe668.domesticationinnovation.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ParticleSimpleBubble extends SimpleAnimatedParticle {

    private ParticleSimpleBubble(ClientLevel world, double x, double y, double z, double motionX, double motionY, double motionZ, SpriteSet sprites) {
        super(world, x, y, z, sprites, 0.0F);
        this.xd = (float) motionX;
        this.yd = (float) motionY;
        this.zd = (float) motionZ;
        this.quadSize = 0.1F + this.random.nextFloat() * 0.1F;
        this.lifetime = 12 + this.random.nextInt(12);
        this.gravity = -0.08F;
        this.pickSprite(sprites);
    }

    public void tick() {
        super.tick();
        this.xd *= 0.8D;
        this.yd *= 0.8D;
        this.zd *= 0.8D;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public int getLightColor(float p_107249_) {
        BlockPos blockpos = new BlockPos(this.x, this.y, this.z);
        return this.level.hasChunkAt(blockpos) ? LevelRenderer.getLightColor(this.level, blockpos) : 0;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public Factory(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            ParticleSimpleBubble p = new ParticleSimpleBubble(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, spriteSet);
            return p;
        }
    }
}
