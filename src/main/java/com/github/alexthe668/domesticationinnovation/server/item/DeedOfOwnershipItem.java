package com.github.alexthe668.domesticationinnovation.server.item;

import com.github.alexthe668.domesticationinnovation.DomesticationMod;
import com.github.alexthe668.domesticationinnovation.server.entity.TameableUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class DeedOfOwnershipItem extends Item {

    public DeedOfOwnershipItem() {
        super(new Item.Properties().tab(DomesticationMod.TAB).stacksTo(1));
    }

    public static boolean isBound(ItemStack stack) {
        return stack.getTag() != null && stack.getTag().getBoolean("HasBoundEntity");
    }

    public boolean isFoil(ItemStack stack) {
        return super.isFoil(stack) || isBound(stack);
    }

    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag flags) {
        if(isBound(stack) && level != null && stack.getTag().getString("BoundEntityName") != null){
            list.add(new TranslatableComponent("item.domesticationinnovation.deed_of_ownership.desc", stack.getTag().getString("BoundEntityName")).withStyle(ChatFormatting.GRAY));
        }
    }

}
