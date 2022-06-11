package com.github.alexthe668.domesticationinnovation.mixin;

import com.github.alexthe666.citadel.server.entity.IComandableMob;
import com.github.alexthe668.domesticationinnovation.DomesticationMod;
import com.github.alexthe668.domesticationinnovation.server.entity.ModifedToBeTameable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Animal.class)
public abstract class AnimalMixin extends AgeableMob {


    protected AnimalMixin(EntityType<? extends AgeableMob> type, Level lvl) {
        super(type, lvl);
    }

    @Inject(
            method = {"Lnet/minecraft/world/entity/animal/Animal;mobInteract(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"},
            remap = true,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/AgeableMob;mobInteract(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"
            ),
            cancellable = true
    )
    private void di_onInteract(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if((Mob)this instanceof Fox && this instanceof IComandableMob && this instanceof ModifedToBeTameable tame && tame.isTame() && tame.getTameOwnerUUID().equals(player.getUUID()) && DomesticationMod.CONFIG.tameableFox.get() && DomesticationMod.CONFIG.trinaryCommandSystem.get()){
            player.swing(hand, true);
            cir.setReturnValue(((IComandableMob)this).playerSetCommand(player, (Animal)(Mob)this));
        }
    }
}
