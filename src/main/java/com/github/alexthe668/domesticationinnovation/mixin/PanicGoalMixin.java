package com.github.alexthe668.domesticationinnovation.mixin;

import com.github.alexthe668.domesticationinnovation.DomesticationMod;
import com.github.alexthe668.domesticationinnovation.server.entity.CommandableMob;
import com.github.alexthe668.domesticationinnovation.server.entity.ModifedToBeTameable;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PanicGoal.class)
public class PanicGoalMixin {

    @Shadow
    @Final
    private PathfinderMob mob;

    @Inject(
            at = {@At("HEAD")},
            remap = true,
            method = {"Lnet/minecraft/world/entity/ai/goal/PanicGoal;canUse()Z"},
            cancellable = true
    )
    private void di_canUse(CallbackInfoReturnable<Boolean> cir){
        if(mob instanceof ModifedToBeTameable mob && mob.isTame()){
            cir.setReturnValue(false);
        }
    }

    @Inject(
            at = {@At("HEAD")},
            remap = true,
            method = {"Lnet/minecraft/world/entity/ai/goal/PanicGoal;canContinueToUse()Z"},
            cancellable = true
    )
    private void di_canContinueToUse(CallbackInfoReturnable<Boolean> cir){
        if(mob instanceof ModifedToBeTameable mob && mob.isTame()){
            cir.setReturnValue(false);
        }

    }
}
