package com.github.alexthe668.domesticationinnovation.mixin;

import com.github.alexthe668.domesticationinnovation.server.enchantment.DIEnchantmentRegistry;
import com.github.alexthe668.domesticationinnovation.server.entity.TameableUtils;
import com.github.alexthe668.domesticationinnovation.server.entity.ai.AmphibiousPathNavigation;
import com.github.alexthe668.domesticationinnovation.server.entity.ai.AquaticMoveControl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class MobMixin extends LivingEntity {

    @Shadow protected MoveControl moveControl;
    @Shadow protected PathNavigation navigation;
    @Shadow  public abstract void setPathfindingMalus(BlockPathTypes water, float v);
    private boolean hasWaterEnchantNavigator = false;
    private MoveControl prevMoveControl;
    private PathNavigation prevNavigation;

    protected MobMixin(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
    }

    @Inject(
            method = {"Lnet/minecraft/world/entity/Mob;tick()V"},
            remap = true,
            at = @At(value = "TAIL")
    )
    private void di_tick(CallbackInfo ci) {
        if(TameableUtils.isTamed(this) && !level.isClientSide){
            if(TameableUtils.hasEnchant(this, DIEnchantmentRegistry.AMPHIBIOUS) && !((LivingEntity)this instanceof Axolotl)){
                if(!hasWaterEnchantNavigator && this.isInWaterOrBubble()){
                    this.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
                    this.setPathfindingMalus(BlockPathTypes.WATER_BORDER, 0.0F);
                    prevMoveControl = moveControl;
                    prevNavigation = navigation;
                    moveControl = new AquaticMoveControl((Mob)(LivingEntity)this);
                    navigation = new AmphibiousPathNavigation((Mob)(LivingEntity)this, level);
                    hasWaterEnchantNavigator = true;
                }
                if(hasWaterEnchantNavigator && !this.isInWaterOrBubble()) {
                    moveControl = prevMoveControl;
                    navigation = prevNavigation;
                    hasWaterEnchantNavigator = false;
                }
            }else if(hasWaterEnchantNavigator){
                moveControl = prevMoveControl;
                navigation = prevNavigation;
                hasWaterEnchantNavigator = false;
            }
        }
    }


    @Inject(
            method = {"Lnet/minecraft/world/entity/Mob;pickUpItem(Lnet/minecraft/world/entity/item/ItemEntity;)V"},
            remap = true,
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private void di_pickUpItem(ItemEntity item, CallbackInfo ci) {
        if(TameableUtils.isTamed(this) && TameableUtils.hasEnchant(this, DIEnchantmentRegistry.LINKED_INVENTORY)){
            Entity owner = TameableUtils.getOwnerOf(this);
            if(owner instanceof Player){
                ci.cancel();
                if(((Player) owner).addItem(item.getItem())){
                    item.discard();
                }else{
                    item.copyPosition(owner);
                }
            }

        }
    }

    @Inject(
            method = {"Lnet/minecraft/world/entity/Mob;playAmbientSound()V"},
            remap = true,
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private void di_playAmbientSound(CallbackInfo ci) {
        if(TameableUtils.isTamed(this) && TameableUtils.hasEnchant(this, DIEnchantmentRegistry.MUFFLED)){
            ci.cancel();
        }
    }
}
