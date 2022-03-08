package com.github.alexthe668.domesticationinnovation.mixin;

import com.github.alexthe668.domesticationinnovation.server.entity.FeatherEntity;
import com.github.alexthe668.domesticationinnovation.server.item.DIItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FishingHook.class)
public abstract class FishingHookMixin extends Projectile {

    protected FishingHookMixin(EntityType<? extends Projectile> type, Level level) {
        super(type, level);
    }

    @Redirect(
            method = {"Lnet/minecraft/world/entity/projectile/FishingHook;tick()V"},
            remap = true,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;getFluidState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/material/FluidState;"
            )
    )
    private FluidState di_getFluidState(Level level, BlockPos pos) {
        return (Projectile)this instanceof FeatherEntity ? Fluids.EMPTY.defaultFluidState() : level.getFluidState(pos);
    }

    @Inject(
            method = {"Lnet/minecraft/world/entity/projectile/FishingHook;shouldStopFishing(Lnet/minecraft/world/entity/player/Player;)Z"},
            remap = true,
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    private void di_shouldStopFishing(Player player, CallbackInfoReturnable<Boolean> cir) {
        if((Projectile)this instanceof FeatherEntity){
            ItemStack itemstack = player.getMainHandItem();
            ItemStack itemstack1 = player.getOffhandItem();
            boolean flag = itemstack.is(DIItemRegistry.FEATHER_ON_A_STICK.get());
            boolean flag1 = itemstack1.is(DIItemRegistry.FEATHER_ON_A_STICK.get());
            if (!this.isRemoved() && this.isAlive() && (flag || flag1) && this.distanceToSqr(player) < 1024.0D) {
                cir.setReturnValue(false);
            } else {
                this.discard();
                cir.setReturnValue(true);
            }
        }
    }
}
