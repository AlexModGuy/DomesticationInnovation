package com.github.alexthe668.domesticationinnovation.mixin;

import com.github.alexthe668.domesticationinnovation.server.enchantment.DIEnchantmentRegistry;
import com.github.alexthe668.domesticationinnovation.server.entity.TameableUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    public LivingEntityMixin(EntityType<?> entityType, Level lvl) {
        super(entityType, lvl);
    }

    @Inject(
            method = {"Lnet/minecraft/world/entity/LivingEntity;getWaterSlowDown()F"},
            remap = true,
            at = @At(value = "TAIL"),
            cancellable = true
    )
    private void di_getWaterSlowdown(CallbackInfoReturnable<Float> cir) {
        if(TameableUtils.isTamed(this) && isLandAndSea()){
            cir.setReturnValue(0.98F);
        }
    }

    private boolean isLandAndSea(){
        return TameableUtils.hasEnchant(((LivingEntity) (Entity)this), DIEnchantmentRegistry.AMPHIBIOUS);
    }

    @Inject(
            method = {"Lnet/minecraft/world/entity/LivingEntity;rideableUnderWater()Z"},
            remap = true,
            at = @At(value = "HEAD"),
            cancellable = true
    )
    protected void di_rideableInWater(CallbackInfoReturnable<Boolean> cir) {
        if(TameableUtils.isTamed(this) && this.isLandAndSea()){
            cir.setReturnValue(true);
        }
    }
}
