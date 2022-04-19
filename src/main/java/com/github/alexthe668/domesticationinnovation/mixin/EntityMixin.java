package com.github.alexthe668.domesticationinnovation.mixin;

import com.github.alexthe668.domesticationinnovation.server.enchantment.DIEnchantmentRegistry;
import com.github.alexthe668.domesticationinnovation.server.entity.PsychicWallEntity;
import com.github.alexthe668.domesticationinnovation.server.entity.TameableUtils;
import com.google.common.collect.ImmutableList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.Predicate;

@Mixin(Entity.class)
public class EntityMixin {

    @Inject(
            method = {"Lnet/minecraft/world/entity/Entity;fireImmune()Z"},
            remap = true,
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    private void di_isFireImmune(CallbackInfoReturnable<Boolean> cir) {
        Entity us = (Entity)((Object)this);
        if(TameableUtils.isTamed(us) && TameableUtils.hasEnchant((LivingEntity) us, DIEnchantmentRegistry.FIREPROOF)){
            cir.setReturnValue(true);
        }
    }

    @Inject(
            method = {"Lnet/minecraft/world/entity/Entity;isPushedByFluid()Z"},
            remap = true,
            at = @At(value = "HEAD"),
            cancellable = true
    )
    protected void di_pushedByWater(CallbackInfoReturnable<Boolean> cir) {
        if((Object)this instanceof LivingEntity && TameableUtils.isTamed((LivingEntity)(Object)this) && TameableUtils.hasEnchant((LivingEntity)(Object)this, DIEnchantmentRegistry.AMPHIBIOUS)){
            cir.setReturnValue(false);
        }
    }

    @Inject(
            method = {"Lnet/minecraft/world/entity/Entity;isAlliedTo(Lnet/minecraft/world/entity/Entity;)Z"},
            remap = true,
            at = @At(value = "HEAD"),
            cancellable = true
    )
    protected void di_isAlliedTo(Entity other, CallbackInfoReturnable<Boolean> cir) {
        if(TameableUtils.isTamed(other) && TameableUtils.isTamed((Entity)(Object)this) && TameableUtils.hasSameOwnerAs((LivingEntity) other, (Entity)(Object)this)){
            cir.setReturnValue(true);
        }
    }

    @Inject(
            method = {"Lnet/minecraft/world/entity/Entity;canCollideWith(Lnet/minecraft/world/entity/Entity;)Z"},
            remap = true,
            at = @At(value = "HEAD"),
            cancellable = true
    )
    protected void di_canCollideWith(Entity other, CallbackInfoReturnable<Boolean> cir) {
        if(other instanceof PsychicWallEntity && ((PsychicWallEntity)other).isSameTeam((Entity)(Object)this)){
            cir.setReturnValue(false);
        }
    }

        @Redirect(
            method = {"Lnet/minecraft/world/entity/Entity;collide(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;"},
            remap = true,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;getEntityCollisions(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;"
            )
    )
    protected List<VoxelShape> di_getEntityCollisions(Level level, Entity entity, AABB aabb) {
        if (aabb.getSize() < 1.0E-7D) {
            return List.of();
        } else {
            Predicate<Entity> predicate = entity == null ? EntitySelector.CAN_BE_COLLIDED_WITH : EntitySelector.NO_SPECTATORS.and(entity::canCollideWith);
            List<Entity> list = level.getEntities(entity, aabb.inflate(1.0E-7D), predicate);
            if (list.isEmpty()) {
                return List.of();
            } else {
                ImmutableList.Builder<VoxelShape> builder = ImmutableList.builderWithExpectedSize(list.size());
                for(Entity entity2 : list) {
                    if(!(entity2 instanceof PsychicWallEntity) || !((PsychicWallEntity)entity2).isSameTeam(entity)){
                        builder.add(Shapes.create(entity2.getBoundingBox()));
                    }
                }
                return builder.build();
            }
        }
    }
}
