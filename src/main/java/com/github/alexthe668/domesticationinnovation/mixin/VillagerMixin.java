package com.github.alexthe668.domesticationinnovation.mixin;

import com.github.alexthe668.domesticationinnovation.server.entity.TameableUtils;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Villager.class)
public class VillagerMixin {


    @Inject(
            at = {@At("TAIL")},
            remap = true,
            method = {"Lnet/minecraft/world/entity/npc/Villager;getPlayerReputation(Lnet/minecraft/world/entity/player/Player;)I"},
            cancellable = true
    )
    private void di_getPlayerReputation(Player player, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(cir.getReturnValue() + TameableUtils.getCharismaBonusForOwner(player));
    }
}
