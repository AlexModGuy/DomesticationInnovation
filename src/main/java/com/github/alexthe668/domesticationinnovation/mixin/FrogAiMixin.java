package com.github.alexthe668.domesticationinnovation.mixin;

import com.github.alexthe668.domesticationinnovation.server.entity.DIActivityRegistry;
import com.github.alexthe668.domesticationinnovation.server.entity.ModifedToBeTameable;
import com.github.alexthe668.domesticationinnovation.server.entity.ai.AmphibianFollowOwnerBehavior;
import com.github.alexthe668.domesticationinnovation.server.entity.ai.AmphibianStayBehavior;
import com.github.alexthe668.domesticationinnovation.server.misc.DITagRegistry;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.frog.FrogAi;
import net.minecraft.world.entity.animal.frog.ShootTongue;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(FrogAi.class)
public class FrogAiMixin {


    @Inject(
            method = {"Lnet/minecraft/world/entity/animal/frog/FrogAi;makeBrain(Lnet/minecraft/world/entity/ai/Brain;)Lnet/minecraft/world/entity/ai/Brain;"},
            remap = true,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/animal/frog/FrogAi;initJumpActivity(Lnet/minecraft/world/entity/ai/Brain;)V"
            )
    )
    private static void di_makeBrain(Brain<Frog> brain, CallbackInfoReturnable<Brain<?>> cir) {
        brain.addActivity(DIActivityRegistry.FROG_FOLLOW.get(), ImmutableList.of(Pair.of(0, new AmphibianFollowOwnerBehavior(1.25F, 1.0F)), Pair.of(1, new StartAttacking<>(FrogAiMixin::canAttack , (p_218605_) -> {
            return p_218605_.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET);
        })), Pair.of(2, new ShootTongue(SoundEvents.FROG_TONGUE, SoundEvents.FROG_EAT))));
        brain.addActivity(DIActivityRegistry.FROG_STAY.get(), ImmutableList.of(Pair.of(0, new AmphibianStayBehavior())));
    }

    private static boolean canAttack(Frog frog) {
        return !frog.isInLove();
    }

    @Inject(
            method = {"Lnet/minecraft/world/entity/animal/frog/FrogAi;updateActivity(Lnet/minecraft/world/entity/animal/frog/Frog;)V"},
            remap = true,
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    private static void di_updateActivity(Frog frog, CallbackInfo ci) {
        Brain<Frog> brain = frog.getBrain();
        Activity activity = brain.getActiveNonCoreActivity().orElse(null);
        if (frog instanceof ModifedToBeTameable modifedToBeTameable) {
            if (modifedToBeTameable.isStayingStill()) {
                brain.setActiveActivityIfPossible(DIActivityRegistry.FROG_STAY.get());
                ci.cancel();
            } else if (modifedToBeTameable.isFollowingOwner()) {
                if(frog.getTarget() != null && frog.getTarget().isAlive()){
                    brain.setMemory(MemoryModuleType.ATTACK_TARGET, frog.getTarget());
                    brain.setMemory(MemoryModuleType.NEAREST_ATTACKABLE, frog.getTarget());
                    brain.setActiveActivityIfPossible(Activity.TONGUE);
                }else{
                    frog.getBrain().setActiveActivityToFirstValid(ImmutableList.of(DIActivityRegistry.FROG_FOLLOW.get(), Activity.TONGUE, Activity.LAY_SPAWN, Activity.LONG_JUMP, Activity.SWIM,  Activity.IDLE));
                }
                ci.cancel();
            }
        }
    }

    @Inject(
            method = {"Lnet/minecraft/world/entity/animal/frog/FrogAi;getTemptations()Lnet/minecraft/world/item/crafting/Ingredient;"},
            remap = true,
            at = @At(
                    value = "TAIL"
            ),
            cancellable = true
    )
    private static void di_getTemptationItems(CallbackInfoReturnable<Ingredient> cir) {
        cir.setReturnValue(Ingredient.merge(ImmutableList.of(cir.getReturnValue(), Ingredient.of(DITagRegistry.TAME_FROGS_WITH))));
    }
}
