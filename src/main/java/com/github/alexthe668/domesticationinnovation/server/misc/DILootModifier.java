package com.github.alexthe668.domesticationinnovation.server.misc;

import com.github.alexthe668.domesticationinnovation.DomesticationMod;
import com.github.alexthe668.domesticationinnovation.server.enchantment.DIEnchantmentRegistry;
import com.github.alexthe668.domesticationinnovation.server.enchantment.PetEnchantment;
import com.github.alexthe668.domesticationinnovation.server.item.DIItemRegistry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;

import javax.annotation.Nonnull;
import java.util.Random;
import java.util.function.Supplier;

public class DILootModifier extends LootModifier {
    public static final Supplier<Codec<DILootModifier>> CODEC = () ->
            RecordCodecBuilder.create(inst ->
                    inst.group(
                                    LOOT_CONDITIONS_CODEC.fieldOf("conditions").forGetter(lm -> lm.conditions),
                                    Codec.INT.fieldOf("loot_type").orElse(0).forGetter((configuration) -> configuration.lootType)
                            )
                            .apply(inst, DILootModifier::new));

    private final int lootType;
    protected DILootModifier(LootItemCondition[] conditionsIn, int lootType) {
        super(conditionsIn);
        this.lootType = lootType;
    }

    @Nonnull
    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        switch (lootType){
            case 0:
                if (context.getRandom().nextFloat() < DomesticationMod.CONFIG.sinisterCarrotLootChance.get()) {
                    generatedLoot.add(new ItemStack(DIItemRegistry.SINISTER_CARROT.get(), context.getRandom().nextInt(1, 2)));
                }
                break;
            case 1:
                if (context.getRandom().nextFloat() < DomesticationMod.CONFIG.bubblingLootChance.get()) {
                    generatedLoot.add(enchantedBook(DIEnchantmentRegistry.BUBBLING, context.getRandom()));
                }
                break;
            case 2:
                if (context.getRandom().nextFloat() < DomesticationMod.CONFIG.vampirismLootChance.get()) {
                    generatedLoot.add(enchantedBook(DIEnchantmentRegistry.VAMPIRE, context.getRandom()));
                }
                break;
            case 3:
                if (context.getRandom().nextFloat() < DomesticationMod.CONFIG.voidCloudLootChance.get()) {
                    generatedLoot.add(enchantedBook(DIEnchantmentRegistry.VOID_CLOUD, context.getRandom()));
                }
                break;
            case 4:
                if (context.getRandom().nextFloat() < DomesticationMod.CONFIG.oreScentingLootChance.get()) {
                    generatedLoot.add(enchantedBook(DIEnchantmentRegistry.ORE_SCENTING, context.getRandom()));
                }
                break;
            case 5:
                if (context.getRandom().nextFloat() < DomesticationMod.CONFIG.muffledLootChance.get()) {
                    generatedLoot.add(enchantedBook(DIEnchantmentRegistry.MUFFLED, context.getRandom()));
                }
                break;
            case 6:
                if (context.getRandom().nextFloat() < DomesticationMod.CONFIG.blazingProtectionLootChance.get()) {
                    generatedLoot.add(enchantedBook(DIEnchantmentRegistry.BLAZING_PROTECTION, context.getRandom()));
                }
                break;
        }
        return generatedLoot;
    }

    private ItemStack enchantedBook(Enchantment enchantment, RandomSource randomSource) {
        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
        int maxLevels = enchantment.getMaxLevel();
        EnchantedBookItem.addEnchantment(book, new EnchantmentInstance(enchantment, maxLevels > 1 ? 1 + randomSource.nextInt(maxLevels - 1) : 1));
        return book;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}