package com.github.alexthe668.domesticationinnovation.mixin;

import com.github.alexthe666.citadel.server.entity.IComandableMob;
import com.github.alexthe668.domesticationinnovation.DomesticationMod;
import com.github.alexthe668.domesticationinnovation.server.entity.ModifedToBeTameable;
import com.github.alexthe668.domesticationinnovation.server.entity.ai.FollowOwner2Goal;
import com.github.alexthe668.domesticationinnovation.server.entity.ai.OwnerHurtTarget2Goal;
import com.github.alexthe668.domesticationinnovation.server.entity.ai.Sit2Goal;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

@Mixin(Fox.class)
public abstract class FoxMixin extends Animal implements ModifedToBeTameable, IComandableMob {

    @Shadow @Final private static EntityDataAccessor<Optional<UUID>> DATA_TRUSTED_ID_0;
    @Shadow @Final private static EntityDataAccessor<Optional<UUID>> DATA_TRUSTED_ID_1;
    @Shadow abstract void addTrustedUUID(UUID uuid);
    @Shadow public abstract void setSitting(boolean p_28611_);
    @Shadow abstract void setSleeping(boolean p_28627_);

    private static final EntityDataAccessor<Integer> COMMAND = SynchedEntityData.defineId(Fox.class, EntityDataSerializers.INT);

    protected FoxMixin(EntityType<? extends Animal> foxType, Level level) {
        super(foxType, level);
    }

    @Inject(
            at = {@At("HEAD")},
            remap = true,
            method = {"Lnet/minecraft/world/entity/animal/Fox;registerGoals()V"}
    )
    private void di_registerGoals(CallbackInfo ci) {
        this.goalSelector.addGoal(1, new Sit2Goal(this));
        this.goalSelector.addGoal(2, new FollowOwner2Goal(this, 1.0D, 10.0F, 3.0F, false));
        this.targetSelector.addGoal(1, new OwnerHurtTarget2Goal(this));
    }

    @Inject(
            at = {@At("TAIL")},
            remap = true,
            method = {"Lnet/minecraft/world/entity/animal/Fox;defineSynchedData()V"}
    )
    private void di_registerData(CallbackInfo ci) {
        this.entityData.define(COMMAND, 0);
    }

    @Inject(
            at = {@At("TAIL")},
            remap = true,
            method = {"Lnet/minecraft/world/entity/animal/Fox;addAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V"}
    )
    private void di_writeAdditional(CompoundTag compoundNBT, CallbackInfo ci) {
        compoundNBT.putInt("DICommand", this.getCommand());
    }

    @Inject(
            at = {@At("TAIL")},
            remap = true,
            method = {"Lnet/minecraft/world/entity/animal/Fox;readAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V"}
    )
    private void di_readAdditional(CompoundTag compoundNBT, CallbackInfo ci) {
        this.setCommand(compoundNBT.getInt("DICommand"));
    }

    public int getCommand(){
        return this.entityData.get(COMMAND);
    }

    public void setCommand(int i){
        this.entityData.set(COMMAND, i);
    }

    public boolean isTame(){
        return (this.entityData.get(DATA_TRUSTED_ID_0).isPresent() || this.entityData.get(DATA_TRUSTED_ID_1).isPresent()) && DomesticationMod.CONFIG.tameableFox.get();
    }

    public void setTame(boolean value){

    }

    @Inject(
            at = @At(
                    shift = At.Shift.BEFORE,
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;finishUsingItem(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/item/ItemStack;"
            ),
            remap = true,
            method = {"Lnet/minecraft/world/entity/animal/Fox;aiStep()V"}
    )
    private void di_aiStep(CallbackInfo ci) {
        ItemStack stack = this.getItemBySlot(EquipmentSlot.MAINHAND);
        if(!stack.isEmpty() && stack.getItem().isEdible() && stack.getItem().getFoodProperties() != null){
            this.heal(stack.getItem().getFoodProperties().getNutrition() * 2);
        }
    }

    @Inject(
            at = {@At("TAIL")},
            remap = true,
            method = {"Lnet/minecraft/world/entity/animal/Fox;aiStep()V"}
    )
    private void di_aiStep_2(CallbackInfo ci) {
        if(this.isFollowingOwner()){
            this.setSleeping(false);
            this.setSitting(false);
        }
    }

    @Nullable
    public UUID getTameOwnerUUID(){
        if (this.entityData.get(DATA_TRUSTED_ID_0).isPresent()) {
            return this.entityData.get(DATA_TRUSTED_ID_0).get();
        } else {
            return this.entityData.get(DATA_TRUSTED_ID_1).orElse(null);
        }
    }

    public void setTameOwnerUUID(@Nullable UUID uuid){
        addTrustedUUID(uuid);
    }

    @Nullable
    public LivingEntity getTameOwner() {
        try {
            UUID uuid = this.getTameOwnerUUID();
            return uuid == null ? null : this.level.getPlayerByUUID(uuid);
        } catch (IllegalArgumentException illegalargumentexception) {
            return null;
        }
    }

    public boolean isFollowingOwner(){
        return this.getCommand() == 2 && DomesticationMod.CONFIG.trinaryCommandSystem.get();
    }

    public boolean isStayingStill(){
        return this.getCommand() == 1 && DomesticationMod.CONFIG.trinaryCommandSystem.get();
    }

    public boolean isValidAttackTarget(LivingEntity target){
        return true;
    }

    @Override
    public void sendCommandMessage(Player owner, int command, Component name) {
        owner.displayClientMessage(Component.translatable("message.domesticationinnovation.command_" + command, name), true);
    }
}
