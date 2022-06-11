package com.github.alexthe668.domesticationinnovation.mixin;

import com.github.alexthe668.domesticationinnovation.server.entity.ModifedToBeTameable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.frog.ShootTongue;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(ShootTongue.class)
public class ShootTongueMixin {

    @Inject(
            method = {"Lnet/minecraft/world/entity/animal/frog/ShootTongue;checkExtraStartConditions(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/animal/frog/Frog;)Z"},
            remap = true,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/animal/frog/Frog;canEat(Lnet/minecraft/world/entity/LivingEntity;)Z"
            ),
            cancellable = true
    )
    private void di_checkExtraStartConditions(ServerLevel level, Frog frog, CallbackInfoReturnable<Boolean> cir){
        if(frog instanceof ModifedToBeTameable tameable && tameable.isTame()){
            cir.setReturnValue(true);
        }
    }

    @Inject(
            method = {"Lnet/minecraft/world/entity/animal/frog/ShootTongue;eatEntity(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/animal/frog/Frog;)V"},
            remap = true,
            at = @At("HEAD"),
            cancellable = true
    )
    private void di_eatEntity(ServerLevel level, Frog frog, CallbackInfo ci) {
        if(frog instanceof ModifedToBeTameable tameable && tameable.isTame()){
            ci.cancel();
            frog.playSound(SoundEvents.FROG_TONGUE);
            Optional<Entity> optional = frog.getTongueTarget();
            if (optional.isPresent()) {
                Entity entity = optional.get();
                if (entity.isAlive()) {
                    frog.doHurtTarget(entity);
                    if (!entity.isAlive() && Frog.canEat(frog)) {
                        entity.remove(Entity.RemovalReason.KILLED);
                    }
                }
            }

        }

    }
}
