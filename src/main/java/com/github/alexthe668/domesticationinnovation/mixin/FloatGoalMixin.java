package com.github.alexthe668.domesticationinnovation.mixin;

import com.github.alexthe668.domesticationinnovation.server.enchantment.DIEnchantmentRegistry;
import com.github.alexthe668.domesticationinnovation.server.entity.TameableUtils;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FloatGoal.class)
public abstract class FloatGoalMixin extends Goal {

    @Shadow
    @Final
    private Mob mob;


    @Inject(
            at = {@At("HEAD")},
            remap = true,
            method = {"Lnet/minecraft/world/entity/ai/goal/FloatGoal;canUse()Z"},
            cancellable = true
    )
    private void di_canUse(CallbackInfoReturnable<Boolean> cir){
        if(TameableUtils.isTamed(mob) && TameableUtils.hasEnchant(mob, DIEnchantmentRegistry.AMPHIBIOUS)){
            cir.setReturnValue(false);
        }
    }

}
