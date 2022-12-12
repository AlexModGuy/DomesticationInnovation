package com.github.alexthe668.domesticationinnovation.server.item;

import com.github.alexthe668.domesticationinnovation.DomesticationMod;
import com.github.alexthe668.domesticationinnovation.server.entity.TameableUtils;
import com.github.alexthe668.domesticationinnovation.server.misc.DICreativeModeTab;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class DeedOfOwnershipItem extends Item {

    public DeedOfOwnershipItem() {
        super(new Item.Properties().stacksTo(1));
    }

    public static boolean isBound(ItemStack stack) {
        return stack.getTag() != null && stack.getTag().getBoolean("HasBoundEntity");
    }

    public boolean isFoil(ItemStack stack) {
        return super.isFoil(stack) || isBound(stack);
    }

    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag flags) {
        if(isBound(stack) && level != null && stack.getTag().getString("BoundEntityName") != null){
            list.add(Component.translatable("item.domesticationinnovation.deed_of_ownership.desc", stack.getTag().getString("BoundEntityName")).withStyle(ChatFormatting.GRAY));
        }
    }

}
