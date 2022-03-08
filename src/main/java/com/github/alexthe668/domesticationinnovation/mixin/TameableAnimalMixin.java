package com.github.alexthe668.domesticationinnovation.mixin;

import com.github.alexthe668.domesticationinnovation.server.entity.TameableUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TamableAnimal.class)
public abstract class TameableAnimalMixin extends Animal {

    protected TameableAnimalMixin(EntityType<? extends Animal> an, Level lvl) {
        super(an, lvl);
    }

    @Inject(
            method = {"Lnet/minecraft/world/entity/TamableAnimal;isAlliedTo(Lnet/minecraft/world/entity/Entity;)Z"},
            remap = true,
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    private void di_isAlliedTo(Entity other, CallbackInfoReturnable<Boolean> cir) {
        if(TameableUtils.hasSameOwnerAs(this, other)){
            cir.setReturnValue(true);
        }
    }
}
