package com.github.alexthe668.domesticationinnovation.mixin;

import com.github.alexthe668.domesticationinnovation.server.entity.ModifedToBeTameable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.sensing.AxolotlAttackablesSensor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AxolotlAttackablesSensor.class)
public class AxolotlAttackablesSensorMixin {

    @Inject(
            method = {"Lnet/minecraft/world/entity/ai/sensing/AxolotlAttackablesSensor;isMatchingEntity(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/LivingEntity;)Z"},
            remap = true,
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private void di_isHuntTarget(LivingEntity axolotl, LivingEntity livingEntity, CallbackInfoReturnable<Boolean> cir) {
        if(axolotl instanceof ModifedToBeTameable tamed && tamed.getTameOwner() != null && !tamed.isStayingStill()){
            if(tamed.isValidAttackTarget(livingEntity)){
                cir.setReturnValue(true);
            }else{
                cir.setReturnValue(false);
            }
        }
    }
}
