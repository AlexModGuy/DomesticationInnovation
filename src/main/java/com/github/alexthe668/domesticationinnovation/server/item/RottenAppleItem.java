package com.github.alexthe668.domesticationinnovation.server.item;

import com.github.alexthe668.domesticationinnovation.DomesticationMod;
import com.github.alexthe668.domesticationinnovation.server.misc.DICreativeModeTab;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.ZombieHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class RottenAppleItem extends Item {

    public RottenAppleItem() {
        super(new Item.Properties().tab(DICreativeModeTab.INSTANCE).food((new FoodProperties.Builder()).nutrition(3).saturationMod(0.3F).effect(new MobEffectInstance(MobEffects.POISON, 100, 1), 1.0F).build()));
    }

    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
        if(entity.getType() == EntityType.HORSE && net.minecraftforge.event.ForgeEventFactory.canLivingConvert(entity, EntityType.ZOMBIE_HORSE, (timer) -> {})){
            player.swing(hand);
            Horse horse = (Horse)entity;
            horse.playSound(SoundEvents.HORSE_DEATH, 0.8F, horse.getVoicePitch());
            horse.playSound(SoundEvents.ZOMBIE_INFECT, 0.8F, horse.getVoicePitch());
            CompoundTag horseExtras = new CompoundTag();
            if(!horse.getArmor().isEmpty()){
                horse.spawnAtLocation(horse.getArmor().copy());
                horse.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
            }
            horse.addAdditionalSaveData(horseExtras);
            for(int i = 0; i < 6 + horse.getRandom().nextInt(5); i++){
                horse.level.addParticle(ParticleTypes.SNEEZE, horse.getRandomX(1.0F), horse.getRandomY(), horse.getRandomZ(1.0F), 0F, 0F, 0F);
            }
            ZombieHorse zombie = EntityType.ZOMBIE_HORSE.create(horse.level);
            if(horse.isLeashed()){
                zombie.setLeashedTo(horse.getLeashHolder(), true);
            }
            zombie.moveTo(horse.getX(), horse.getY(), horse.getZ(), horse.getYRot(), horse.getXRot());
            zombie.setNoAi(horse.isNoAi());
            zombie.setBaby(horse.isBaby());
            if (horse.hasCustomName()) {
                zombie.setCustomName(horse.getCustomName());
                zombie.setCustomNameVisible(horse.isCustomNameVisible());
            }
            zombie.readAdditionalSaveData(horseExtras);
            zombie.setPersistenceRequired();
            net.minecraftforge.event.ForgeEventFactory.onLivingConvert(horse, zombie);
            player.level.addFreshEntity(zombie);
            horse.discard();
            if(!player.isCreative()){
                stack.shrink(1);
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

}
