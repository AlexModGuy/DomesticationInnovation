package com.github.alexthe668.domesticationinnovation.server.item;

import com.github.alexthe668.domesticationinnovation.DomesticationMod;
import com.github.alexthe668.domesticationinnovation.server.entity.TameableUtils;
import com.github.alexthe668.domesticationinnovation.server.misc.DICreativeModeTab;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.entity.animal.horse.ZombieHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

public class SinisterCarrotItem extends Item {

    public SinisterCarrotItem() {
        super(new Item.Properties().tab(DICreativeModeTab.INSTANCE).rarity(Rarity.UNCOMMON).food((new FoodProperties.Builder()).nutrition(1).saturationMod(0.3F).effect(new MobEffectInstance(MobEffects.WITHER, 100), 1.0F).build()));
    }

    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
        if(entity.getType() == EntityType.RABBIT && TameableUtils.isTamed(entity) && TameableUtils.isPetOf(player, entity) && net.minecraftforge.event.ForgeEventFactory.canLivingConvert(entity, EntityType.RABBIT, (timer) -> {})){
            if(entity instanceof Rabbit rabbit && rabbit.getRabbitType() != 99){
                player.swing(hand);
                rabbit.playSound(SoundEvents.RABBIT_ATTACK, 0.8F, rabbit.getVoicePitch());
                rabbit.playSound(SoundEvents.ZOMBIE_INFECT, 0.8F, rabbit.getVoicePitch());
                rabbit.setRabbitType(99);
                if(!player.isCreative()){
                    stack.shrink(1);
                }
                return InteractionResult.CONSUME;
            }
        }
        if(entity.getType() == EntityType.ZOMBIE_HORSE && net.minecraftforge.event.ForgeEventFactory.canLivingConvert(entity, EntityType.SKELETON_HORSE, (timer) -> {})){
            player.swing(hand);
            ZombieHorse horse = (ZombieHorse)entity;
            horse.playSound(SoundEvents.HORSE_DEATH, 0.8F, horse.getVoicePitch());
            horse.playSound(SoundEvents.ZOMBIE_INFECT, 0.8F, horse.getVoicePitch());
            CompoundTag horseExtras = new CompoundTag();
            horse.addAdditionalSaveData(horseExtras);
            for(int i = 0; i < 6 + horse.getRandom().nextInt(5); i++){
                horse.level.addParticle(ParticleTypes.SNEEZE, horse.getRandomX(1.0F), horse.getRandomY(), horse.getRandomZ(1.0F), 0F, 0F, 0F);
            }
            SkeletonHorse skeleton = EntityType.SKELETON_HORSE.create(horse.level);
            if(horse.isLeashed()){
                skeleton.setLeashedTo(horse.getLeashHolder(), true);
            }
            skeleton.moveTo(horse.getX(), horse.getY(), horse.getZ(), horse.getYRot(), horse.getXRot());
            skeleton.setNoAi(horse.isNoAi());
            skeleton.setBaby(horse.isBaby());
            if (horse.hasCustomName()) {
                skeleton.setCustomName(horse.getCustomName());
                skeleton.setCustomNameVisible(horse.isCustomNameVisible());
            }
            skeleton.readAdditionalSaveData(horseExtras);
            skeleton.setPersistenceRequired();
            net.minecraftforge.event.ForgeEventFactory.onLivingConvert(horse, skeleton);
            player.level.addFreshEntity(skeleton);
            horse.discard();
            if(!player.isCreative()){
                stack.shrink(1);
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }
}
