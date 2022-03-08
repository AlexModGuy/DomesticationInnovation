package com.github.alexthe668.domesticationinnovation.mixin;

import com.github.alexthe668.domesticationinnovation.DomesticationMod;
import com.github.alexthe668.domesticationinnovation.server.enchantment.DIEnchantmentRegistry;
import com.github.alexthe668.domesticationinnovation.server.entity.ModifedToBeTameable;
import com.github.alexthe668.domesticationinnovation.server.entity.TameableUtils;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.UUID;

@Mixin(AbstractHorse.class)
public abstract class AbstractHorseMixin extends Animal implements ModifedToBeTameable {

    @Shadow
    protected float playerJumpPendingScale;
    @Shadow
    protected boolean isJumping;
    @Shadow
    private boolean allowStandSliding;

    protected AbstractHorseMixin(EntityType<? extends Animal> type, Level level) {
        super(type, level);
    }

    @Shadow
    public abstract boolean isTamed();

    @Shadow
    public abstract void setTamed(boolean tame);

    @Shadow
    public abstract void setIsJumping(boolean p_30656_);

    @Shadow
    public abstract boolean isStanding();

    @Shadow @Nullable public abstract UUID getOwnerUUID();

    @Shadow public abstract void setOwnerUUID(@org.jetbrains.annotations.Nullable UUID p_30587_);

    @Inject(
            method = {"Lnet/minecraft/world/entity/animal/horse/AbstractHorse;travel(Lnet/minecraft/world/phys/Vec3;)V"},
            remap = true,
            at = {@At("HEAD")},
            cancellable = true
    )
    private void di_travel(Vec3 vec3, CallbackInfo ci) {
        if (this.isAlive() && this.isVehicle() && this.isInWaterOrBubble() && TameableUtils.hasEnchant(this, DIEnchantmentRegistry.AMPHIBIOUS)) {
            LivingEntity livingentity = (LivingEntity) this.getControllingPassenger();
            this.setYRot(livingentity.getYRot());
            this.yRotO = this.getYRot();
            this.setXRot(livingentity.getXRot() * 0.5F);
            this.setRot(this.getYRot(), this.getXRot());
            this.yBodyRot = this.getYRot();
            this.yHeadRot = this.yBodyRot;
            float f = livingentity.xxa * 0.5F;
            float f1 = livingentity.zza;
            if (f1 <= 0.0F) {
                f1 *= 0.25F;
            }
            float down = 0.08F;
            if (playerJumpPendingScale != 0) {
                down -= playerJumpPendingScale;
                playerJumpPendingScale = 0;
            }
            if (this.isControlledByLocalInstance()) {
                this.setSpeed((float) this.getAttributeValue(Attributes.MOVEMENT_SPEED));
                super.travel(new Vec3(f, vec3.y - down, f1));
            } else if (livingentity instanceof Player) {
                this.setDeltaMovement(this.getDeltaMovement().multiply(0.6D, 1.0D, 0.6D).add(0, -down, 0));
            }
            this.calculateEntityAnimation(this, false);
            this.tryCheckInsideBlocks();
            ci.cancel();
        }
    }

    public boolean isTame() {
        return this.isTamed() && DomesticationMod.CONFIG.tameableHorse.get();
    }

    public void setTame(boolean tame) {
        this.setTamed(tame);
    }

    public UUID getTameOwnerUUID(){
        return this.getOwnerUUID();
    }
    public void setTameOwnerUUID(@Nullable UUID uuid){
        this.setOwnerUUID(uuid);
    }

    @Nullable
    public LivingEntity getTameOwner() {
        try {
            UUID uuid = this.getTameOwnerUUID();
            return uuid == null ? null : this.level.getPlayerByUUID(uuid);
        } catch (IllegalArgumentException illegalargumentexception) {
            return null;
        }
    }

    public boolean isFollowingOwner() {
        return false;
    }

    public boolean isStayingStill() {
        return false;
    }

    public boolean isValidAttackTarget(LivingEntity target) {
        return false;
    }
}
