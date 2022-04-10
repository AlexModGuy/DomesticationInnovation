package com.github.alexthe668.domesticationinnovation.server.entity;

import com.github.alexthe666.citadel.Citadel;
import com.github.alexthe666.citadel.server.entity.CitadelEntityData;
import com.github.alexthe666.citadel.server.message.PropertiesMessage;
import com.github.alexthe668.domesticationinnovation.DomesticationMod;
import com.github.alexthe668.domesticationinnovation.server.enchantment.DIEnchantmentRegistry;
import com.github.alexthe668.domesticationinnovation.server.misc.DIParticleRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

public class TameableUtils {

    private static final String ENCHANTMENT_TAG = "StoredPetEnchantments";
    private static final String COLLAR_TAG = "HasPetCollar";
    private static final String IMMUNITY_TIME_TAG = "PetImmunityTimer";
    private static final String FROZEN_TIME_TAG = "PetFrozenTime";
    private static final String ATTACK_TARGET_ENTITY = "PetAttackTarget";
    private static final String SHADOW_PUNCH_TIMES = "PetShadowPunchTimes";
    private static final String SHADOW_PUNCH_TIMES_PREV = "PetPrevShadowPunchTimes";
    private static final String SHADOW_PUNCH_COOLDOWN = "PetShadowPunchCooldown";
    private static final String SHADOW_PUNCH_STRIKING = "PetShadowPunchStriking";
    private static final String JUKEBOX_FOLLOWER_UUID = "PetJukeboxFollowerUUID";
    private static final String JUKEBOX_FOLLOWER_DISC = "PetJukeboxFollowerDisc";
    private static final String HAS_PET_BED = "HasPetBed";
    private static final String PET_BED_X = "PetBedX";
    private static final String PET_BED_Y = "PetBedY";
    private static final String PET_BED_Z = "PetBedZ";
    private static final String PET_BED_DIMENSION = "PetBedDimension";
    private static final String FALL_DISTANCE_SYNC = "SyncedFallDistance";
    private static final String ZOMBIE_PET = "ZombiePet";
    private static final UUID HEALTH_BOOST_UUID = UUID.fromString("556E1665-8B10-40C8-8F9D-CF9B166EEEEE");
    private static final UUID SPEED_BOOST_UUID = UUID.fromString("ff465ded-9040-4eb5-93a1-7bbe97c31744");
    private static final ResourceLocation INFAMY_ENCHANT_ATTRACTS = new ResourceLocation(DomesticationMod.MODID + ":infamy_target_attracted");

    public static boolean hasSameOwnerAs(LivingEntity tameable, Entity target) {
        return hasSameOwnerAsOneWay(tameable, target) || hasSameOwnerAsOneWay(target, tameable);
    }

    private static boolean hasSameOwnerAsOneWay(Entity tameable, Entity target) {
        if (tameable instanceof TamableAnimal tamed && tamed.getOwner() != null) {
            if (target instanceof ModifedToBeTameable axolotl && axolotl.getTameOwner() != null) {
                if (tamed.getOwner().equals(axolotl.getTameOwner())) {
                    return true;
                }
            }
            if (target instanceof TamableAnimal otherPet && otherPet.getOwner() != null) {
                if (tamed.getOwner().equals(otherPet.getOwner())) {
                    return true;
                }
            }
            return tamed.getOwner().equals(target);
        } else if (tameable instanceof ModifedToBeTameable axolotl && axolotl.getTameOwner() != null) {
            if (tameable instanceof TamableAnimal tamed && tamed.getOwner() != null) {
                if (tamed.getOwner().equals(axolotl.getTameOwner())) {
                    return true;
                }
            }
            if (target instanceof ModifedToBeTameable otherPet && otherPet.getTameOwner() != null) {
                if (axolotl.getTameOwner().equals(otherPet.getTameOwner())) {
                    return true;
                }
            }
            return axolotl.getTameOwner().equals(target);
        }
        return false;
    }

    public static boolean isPetOf(Player player, Entity entity) {
        return entity != null && (entity.isAlliedTo(player) || hasSameOwnerAsOneWay(entity, player));
    }

    public static boolean isTamed(Entity entity) {
        //sometimes these are not bound on runtime
        if (entity instanceof Axolotl) {
            return ((ModifedToBeTameable) entity).isTame() && DomesticationMod.CONFIG.tameableAxolotl.get();
        }
        if (entity instanceof Fox) {
            return ((ModifedToBeTameable) entity).isTame() && DomesticationMod.CONFIG.tameableFox.get();
        }
        if (entity instanceof Rabbit) {
            return ((ModifedToBeTameable) entity).isTame() && DomesticationMod.CONFIG.tameableRabbit.get();
        }
        return entity instanceof ModifedToBeTameable && ((ModifedToBeTameable) entity).isTame() || entity instanceof TamableAnimal && ((TamableAnimal) entity).isTame();
    }

    public static boolean couldBeTamed(Entity entity) {
        return entity instanceof ModifedToBeTameable || entity instanceof TamableAnimal;
    }

    public static Entity getOwnerOf(Entity entity) {
        if (entity instanceof ModifedToBeTameable) {
            return ((ModifedToBeTameable) entity).getTameOwner();
        }
        if (entity instanceof TamableAnimal) {
            return ((TamableAnimal) entity).getOwner();
        }
        return null;
    }

    public static UUID getOwnerUUIDOf(Entity entity) {
        if (entity instanceof ModifedToBeTameable) {
            return ((ModifedToBeTameable) entity).getTameOwnerUUID();
        }
        if (entity instanceof TamableAnimal) {
            return ((TamableAnimal) entity).getOwnerUUID();
        }
        return null;
    }

    public static void setOwnerUUIDOf(Entity entity, UUID uuid) {
        if (entity instanceof ModifedToBeTameable) {
            ((ModifedToBeTameable) entity).setTameOwnerUUID(uuid);
        }
        if (entity instanceof TamableAnimal) {
            ((TamableAnimal) entity).setOwnerUUID(uuid);
        }
    }

    private static void setEnchantmentTag(LivingEntity enchanted, ListTag enchants) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.put(ENCHANTMENT_TAG, enchants);
        sync(enchanted, tag);
        onUpdateEnchants(enchanted);
    }

    private static void onUpdateEnchants(LivingEntity enchanted) {
        int healthExtra = getEnchantLevel(enchanted, DIEnchantmentRegistry.HEALTH_BOOST);
        int speedExtra = getEnchantLevel(enchanted, DIEnchantmentRegistry.SPEEDSTER);
        AttributeInstance health = enchanted.getAttribute(Attributes.MAX_HEALTH);
        AttributeInstance speed = enchanted.getAttribute(Attributes.MOVEMENT_SPEED);
        if (health != null) {
            if (healthExtra > 0) {
                AttributeModifier attributemodifier = new AttributeModifier(HEALTH_BOOST_UUID, "health boost pet upgrade", healthExtra * 10, AttributeModifier.Operation.ADDITION);
                if (health.hasModifier(attributemodifier)) {
                    health.removeModifier(attributemodifier);
                    health.addPermanentModifier(attributemodifier);
                } else {
                    health.addPermanentModifier(attributemodifier);
                }
            } else {
                health.removePermanentModifier(HEALTH_BOOST_UUID);
            }
        }
        if (speed != null) {
            if (speedExtra > 0) {
                AttributeModifier attributemodifier = new AttributeModifier(SPEED_BOOST_UUID, "speedster pet upgrade", speedExtra * 0.075F, AttributeModifier.Operation.ADDITION);
                if (speed.hasModifier(attributemodifier)) {
                    speed.removeModifier(attributemodifier);
                    speed.addPermanentModifier(attributemodifier);
                } else {
                    speed.addPermanentModifier(attributemodifier);
                }
            } else {
                speed.removePermanentModifier(SPEED_BOOST_UUID);
            }
        }
    }

    @Nullable
    private static ListTag getEnchantmentList(LivingEntity entity) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(entity);
        if (tag.contains(ENCHANTMENT_TAG)) {
            return tag.getList(ENCHANTMENT_TAG, 10);
        }
        return null;
    }

    public static int getEnchantLevel(LivingEntity entity, Enchantment enchantment) {
        ListTag listtag = getEnchantmentList(entity);
        if (listtag != null && DomesticationMod.CONFIG.isEnchantEnabled(enchantment)) {
            for (int i = 0; i < listtag.size(); ++i) {
                CompoundTag compoundtag = listtag.getCompound(i);
                ResourceLocation res = EnchantmentHelper.getEnchantmentId(compoundtag);
                if (res != null && res.equals(enchantment.getRegistryName())) {
                    return EnchantmentHelper.getEnchantmentLevel(compoundtag);
                }
            }
        }
        return 0;
    }

    public static boolean hasEnchant(LivingEntity entity, Enchantment enchantment) {
        return getEnchantLevel(entity, enchantment) > 0;
    }

    @Nullable
    public static Map<ResourceLocation, Integer> getEnchants(LivingEntity entity) {
        ListTag listtag = getEnchantmentList(entity);
        if (listtag == null) {
            return null;
        }
        Map<ResourceLocation, Integer> enchants = new HashMap<>();
        for (int i = 0; i < listtag.size(); ++i) {
            CompoundTag compoundtag = listtag.getCompound(i);
            ResourceLocation res = EnchantmentHelper.getEnchantmentId(compoundtag);
            if (DomesticationMod.CONFIG.isEnchantEnabled(res)) {
                enchants.put(res, EnchantmentHelper.getEnchantmentLevel(compoundtag));
            }
        }
        return enchants;
    }

    public static List<Component> getEnchantDescriptions(LivingEntity entity) {
        List<Component> list = new ArrayList<>();
        list.add(new TextComponent("   ").append(new TranslatableComponent("message.domesticationinnovation.enchantments").withStyle(ChatFormatting.GOLD)));
        Map<ResourceLocation, Integer> map = getEnchants(entity);
        if (map != null) {
            for (Map.Entry<ResourceLocation, Integer> entry : map.entrySet()) {
                boolean isCurse = entry.getKey().getPath().contains("curse");
                list.add(new TranslatableComponent("enchantment." + entry.getKey().getNamespace() + "." + entry.getKey().getPath()).append(new TextComponent(" ")).append(new TranslatableComponent("enchantment.level." + entry.getValue())).withStyle(isCurse ? ChatFormatting.RED : ChatFormatting.AQUA));
            }
        }
        return list;
    }

    public static boolean hasAnyEnchants(LivingEntity entity) {
        ListTag listtag = getEnchantmentList(entity);
        return listtag != null && !listtag.isEmpty();
    }

    public static void addEnchant(LivingEntity entity, EnchantmentInstance enchantment) {
        ListTag listtag = getEnchantmentList(entity);
        if (listtag != null && DomesticationMod.CONFIG.isEnchantEnabled(enchantment.enchantment)) {
            ResourceLocation resourcelocation = EnchantmentHelper.getEnchantmentId(enchantment.enchantment);
            boolean flag = true;
            for (int i = 0; i < listtag.size(); ++i) {
                CompoundTag compoundtag = listtag.getCompound(i);
                ResourceLocation resourcelocation1 = EnchantmentHelper.getEnchantmentId(compoundtag);
                if (resourcelocation1 != null && resourcelocation1.equals(resourcelocation)) {
                    if (EnchantmentHelper.getEnchantmentLevel(compoundtag) < enchantment.level) {
                        EnchantmentHelper.setEnchantmentLevel(compoundtag, enchantment.level);
                    }

                    flag = false;
                    break;
                }
            }
            if (flag) {
                listtag.add(EnchantmentHelper.storeEnchantment(resourcelocation, enchantment.level));
            }
            setEnchantmentTag(entity, listtag);
        }
    }

    public static void clearEnchants(LivingEntity entity) {
        setEnchantmentTag(entity, new ListTag());
    }

    public static void setHasCollar(LivingEntity enchanted, boolean collar) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putBoolean(COLLAR_TAG, collar);
        sync(enchanted, tag);
    }

    public static boolean hasCollar(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.contains(COLLAR_TAG) && tag.getBoolean(COLLAR_TAG);
    }

    public static int getImmuneTime(LivingEntity enchanted) {
        if (hasEnchant(enchanted, DIEnchantmentRegistry.IMMUNITY_FRAME)) {
            CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
            return tag.getInt(IMMUNITY_TIME_TAG);
        }
        return 0;
    }

    public static void setImmuneTime(LivingEntity enchanted, int time) {
        if (hasEnchant(enchanted, DIEnchantmentRegistry.IMMUNITY_FRAME)) {
            CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
            tag.putInt(IMMUNITY_TIME_TAG, time);
            sync(enchanted, tag);
        }
    }

    public static int getFrozenTime(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.getInt(FROZEN_TIME_TAG);
    }

    public static void setFrozenTimeTag(LivingEntity enchanted, int time) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putInt(FROZEN_TIME_TAG, time);
        sync(enchanted, tag);
    }

    public static int getPetAttackTargetID(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return !tag.contains(ATTACK_TARGET_ENTITY) ? -1 : tag.getInt(ATTACK_TARGET_ENTITY);
    }

    @Nullable
    public static Entity getPetAttackTarget(LivingEntity enchanted) {
        int i = getPetAttackTargetID(enchanted);
        return i == -1 ? null : enchanted.level.getEntity(i);
    }

    public static void setPetAttackTarget(LivingEntity enchanted, int id) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putInt(ATTACK_TARGET_ENTITY, id);
        sync(enchanted, tag);
    }

    public static int getShadowPunchCooldown(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.getInt(SHADOW_PUNCH_COOLDOWN);
    }

    public static void setShadowPunchCooldown(LivingEntity enchanted, int time) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putInt(SHADOW_PUNCH_COOLDOWN, time);
        sync(enchanted, tag);
    }

    public static int[] getShadowPunchTimes(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.getIntArray(SHADOW_PUNCH_TIMES);
    }

    public static void setShadowPunchTimes(LivingEntity enchanted, int[] times) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putIntArray(SHADOW_PUNCH_TIMES, times);
        sync(enchanted, tag);
    }

    public static void setShadowPunchStriking(LivingEntity enchanted, int[] times) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putIntArray(SHADOW_PUNCH_STRIKING, times);
        sync(enchanted, tag);
    }

    public static int[] getShadowPunchStriking(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.getIntArray(SHADOW_PUNCH_STRIKING);
    }

    public static void setPetJukeboxUUID(LivingEntity enchanted, UUID id) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putUUID(JUKEBOX_FOLLOWER_UUID, id);
        sync(enchanted, tag);
    }

    public static UUID getPetJukeboxUUID(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.contains(JUKEBOX_FOLLOWER_UUID) ? tag.getUUID(JUKEBOX_FOLLOWER_UUID) : null;
    }

    public static void setPetJukeboxDisc(LivingEntity enchanted, ItemStack stack) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.put(JUKEBOX_FOLLOWER_DISC, stack.save(new CompoundTag()));
        sync(enchanted, tag);
    }

    public static ItemStack getPetJukeboxDisc(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.contains(JUKEBOX_FOLLOWER_DISC) ? ItemStack.of(tag.getCompound(JUKEBOX_FOLLOWER_DISC)) : ItemStack.EMPTY;
    }

    private static void sync(LivingEntity enchanted, CompoundTag tag) {
        CitadelEntityData.setCitadelTag(enchanted, tag);
        if (!enchanted.level.isClientSide) {
            Citadel.sendMSGToAll(new PropertiesMessage("CitadelTagUpdate", tag, enchanted.getId()));
        } else {
            Citadel.sendMSGToServer(new PropertiesMessage("CitadelTagUpdate", tag, enchanted.getId()));
        }
    }

    @Nullable
    public static BlockPos getPetBedPos(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        if (tag.getBoolean(HAS_PET_BED) && tag.contains(PET_BED_X) && tag.contains(PET_BED_Y) && tag.contains(PET_BED_Z)) {
            return new BlockPos(tag.getInt(PET_BED_X), tag.getInt(PET_BED_Y), tag.getInt(PET_BED_Z));
        }
        return null;
    }

    public static void setPetBedPos(LivingEntity enchanted, BlockPos petBed) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putBoolean(HAS_PET_BED, true);
        tag.putInt(PET_BED_X, petBed.getX());
        tag.putInt(PET_BED_Y, petBed.getY());
        tag.putInt(PET_BED_Z, petBed.getZ());
        sync(enchanted, tag);
    }

    public static void removePetBedPos(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putBoolean(HAS_PET_BED, false);
        sync(enchanted, tag);
    }

    public static String getPetBedDimension(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return !tag.contains(PET_BED_DIMENSION) ? "minecraft:overworld" : tag.getString(PET_BED_DIMENSION);
    }

    public static void setPetBedDimension(LivingEntity enchanted, String dimension) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putString(PET_BED_DIMENSION, dimension);
        sync(enchanted, tag);
    }

    public static void attractAnimals(LivingEntity attractor, int max) {
        if ((attractor.tickCount + attractor.getId()) % 8 == 0) {
            Predicate<Entity> notOnTeam = (animal) -> !hasSameOwnerAs((LivingEntity) animal, attractor) && animal.distanceTo(attractor) > 3 + attractor.getBbWidth() * 1.6F;
            List<Animal> list = attractor.level.getEntitiesOfClass(Animal.class, attractor.getBoundingBox().inflate(16, 8, 16), EntitySelector.NO_SPECTATORS.and(notOnTeam));
            list.sort(Comparator.comparingDouble(attractor::distanceToSqr));
            for (int i = 0; i < Math.min(max, list.size()); i++) {
                Animal e = list.get(i);
                e.setTarget(null);
                e.setLastHurtByMob(null);
                e.getNavigation().moveTo(attractor, 1.1D);
            }

        }
    }

    public static void aggroRandomMonsters(LivingEntity attractor) {
        if ((attractor.tickCount + attractor.getId()) % 400 == 0) {
            //Tag<EntityType<?>> tag = EntityTypeTags.getAllTags().getTag(INFAMY_ENCHANT_ATTRACTS);
            Predicate<Entity> notOnTeamAndMonster = (animal) -> animal instanceof Monster && !hasSameOwnerAs((LivingEntity) animal, attractor) && animal.distanceTo(attractor) > 3 + attractor.getBbWidth() * 1.6F;
            List<Mob> list = attractor.level.getEntitiesOfClass(Mob.class, attractor.getBoundingBox().inflate(20, 8, 20), EntitySelector.NO_SPECTATORS.and(notOnTeamAndMonster));
            list.sort(Comparator.comparingDouble(attractor::distanceToSqr));
            if (!list.isEmpty()) {
                list.get(0).setTarget(attractor);
            }
        }
    }

    public static void detectRandomOres(LivingEntity attractor, int interval, int range, int effectLength, int maxOres) {
        int tick = (attractor.tickCount + attractor.getId()) % interval;
        if(tick <= 30){
            attractor.xRotO = attractor.getXRot();
            attractor.setXRot((float)Math.sin(tick * 0.6F) * 30F);
            Vec3 look = attractor.getEyePosition().add(attractor.getViewVector(1.0F).scale(attractor.getBbWidth()));
            Random rand = attractor.getRandom();
            for(int i = 0; i < 3; i++){
                double x = attractor.getRandomX(2.0F);
                double y = attractor.position().y;
                double z = attractor.getRandomZ(2.0F);
                attractor.getLevel().addParticle(DIParticleRegistry.SNIFF, x, y, z, look.x, look.y, look.z);
            }
        }
        if (tick == 30) {
            List<BlockPos> ores = new ArrayList<>();
            BlockPos blockpos = attractor.blockPosition();
            int half = range / 2;
            for(int i = 0; i <= half && i >= -half; i = (i <= 0 ? 1 : 0) - i) {
                for(int j = 0; j <= range && j >= -range; j = (j <= 0 ? 1 : 0) - j) {
                    for(int k = 0; k <= range && k >= -range; k = (k <= 0 ? 1 : 0) - k) {
                        BlockPos offset = blockpos.offset(j, i, k);
                        BlockState state = attractor.getLevel().getBlockState(offset);
                        if (state.is(Tags.Blocks.ORES)) {
                            if (ores.size() < maxOres) {
                                ores.add(offset);
                            }else{
                                break;
                            }
                        }
                    }
                }
            }
            for(BlockPos ore : ores){
                HighlightedBlockEntity highlight = DIEntityRegistry.HIGHLIGHTED_BLOCK.get().create(attractor.level);
                highlight.setPos(Vec3.atBottomCenterOf(ore));
                highlight.setLifespan(effectLength);
                highlight.setXRot(0);
                highlight.setYRot(0);
                attractor.getLevel().addFreshEntity(highlight);
            }
        }
    }

    public static float getFallDistance(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.getFloat(FALL_DISTANCE_SYNC);
    }

    public static void setFallDistance(LivingEntity enchanted, float dist) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putFloat(FALL_DISTANCE_SYNC, dist);
        sync(enchanted, tag);

    }


    public static void setZombiePet(LivingEntity enchanted, boolean zombiefied) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putBoolean(ZOMBIE_PET, zombiefied);
        sync(enchanted, tag);

    }

    public static boolean isZombiePet(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.getBoolean(ZOMBIE_PET);
    }

    public static int getCharismaBonusForOwner(Player player) {
        Predicate<Entity> pet = (animal) -> isTamed(animal) && isPetOf(player, animal);
        List<LivingEntity> list = player.level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(25, 8, 25), EntitySelector.NO_SPECTATORS.and(pet));
        int charismas = 0;
        for (LivingEntity entity : list) {
            charismas += 10 * getEnchantLevel(entity, DIEnchantmentRegistry.CHARISMA);
        }
        return Math.min(charismas, 50);
    }
}
