package com.github.alexthe668.domesticationinnovation.mixin;

import com.github.alexthe668.domesticationinnovation.DomesticationMod;
import com.github.alexthe668.domesticationinnovation.server.entity.CommandableMob;
import com.github.alexthe668.domesticationinnovation.server.entity.ModifedToBeTameable;
import com.github.alexthe668.domesticationinnovation.server.entity.ai.*;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
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

@Mixin(Rabbit.class)
public abstract class RabbitMixin extends Animal implements ModifedToBeTameable, CommandableMob {

    @Shadow @Final private static EntityDataAccessor<Integer> DATA_TYPE_ID;

    @Shadow public abstract int getRabbitType();

    private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID = SynchedEntityData.defineId(Rabbit.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Integer> COMMAND = SynchedEntityData.defineId(Rabbit.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> TAMED = SynchedEntityData.defineId(Rabbit.class, EntityDataSerializers.BOOLEAN);

    protected RabbitMixin(EntityType<? extends Animal> type, Level level) {
        super(type, level);
    }

    @Inject(
            at = {@At("HEAD")},
            remap = true,
            method = {"Lnet/minecraft/world/entity/animal/Rabbit;registerGoals()V"}
    )
    private void di_registerGoals(CallbackInfo ci) {
        this.goalSelector.addGoal(1, new Sit2Goal(this));
        this.goalSelector.addGoal(2, new FollowOwner2Goal(this, 2.0D, 10.0F, 3.0F, false));
        this.targetSelector.addGoal(2, new OwnerHurtTarget2Goal(this));
        this.targetSelector.addGoal(3, new OwnerHurtByTarget2Goal(this));
    }

    @Inject(
            at = {@At("TAIL")},
            remap = true,
            method = {"Lnet/minecraft/world/entity/animal/Rabbit;defineSynchedData()V"}
    )
    private void di_registerData(CallbackInfo ci) {
        this.entityData.define(OWNER_UUID, Optional.empty());
        this.entityData.define(COMMAND, 0);
        this.entityData.define(TAMED, false);
    }

    @Inject(
            at = {@At("TAIL")},
            remap = true,
            method = {"Lnet/minecraft/world/entity/animal/Rabbit;addAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V"}
    )
    private void di_writeAdditional(CompoundTag compoundNBT, CallbackInfo ci) {
        compoundNBT.putInt("DICommand", this.getCommand());
        compoundNBT.putBoolean("Tamed", this.isTame());
        if (this.getTameOwnerUUID() != null) {
            compoundNBT.putUUID("Owner", this.getTameOwnerUUID());
        }
    }

    @Inject(
            at = {@At("TAIL")},
            remap = true,
            method = {"Lnet/minecraft/world/entity/animal/Rabbit;readAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V"}
    )
    private void di_readAdditional(CompoundTag compoundNBT, CallbackInfo ci) {
        this.setCommand(compoundNBT.getInt("DICommand"));
        this.setTame(compoundNBT.getBoolean("Tamed"));
        UUID uuid;
        if (compoundNBT.hasUUID("Owner")) {
            uuid = compoundNBT.getUUID("Owner");
        } else {
            String s = compoundNBT.getString("Owner");
            uuid = OldUsersConverter.convertMobOwnerIfNecessary(this.getServer(), s);
        }

        if (uuid != null) {
            try {
                this.setTameOwnerUUID(uuid);
                this.setTame(true);
            } catch (Throwable throwable) {
                this.setTame(false);
            }
        }
    }

    public int getCommand(){
        return this.entityData.get(COMMAND);
    }

    public void setCommand(int i){
        this.entityData.set(COMMAND, i);
    }

    public boolean isTame(){
        return this.entityData.get(TAMED);
    }

    public void setTame(boolean b){
        this.entityData.set(TAMED, b);
    }

    @Nullable
    public UUID getTameOwnerUUID() {
        return DomesticationMod.CONFIG.tameableRabbit.get() ? this.entityData.get(OWNER_UUID).orElse((UUID)null) : null;
    }

    public void setTameOwnerUUID(@Nullable UUID uuid) {
        this.entityData.set(OWNER_UUID, Optional.ofNullable(uuid));
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

    public boolean isValidAttackTarget(LivingEntity target) {
        return this.getRabbitType() == 99;
    }


    @Inject(
            at = {@At("HEAD")},
            remap = true,
            method = {"Lnet/minecraft/world/entity/animal/Rabbit;setRabbitType(I)V"},
            cancellable = true
    )
    private void di_setRabbitType(int type, CallbackInfo ci) {
        if(type == 99){
            ci.cancel();
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(30.0D);
            this.getAttribute(Attributes.ARMOR).setBaseValue(8.0D);
            this.heal(22.0F);
            this.goalSelector.addGoal(4, new RabbitMeleeGoal(this));
            this.targetSelector.addGoal(1, (new HurtByTargetGoal(this)).setAlertOthers());
            if(!this.isTame()){
                this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
                this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Wolf.class, true));
            }else{
                this.targetSelector.addGoal(2, new OwnerHurtTarget2Goal(this));
                this.targetSelector.addGoal(3, new OwnerHurtByTarget2Goal(this));
            }
            if (!this.hasCustomName()) {
                this.setCustomName(new TranslatableComponent(Util.makeDescriptionId("entity", new ResourceLocation("killer_bunny"))));
            }
            this.entityData.set(DATA_TYPE_ID, 99);
        }
    }


}
