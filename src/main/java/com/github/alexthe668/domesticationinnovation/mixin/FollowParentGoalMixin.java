package com.github.alexthe668.domesticationinnovation.mixin;

import com.github.alexthe666.citadel.server.entity.IComandableMob;
import com.github.alexthe668.domesticationinnovation.DomesticationMod;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FollowParentGoal.class)
public class FollowParentGoalMixin {

    @Shadow
    @Final
    private Animal animal;

    @Inject(
            at = {@At("HEAD")},
            remap = true,
            method = {"Lnet/minecraft/world/entity/ai/goal/FollowParentGoal;canUse()Z"},
            cancellable = true
    )
    private void di_canUse(CallbackInfoReturnable<Boolean> cir){
        if(animal instanceof IComandableMob commandableMob && commandableMob.getCommand() != 0 && DomesticationMod.CONFIG.trinaryCommandSystem.get()){
            cir.setReturnValue(false);
        }
    }

    @Inject(
            at = {@At("HEAD")},
            remap = true,
            method = {"Lnet/minecraft/world/entity/ai/goal/FollowParentGoal;canContinueToUse()Z"},
            cancellable = true
    )
    private void di_canContinueToUse(CallbackInfoReturnable<Boolean> cir){
        if(animal instanceof IComandableMob commandableMob && commandableMob.getCommand() != 0 && DomesticationMod.CONFIG.trinaryCommandSystem.get()){
            cir.setReturnValue(false);
        }
    }
}
