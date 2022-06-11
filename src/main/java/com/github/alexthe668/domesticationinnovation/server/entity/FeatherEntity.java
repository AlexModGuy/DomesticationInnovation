package com.github.alexthe668.domesticationinnovation.server.entity;

import com.github.alexthe666.citadel.server.entity.IComandableMob;
import com.github.alexthe668.domesticationinnovation.DomesticationMod;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages;

import javax.annotation.Nullable;

public class FeatherEntity extends FishingHook {

    private static final EntityDataAccessor<Integer> OWNER_ID = SynchedEntityData.defineId(FeatherEntity.class, EntityDataSerializers.INT);

    public FeatherEntity(EntityType featherType, Level level) {
        super(featherType, level);
    }

    private boolean prevOnGround = false;
    public LivingEntity closestPet = null;

    public FeatherEntity(Player player, Level level) {
        this(DIEntityRegistry.FEATHER.get(), level);
        this.setOwner(player);
        float f = player.getXRot();
        float f1 = player.getYRot();
        float f2 = Mth.cos(-f1 * ((float)Math.PI / 180F) - (float)Math.PI);
        float f3 = Mth.sin(-f1 * ((float)Math.PI / 180F) - (float)Math.PI);
        float f4 = -Mth.cos(-f * ((float)Math.PI / 180F));
        float f5 = Mth.sin(-f * ((float)Math.PI / 180F));
        double d0 = player.getX() - (double)f3 * 0.3D;
        double d1 = player.getEyeY();
        double d2 = player.getZ() - (double)f2 * 0.3D;
        this.moveTo(d0, d1, d2, f1, f);
        Vec3 vec3 = new Vec3((double)(-f3), (double)Mth.clamp(-(f5 / f4), -5.0F, 5.0F), (double)(-f2));
        double d3 = vec3.length();
        vec3 = vec3.multiply(0.6D / d3 + 0.5D + this.random.nextGaussian() * 0.0045D, 0.6D / d3 + 0.5D + this.random.nextGaussian() * 0.0045D, 0.6D / d3 + 0.5D + this.random.nextGaussian() * 0.0045D);
        this.setDeltaMovement(vec3);
        this.setYRot((float)(Mth.atan2(vec3.x, vec3.z) * (double)(180F / (float)Math.PI)));
        this.setXRot((float)(Mth.atan2(vec3.y, vec3.horizontalDistance()) * (double)(180F / (float)Math.PI)));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    public FeatherEntity(PlayMessages.SpawnEntity spawnEntity, Level world) {
        this(DIEntityRegistry.FEATHER.get(), world);
        this.setInvulnerable(true);
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }


    protected void defineSynchedData() {
        super.defineSynchedData();
        this.getEntityData().define(OWNER_ID, 0);
    }

    public void tick() {
        prevOnGround = onGround;
        super.tick();
        this.setDeltaMovement(this.getDeltaMovement().multiply(0.8D, 0.8D, 0.8D));
        if(this.isOnGround() && !level.isClientSide){
            if(!prevOnGround){
                closestPet = findClosestPetOf(this.getPlayerOwner());
            }
            if(closestPet instanceof Mob mob && isPetAmbulatory(mob)){
                if(closestPet.distanceTo(this) <= closestPet.getBbWidth() + 1.0F){
                    if(closestPet.distanceTo(this) > 0.1D){
                        mob.lookAt(this, 40, 90);
                        Vec3 move = new Vec3(this.getX() - mob.getX(), this.getY() - (double) mob.getY(), this.getZ() - mob.getZ());
                        mob.setDeltaMovement(mob.getDeltaMovement().add(move.normalize().scale(0.05D)));
                    }
                    mob.getNavigation().stop();
                }else{
                    mob.getNavigation().moveTo(this, 1.0D);
                }
            }
        }
    }

    private LivingEntity findClosestPetOf(Player owner) {
        LivingEntity closestValid = null;
        for (Entity entity : this.level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(21.0D))) {
            if (TameableUtils.isTamed(entity) && TameableUtils.isPetOf(owner, entity) && isPetAmbulatory((LivingEntity) entity) && ((LivingEntity)entity).hasLineOfSight(this)) {
                if (closestValid == null || this.distanceTo(entity) < this.distanceTo(closestValid)) {
                    closestValid = (LivingEntity)entity;
                }
            }
        }
        return closestValid;
    }

    protected boolean canHitEntity(Entity entity) {
        return false;
    }

    protected void onHitEntity(EntityHitResult result) {

    }

    @Override
    public Entity getOwner(){
        Entity prev = super.getOwner();
        if(this.entityData.get(OWNER_ID) != -1){
            return level.getEntity(this.entityData.get(OWNER_ID));
        }else{
            return prev;
        }
    }

    @Override
    public void setOwner(@Nullable Entity owner) {
        super.setOwner(owner);
        this.entityData.set(OWNER_ID,owner == null ? -1 : owner.getId());
    }

    @Nullable
    @Override
    public Player getPlayerOwner() {
        if(level.isClientSide  && this.entityData.get(OWNER_ID) != -1){
            Entity entity = level.getEntity(this.entityData.get(OWNER_ID));
            return entity instanceof Player ? (Player) entity : null;
        }else{
            Entity entity = this.getOwner();
            return entity instanceof Player ? (Player)entity : null;
        }
    }

    public boolean isPetAmbulatory(LivingEntity entity){
        if(entity instanceof TamableAnimal && (((TamableAnimal) entity).isOrderedToSit() || ((TamableAnimal) entity).isInSittingPose())){
            return false;
        }
        if(entity instanceof IComandableMob && ((IComandableMob) entity).getCommand() == 1 && DomesticationMod.CONFIG.trinaryCommandSystem.get()){
            return false;
        }
        return true;
    }
}
