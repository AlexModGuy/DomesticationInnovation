package com.github.alexthe668.domesticationinnovation.server.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class RecallBallEntity extends Entity {

    private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID = SynchedEntityData.defineId(RecallBallEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<String> CONTAINED_ENTITY_TYPE = SynchedEntityData.defineId(RecallBallEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<CompoundTag> CONTAINED_ENTITY_DATA = SynchedEntityData.defineId(RecallBallEntity.class, EntityDataSerializers.COMPOUND_TAG);
    private static final EntityDataAccessor<Boolean> OPENED = SynchedEntityData.defineId(RecallBallEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean>  FINISHED = SynchedEntityData.defineId(RecallBallEntity.class, EntityDataSerializers.BOOLEAN);
    private float prevOpenProgress;
    private float openProgress;

    public RecallBallEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    public RecallBallEntity(PlayMessages.SpawnEntity spawnEntity, Level world) {
        this(DIEntityRegistry.RECALL_BALL.get(), world);
        this.setInvulnerable(true);
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }


    @Override
    protected void defineSynchedData() {
        this.entityData.define(OWNER_UUID, Optional.empty());
        this.entityData.define(CONTAINED_ENTITY_TYPE, "minecraft:pig");
        this.entityData.define(CONTAINED_ENTITY_DATA, new CompoundTag());
        this.entityData.define(OPENED, false);
        this.entityData.define(FINISHED, false);
    }

    public InteractionResult interactAt(Player player, Vec3 vec3, InteractionHand hand) {
        return interact(player, hand);
    }

    public boolean isPickable() {
        return !this.isFinished();
    }

    public InteractionResult interact(Player player, InteractionHand hand) {
        if(!this.isFinished()){
            if(this.getOwnerUUID() == null){
                this.discard();
            }else if(player.getUUID().equals(this.getOwnerUUID()) && !this.entityData.get(OPENED)){
                this.playSound(SoundEvents.ENDER_CHEST_OPEN, 1.0F, 1.5F);
                this.entityData.set(OPENED, true);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    public void tick(){
        super.tick();
        this.setYRot(this.getYRot() + 1);
        this.setXRot(0);
        prevOpenProgress = openProgress;
        if(this.isInWaterOrBubble() || this.isInLava()){
            this.setPos(this.position().add(0, 0.08, 0));
        }
        if(this.entityData.get(OPENED) && openProgress < 1F){
            openProgress += 0.1F;
        }
        if(!this.entityData.get(OPENED) && openProgress > 0F){
            openProgress -= 0.1F;
        }
        if(random.nextFloat() < 0.4F){
            this.level.addParticle(ParticleTypes.PORTAL, this.getRandomX(0.5D), this.getRandomY() - 0.25D, this.getRandomZ(0.5D), (this.random.nextDouble() - 0.5D) * 2.0D, -this.random.nextDouble(), (this.random.nextDouble() - 0.5D) * 2.0D);
        }
        if(this.entityData.get(OPENED) && openProgress >= 1F && !this.isFinished()){
            if(!level.isClientSide){
                EntityType type = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(this.getContainedEntityType()));
                if(type != null){
                    Entity entity = type.create(level);
                    if(entity instanceof LivingEntity alive){
                        alive.readAdditionalSaveData(this.getContainedData());
                        alive.setHealth(Math.max(2, alive.getMaxHealth() * 0.25F));
                        alive.setYRot(random.nextFloat() * 360 - 180);
                        alive.copyPosition(this);
                        level.addFreshEntity(alive);
                    }
                    this.entityData.set(FINISHED, true);
                    this.entityData.set(OPENED, false);
                }
            }
        }
        if(this.isFinished() && openProgress <= 0.01F){
            this.discard();
        }
    }

    public boolean isNoGravity() {
        return true;
    }

    public boolean shouldBeSaved() {
        return !this.isFinished() && super.shouldBeSaved();
    }

    public boolean isFinished() {
        return this.entityData.get(FINISHED);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundNBT) {
        UUID uuid;
        if (compoundNBT.hasUUID("Owner")) {
            uuid = compoundNBT.getUUID("Owner");
        } else {
            String s = compoundNBT.getString("Owner");
            uuid = OldUsersConverter.convertMobOwnerIfNecessary(this.getServer(), s);
        }

        if (uuid != null) {
            try {
                this.setOwnerUUID(uuid);
            } catch (Throwable throwable) {
            }
        }
        this.setContainedEntityType(compoundNBT.getString("ContainedEntityType"));
        if (!compoundNBT.getCompound("ContainedData").isEmpty()) {
            this.setContainedData(compoundNBT.getCompound("ContainedData"));
        }
        this.entityData.set(FINISHED, compoundNBT.getBoolean("Finished"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundNBT) {
        if (this.getOwnerUUID() != null) {
            compoundNBT.putUUID("Owner", this.getOwnerUUID());
        }
        compoundNBT.putString("ContainedEntityType", this.getContainedEntityType());
        compoundNBT.put("ContainedData", this.getContainedData());
        compoundNBT.putBoolean( "Finished", this.isFinished());
    }

    @Nullable
    public UUID getOwnerUUID() {
        return this.entityData.get(OWNER_UUID).orElse((UUID)null);
    }

    public void setOwnerUUID(@Nullable UUID uuid) {
        this.entityData.set(OWNER_UUID, Optional.ofNullable(uuid));
    }

    public String getContainedEntityType(){
        return this.entityData.get(CONTAINED_ENTITY_TYPE);
    }

    public void setContainedEntityType(String containedEntityType) {
        this.entityData.set(CONTAINED_ENTITY_TYPE, containedEntityType);
    }

    public CompoundTag getContainedData(){
        return this.entityData.get(CONTAINED_ENTITY_DATA);
    }

    public void setContainedData(CompoundTag containedData) {
        this.entityData.set(CONTAINED_ENTITY_DATA, containedData);
    }

    public float getOpenProgress(float f){
        return Mth.lerp(f, prevOpenProgress, openProgress);
    }
}
