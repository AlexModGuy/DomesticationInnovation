package com.github.alexthe668.domesticationinnovation.server.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages;

import java.util.ArrayList;
import java.util.List;

public class ChainLightningEntity extends Entity {

    private static final EntityDataAccessor<Integer> CREATOR_ID = SynchedEntityData.defineId(ChainLightningEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FROM_ID = SynchedEntityData.defineId(ChainLightningEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> TARGET_COUNT = SynchedEntityData.defineId(ChainLightningEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CURRENT_TARGET_ID = SynchedEntityData.defineId(ChainLightningEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DIES_IN = SynchedEntityData.defineId(ChainLightningEntity.class, EntityDataSerializers.INT);
    private List<Entity> previouslyShocked = new ArrayList<>();
    private boolean hasShocked = false;
    private boolean hasChained = false;

    public ChainLightningEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    public ChainLightningEntity(PlayMessages.SpawnEntity spawnEntity, Level world) {
        this(DIEntityRegistry.CHAIN_LIGHTNING.get(), world);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return (Packet<ClientGamePacketListener>) NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(CREATOR_ID, -1);
        this.entityData.define(FROM_ID, -1);
        this.entityData.define(TARGET_COUNT, 1);
        this.entityData.define(CURRENT_TARGET_ID, -1);
        this.entityData.define(DIES_IN, 5);
    }

    @Override
    public void tick() {
        super.tick();
        Entity creator = getCreatorEntity();
        Entity current = getToEntity();
        if (creator instanceof LivingEntity) {
            if (current != null) {
                this.setPos(new Vec3(current.getX(), current.getY() + current.getBbHeight() * 0.5F, current.getZ()));
                if(!level.isClientSide){
                    if(!hasShocked){
                        hasShocked = true;
                        current.hurt(current.damageSources().lightningBolt(), 3);
                    }
                }
            }
        }
        if(!level.isClientSide){
            if(!hasChained){
                if(this.getChainsLeft() > 0 && creator instanceof LivingEntity) {
                    Entity closestValid = null;
                    for (Entity entity : this.level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8.0D))) {
                        if (!entity.equals(creator) && !TameableUtils.hasSameOwnerAs((LivingEntity) creator, entity) && !previouslyShocked.contains(entity) && !creator.isAlliedTo(entity) && entity instanceof Mob && this.hasLineOfSight(entity)) {
                            if (closestValid == null || this.distanceTo(entity) < this.distanceTo(closestValid)) {
                                closestValid = entity;
                            }
                        }
                    }
                    if(closestValid != null){
                        createLightningAt(closestValid);
                        hasChained = true;
                    }
                }
            }
            if(getDiesInTicks() > 0){
                this.setDiesInTicks(getDiesInTicks() - 1);
            }else{
                this.remove(RemovalReason.DISCARDED);
            }
        }
    }

    private boolean hasLineOfSight(Entity entity) {
        if (entity.level != this.level) {
            return false;
        } else {
            Vec3 vec3 = new Vec3(this.getX(), this.getEyeY(), this.getZ());
            Vec3 vec31 = new Vec3(entity.getX(), entity.getEyeY(), entity.getZ());
            if (vec31.distanceTo(vec3) > 128.0D) {
                return false;
            } else {
                return this.level.clip(new ClipContext(vec3, vec31, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.MISS;
            }
        }     
    }

    private void createLightningAt(Entity closestValid) {
        ChainLightningEntity child = DIEntityRegistry.CHAIN_LIGHTNING.get().create(this.level);
        child.previouslyShocked = new ArrayList<>(previouslyShocked);
        child.previouslyShocked.add(closestValid);
        child.setCreatorEntityID(this.getCreatorEntityID());
        child.setFromEntityID(this.getId());
        child.setToEntityID(closestValid.getId());
        child.copyPosition(closestValid);
        child.setChainsLeft(this.getChainsLeft() - 1);
        this.level.addFreshEntity(child);
    }

    public int getCreatorEntityID() {
        return this.entityData.get(CREATOR_ID);
    }

    public void setCreatorEntityID(int id) {
        this.entityData.set(CREATOR_ID, id);
    }

    public Entity getCreatorEntity() {
        return getCreatorEntityID() == -1 ? null : this.level.getEntity(getCreatorEntityID());
    }

    public int getFromEntityID() {
        return this.entityData.get(FROM_ID);
    }

    public void setFromEntityID(int id) {
        this.entityData.set(FROM_ID, id);
    }

    public Entity getFromEntity() {
        return getFromEntityID() == -1 ? null : this.level.getEntity(getFromEntityID());
    }

    public int getToEntityID() {
        return this.entityData.get(CURRENT_TARGET_ID);
    }

    public void setToEntityID(int id) {
        this.entityData.set(CURRENT_TARGET_ID, id);
    }

    public Entity getToEntity() {
        return getToEntityID() == -1 ? null : this.level.getEntity(getToEntityID());
    }

    public int getChainsLeft() {
        return this.entityData.get(TARGET_COUNT);
    }

    public void setChainsLeft(int i) {
        this.entityData.set(TARGET_COUNT, i);
    }

    public int getDiesInTicks() {
        return this.entityData.get(DIES_IN);
    }

    public void setDiesInTicks(int i) {
        this.entityData.set(DIES_IN, i);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag p_20052_) {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag p_20139_) {

    }
}
