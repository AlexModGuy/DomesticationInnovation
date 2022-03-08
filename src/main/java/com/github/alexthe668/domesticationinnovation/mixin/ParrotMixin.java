package com.github.alexthe668.domesticationinnovation.mixin;

import com.github.alexthe668.domesticationinnovation.DomesticationMod;
import com.github.alexthe668.domesticationinnovation.server.entity.CommandableMob;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Parrot.class)
public abstract class ParrotMixin extends TamableAnimal implements CommandableMob {
    private static final EntityDataAccessor<Integer> COMMAND = SynchedEntityData.defineId(Parrot.class, EntityDataSerializers.INT);

    protected ParrotMixin(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
    }

    @Inject(
            at = {@At("TAIL")},
            remap = true,
            method = {"Lnet/minecraft/world/entity/animal/Parrot;defineSynchedData()V"}
    )
    private void di_registerData(CallbackInfo ci) {
        this.entityData.define(COMMAND, 0);
    }

    @Inject(
            at = {@At("TAIL")},
            remap = true,
            method = {"Lnet/minecraft/world/entity/animal/Parrot;addAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V"}
    )
    private void di_writeAdditional(CompoundTag compoundNBT, CallbackInfo ci) {
        compoundNBT.putInt("DICommand", this.getCommand());
    }

    @Inject(
            at = {@At("TAIL")},
            remap = true,
            method = {"Lnet/minecraft/world/entity/animal/Parrot;readAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V"}
    )
    private void di_readAdditional(CompoundTag compoundNBT, CallbackInfo ci) {
        this.setCommand(compoundNBT.getInt("DICommand"));
    }

    @Inject(
            method = {"Lnet/minecraft/world/entity/animal/Parrot;mobInteract(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"},
            remap = true,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/animal/Parrot;setOrderedToSit(Z)V"
            ),
            cancellable = true
    )
    private void di_onInteract(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if(DomesticationMod.CONFIG.trinaryCommandSystem.get()){
            this.jumping = false;
            this.navigation.stop();
            this.setTarget((LivingEntity)null);
            player.swing(hand, true);
            cir.setReturnValue(this.playerSetCommand(player, this));
        }
    }

    public int getCommand(){
        return this.entityData.get(COMMAND);
    }

    public void setCommand(int i){
        this.entityData.set(COMMAND, i);
    }


}
