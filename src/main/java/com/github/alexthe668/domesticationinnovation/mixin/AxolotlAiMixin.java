package com.github.alexthe668.domesticationinnovation.mixin;

import com.github.alexthe668.domesticationinnovation.server.enchantment.DIEnchantmentRegistry;
import com.github.alexthe668.domesticationinnovation.server.entity.DIActivityRegistry;
import com.github.alexthe668.domesticationinnovation.server.entity.ModifedToBeTameable;
import com.github.alexthe668.domesticationinnovation.server.entity.TameableUtils;
import com.github.alexthe668.domesticationinnovation.server.entity.ai.AmphibianFollowOwnerBehavior;
import com.github.alexthe668.domesticationinnovation.server.entity.ai.AmphibianStayBehavior;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.axolotl.AxolotlAi;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(AxolotlAi.class)
public class AxolotlAiMixin {

    @Inject(
            method = {"Lnet/minecraft/world/entity/animal/axolotl/AxolotlAi;makeBrain(Lnet/minecraft/world/entity/ai/Brain;)Lnet/minecraft/world/entity/ai/Brain;"},
            remap = true,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/animal/axolotl/AxolotlAi;initPlayDeadActivity(Lnet/minecraft/world/entity/ai/Brain;)V"
            )
    )
    private static void di_makeBrain(Brain<Axolotl> brain, CallbackInfoReturnable<Brain<?>> cir) {
        brain.addActivity(DIActivityRegistry.AXOLOTL_FOLLOW.get(), ImmutableList.of(Pair.of(0, new AmphibianFollowOwnerBehavior(0.3F, 0.6F)), Pair.of(1, new StartAttacking<>(AxolotlAiMixin::findAttackTargetAxl))));
        brain.addActivity(DIActivityRegistry.AXOLOTL_STAY.get(), ImmutableList.of(Pair.of(0, new AmphibianStayBehavior())));
    }

    @Inject(
            method = {"Lnet/minecraft/world/entity/animal/axolotl/AxolotlAi;updateActivity(Lnet/minecraft/world/entity/animal/axolotl/Axolotl;)V"},
            remap = true,
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    private static void di_updateActivity(Axolotl axolotl, CallbackInfo ci) {
        Brain<Axolotl> brain = axolotl.getBrain();
        Activity activity = brain.getActiveNonCoreActivity().orElse(null);
        if (activity != Activity.PLAY_DEAD && !axolotl.isPlayingDead() && axolotl instanceof ModifedToBeTameable modifedToBeTameable) {
            if (modifedToBeTameable.isStayingStill()) {
                brain.setActiveActivityIfPossible(DIActivityRegistry.AXOLOTL_STAY.get());
                ci.cancel();
            } else if (modifedToBeTameable.isFollowingOwner()) {
                brain.setActiveActivityToFirstValid(ImmutableList.of(Activity.PLAY_DEAD, Activity.FIGHT, DIActivityRegistry.AXOLOTL_FOLLOW.get()));
                ci.cancel();
            }
        }
    }


    @Inject(
            method = {"Lnet/minecraft/world/entity/animal/axolotl/AxolotlAi;getTemptations()Lnet/minecraft/world/item/crafting/Ingredient;"},
            remap = true,
            at = @At(
                    value = "TAIL"
            ),
            cancellable = true
    )
    private static void di_getTemptationItems(CallbackInfoReturnable<Ingredient> cir) {
        cir.setReturnValue(Ingredient.merge(ImmutableList.of(cir.getReturnValue(), Ingredient.of(Items.TROPICAL_FISH))));
    }

    private static Optional<? extends LivingEntity> findAttackTargetAxl(Axolotl p_149299_) {
        return p_149299_.isInLove() ? Optional.empty() : p_149299_.getBrain().getMemory(MemoryModuleType.NEAREST_ATTACKABLE);
    }


    @Inject(
            method = {"Lnet/minecraft/world/entity/animal/axolotl/AxolotlAi;getSpeedModifierChasing(Lnet/minecraft/world/entity/LivingEntity;)F"},
            remap = true,
            at = @At(
                    value = "TAIL"
            ),
            cancellable = true
    )
    private static void di_getSpeedModifierChasing(LivingEntity axolotl, CallbackInfoReturnable<Float> cir) {
        int speedsterLevel = TameableUtils.getEnchantLevel(axolotl, DIEnchantmentRegistry.SPEEDSTER);
        cir.setReturnValue(axolotl.isInWaterOrBubble() ? 0.6F + speedsterLevel * 0.05F : 0.15F + speedsterLevel * 0.1F);
    }

    @Inject(
            method = {"Lnet/minecraft/world/entity/animal/axolotl/AxolotlAi;getSpeedModifierFollowingAdult(Lnet/minecraft/world/entity/LivingEntity;)F"},
            remap = true,
            at = @At(
                    value = "TAIL"
            ),
            cancellable = true
    )
    private static void di_getSpeedModifierFollowingAdult(LivingEntity axolotl, CallbackInfoReturnable<Float> cir) {
        int speedsterLevel = TameableUtils.getEnchantLevel(axolotl, DIEnchantmentRegistry.SPEEDSTER);
        cir.setReturnValue(axolotl.isInWaterOrBubble() ? 0.6F + speedsterLevel * 0.05F : 0.15F + speedsterLevel * 0.1F);
    }

    @Inject(
            method = {"Lnet/minecraft/world/entity/animal/axolotl/AxolotlAi;getSpeedModifier(Lnet/minecraft/world/entity/LivingEntity;)F"},
            remap = true,
            at = @At(
                    value = "TAIL"
            ),
            cancellable = true
    )
    private static void di_getSpeedModifier(LivingEntity axolotl, CallbackInfoReturnable<Float> cir) {
        int speedsterLevel = TameableUtils.getEnchantLevel(axolotl, DIEnchantmentRegistry.SPEEDSTER);
        cir.setReturnValue(axolotl.isInWaterOrBubble() ? 0.5F + speedsterLevel * 0.05F : 0.15F + speedsterLevel * 0.15F);
    }
}
