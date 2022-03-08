package com.github.alexthe668.domesticationinnovation.mixin;

import com.github.alexthe668.domesticationinnovation.DomesticationMod;
import com.github.alexthe668.domesticationinnovation.server.enchantment.DIEnchantmentRegistry;
import com.github.alexthe668.domesticationinnovation.server.entity.CommandableMob;
import com.github.alexthe668.domesticationinnovation.server.entity.TameableUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.LevelReader;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FollowOwnerGoal.class)
public abstract class FollowOwnerGoalMixin extends Goal {

    @Shadow
    @Final
    private TamableAnimal tamable;
    @Shadow
    @Final
    private LevelReader level;
    @Shadow
    private LivingEntity owner;
    @Shadow
    @Final
    private float stopDistance;

    @Shadow @Final private double speedModifier;

    @Inject(
            at = {@At("HEAD")},
            remap = true,
            method = {"Lnet/minecraft/world/entity/ai/goal/FollowOwnerGoal;canUse()Z"},
            cancellable = true
    )
    private void di_canUse(CallbackInfoReturnable<Boolean> cir){
        if(tamable instanceof CommandableMob commandableMob && commandableMob.getCommand() != 2 && DomesticationMod.CONFIG.trinaryCommandSystem.get()){
            cir.setReturnValue(false);
        }
    }

    @Inject(
            at = {@At("HEAD")},
            remap = true,
            method = {"Lnet/minecraft/world/entity/ai/goal/FollowOwnerGoal;canContinueToUse()Z"},
            cancellable = true
    )
    private void di_canContinueToUse(CallbackInfoReturnable<Boolean> cir){
        if(tamable instanceof CommandableMob commandableMob && commandableMob.getCommand() != 2 && DomesticationMod.CONFIG.trinaryCommandSystem.get()){
            cir.setReturnValue(false);
        }
    }

    @Inject(
            at = {@At("HEAD")},
            remap = true,
            method = {"Lnet/minecraft/world/entity/ai/goal/FollowOwnerGoal;canTeleportTo(Lnet/minecraft/core/BlockPos;)Z"},
            cancellable = true
    )
    private void di_canTeleportTo(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if(TameableUtils.hasEnchant(tamable, DIEnchantmentRegistry.AMPHIBIOUS) && level.isWaterAt(pos)){
            cir.setReturnValue(true);
        }
    }

    @Inject(
            at = {@At("HEAD")},
            remap = true,
            method = {"Lnet/minecraft/world/entity/ai/goal/FollowOwnerGoal;tick()V"},
            cancellable = true
    )
    private void di_tick(CallbackInfo ci) {
        if(TameableUtils.hasEnchant(tamable, DIEnchantmentRegistry.AMPHIBIOUS) && tamable.isInWaterOrBubble() && this.tamable.distanceToSqr(this.owner) < 144.0D){
            tamable.getNavigation().moveTo(owner, speedModifier);
            ci.cancel();
        }
    }
}