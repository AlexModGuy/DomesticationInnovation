package com.github.alexthe668.domesticationinnovation.server;

import com.github.alexthe666.citadel.server.entity.IComandableMob;
import com.github.alexthe668.domesticationinnovation.DomesticationMod;
import com.github.alexthe668.domesticationinnovation.server.block.DIBlockRegistry;
import com.github.alexthe668.domesticationinnovation.server.block.PetBedBlock;
import com.github.alexthe668.domesticationinnovation.server.block.PetBedBlockEntity;
import com.github.alexthe668.domesticationinnovation.server.enchantment.DIEnchantmentRegistry;
import com.github.alexthe668.domesticationinnovation.server.entity.*;
import com.github.alexthe668.domesticationinnovation.server.item.DIItemRegistry;
import com.github.alexthe668.domesticationinnovation.server.item.DeedOfOwnershipItem;
import com.github.alexthe668.domesticationinnovation.server.misc.*;
import com.github.alexthe668.domesticationinnovation.server.misc.trades.*;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.*;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.antlr.v4.runtime.misc.Triple;

import java.util.*;
import java.util.function.Predicate;

@Mod.EventBusSubscriber(modid = DomesticationMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CommonProxy {

    private static final UUID FROST_FANG_SLOW = UUID.fromString("1eaf83ff-7207-4596-b37a-d7a07b3ec4cf");
    private static final TargetingConditions ZOMBIE_TARGET = TargetingConditions.forCombat().range(32.0D);
    //list of pets to be teleported across dimensions, cleared after every tick
    public static List<Triple<Entity, ServerLevel, UUID>> teleportingPets = new ArrayList<>();

    private static final Map<Level, CollarTickTracker> COLLAR_TICK_TRACKER_MAP = new HashMap<>();

    public void init() {
    }

    public void serverInit() {
        ForgeChunkManager.setForcedChunkLoadingCallback(DomesticationMod.MODID, this::removeAllChunkTickets);
        DIVillagePieceRegistry.registerHouses();
    }

    private void removeAllChunkTickets(ServerLevel serverLevel, ForgeChunkManager.TicketHelper ticketHelper) {
        int i = 0;
        for (Map.Entry<UUID, Pair<LongSet, LongSet>> entry : ticketHelper.getEntityTickets().entrySet()) {
            ticketHelper.removeAllTickets(entry.getKey());
            i++;
        }
        DomesticationMod.LOGGER.debug("Removed " + i + " chunkloading tickets");
    }

    public void clientInit() {
    }

    public void updateVisualDataForMob(Entity entity, int[] arr) {

    }

    public void updateEntityStatus(Entity entity, byte updateKind) {

    }

    @SubscribeEvent
    public void onEntityJoinWorldEvent(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof LivingEntity living && TameableUtils.couldBeTamed(living)) {
            if (TameableUtils.hasEnchant(living, DIEnchantmentRegistry.HEALTH_BOOST)) {
                living.setHealth((float) Math.max(living.getHealth(), TameableUtils.getSafePetHealth(living)));
            }
            if (living.isAlive() && TameableUtils.isTamed(living)) {
                DIWorldData data = DIWorldData.get(living.level);
                if (data != null) {
                    data.removeMatchingLanternRequests(living.getUUID());
                }
            }
        }
    }

    @SubscribeEvent
    public void onEntityLeaveWorld(EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof LivingEntity living) {
            if (!living.level.isClientSide && living.isAlive() && TameableUtils.isTamed(living) && TameableUtils.shouldUnloadToLantern(living)) {
                UUID ownerUUID = TameableUtils.getOwnerUUIDOf(event.getEntity());
                String saveName = event.getEntity().hasCustomName() ? event.getEntity().getCustomName().getString() : "";
                DIWorldData data = DIWorldData.get(living.level);
                if (data != null) {
                    LanternRequest request = new LanternRequest(living.getUUID(), ForgeRegistries.ENTITY_TYPES.getKey(event.getEntity().getType()).toString(), ownerUUID, living.blockPosition(), event.getEntity().level.dayTime(), saveName);
                    data.addLanternRequest(request);
                }
            }
            if (TameableUtils.couldBeTamed(living) && TameableUtils.hasEnchant(living, DIEnchantmentRegistry.HEALTH_BOOST)) {
                TameableUtils.setSafePetHealth(living, living.getHealth());
            }

        }
    }

    private boolean canTickCollar(Entity entity){
        if(entity.level.isClientSide){
            return true;
        }else{
            CollarTickTracker tracker = COLLAR_TICK_TRACKER_MAP.get(entity.level);
            return tracker == null || !tracker.isEntityBlocked(entity);
        }
    }

    private void blockCollarTick(Entity entity){
        if(!entity.level.isClientSide){
            CollarTickTracker tracker = COLLAR_TICK_TRACKER_MAP.get(entity.level);
            if(tracker == null){
                tracker.addBlockedEntityTick(entity.getUUID(), 5);
            }
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.LevelTickEvent tick) {
        if (!tick.level.isClientSide) {
            COLLAR_TICK_TRACKER_MAP.computeIfAbsent(tick.level, k -> new CollarTickTracker());
            CollarTickTracker tracker = COLLAR_TICK_TRACKER_MAP.get(tick.level);
            tracker.tick();
        }
        if (!tick.level.isClientSide && tick.level instanceof ServerLevel) {
            for (final var triple : teleportingPets) {
                Entity entity = triple.a;
                ServerLevel endpointWorld = triple.b;
                UUID ownerUUID = triple.c;
                entity.unRide();
                entity.level = endpointWorld;
                Entity player = endpointWorld.getPlayerByUUID(ownerUUID);
                if (player != null) {
                    Entity teleportedEntity = entity.getType().create(endpointWorld);
                    if (teleportedEntity != null) {
                        teleportedEntity.restoreFrom(entity);
                        Vec3 toPos = player.position();
                        EntityDimensions dimensions = entity.getDimensions(entity.getPose());
                        AABB suffocationBox = new AABB(-dimensions.width / 2.0F, 0, -dimensions.width / 2.0F, dimensions.width / 2.0F, dimensions.height, dimensions.width / 2.0F);
                        while (!endpointWorld.noCollision(entity, suffocationBox.move(toPos.x, toPos.y, toPos.z)) && toPos.y < 300) {
                            toPos = toPos.add(0, 1, 0);
                        }
                        teleportedEntity.moveTo(toPos.x, toPos.y, toPos.z, entity.getYRot(), entity.getXRot());
                        teleportedEntity.setYHeadRot(entity.getYHeadRot());
                        teleportedEntity.fallDistance = 0.0F;
                        teleportedEntity.setPortalCooldown();
                        endpointWorld.addFreshEntity(teleportedEntity);
                    }
                    entity.remove(Entity.RemovalReason.DISCARDED);
                }
            }
            teleportingPets.clear();
        }
    }

    @SubscribeEvent
    public void onProjectileImpactEvent(ProjectileImpactEvent event) {
        if (event.getRayTraceResult() instanceof EntityHitResult) {
            Entity hit = ((EntityHitResult) event.getRayTraceResult()).getEntity();
            if (event.getProjectile().getOwner() instanceof Player) {
                Player player = (Player) event.getProjectile().getOwner();
                if (TameableUtils.isPetOf(player, hit)) {
                    event.setCanceled(true);
                }
            }
            if (TameableUtils.isTamed(hit)) {
                if (event.getEntity() instanceof AbstractArrow arrow) {
                    //fixes soft crash with vanilla
                    if (arrow.getPierceLevel() > 0) {
                        arrow.setPierceLevel((byte) 0);
                        arrow.remove(Entity.RemovalReason.DISCARDED);
                        event.setCanceled(true);
                        return;
                    }
                }
                if (TameableUtils.hasEnchant((LivingEntity) hit, DIEnchantmentRegistry.DEFLECTION)) {
                    event.setCanceled(true);
                    float xRot = event.getProjectile().getXRot();
                    float yRot = event.getProjectile().yRotO;
                    Vec3 vec3 = event.getProjectile().position().subtract(hit.position()).normalize().scale(hit.getBbWidth() + 0.5F);
                    Vec3 vec32 = hit.position().add(vec3);
                    hit.level.addParticle(DIParticleRegistry.DEFLECTION_SHIELD.get(), vec32.x, vec32.y, vec32.z, xRot, yRot, 0.0F);
                    event.getProjectile().setDeltaMovement(event.getProjectile().getDeltaMovement().scale(-0.2D));
                    event.getProjectile().setYRot(yRot + 180);
                    event.getProjectile().setXRot(xRot + 180);
                }
            }
        }
    }

    @SubscribeEvent
    public void onItemDespawnEvent(ItemExpireEvent event) {
        if (event.getEntity().getItem().getItem() == Items.APPLE && DomesticationMod.CONFIG.rottenApple.get()) {
            if (new Random().nextFloat() < 0.1F * event.getEntity().getItem().getCount()) {
                event.getEntity().getItem().shrink(1);
                event.setExtraLife(10);
                ItemEntity rotten = new ItemEntity(event.getEntity().level, event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), new ItemStack(DIItemRegistry.ROTTEN_APPLE.get()));
                event.getEntity().getLevel().addFreshEntity(rotten);
            }
        }
    }

    @SubscribeEvent
    public void onLivingUpdate(LivingEvent.LivingTickEvent event) {
        int frozenTime = TameableUtils.getFrozenTime(event.getEntity());
        if (TameableUtils.couldBeTamed(event.getEntity()) && canTickCollar(event.getEntity())) {
            if (TameableUtils.hasEnchant(event.getEntity(), DIEnchantmentRegistry.IMMUNITY_FRAME)) {
                int i = TameableUtils.getImmuneTime(event.getEntity());
                if (i > 0) {
                    TameableUtils.setImmuneTime(event.getEntity(), i - 1);
                }
            }
            if (event.getEntity().hasEffect(MobEffects.POISON) && TameableUtils.hasEnchant(event.getEntity(), DIEnchantmentRegistry.POISON_RESISTANCE)) {
                event.getEntity().removeEffect(MobEffects.POISON);
            }
            if (TameableUtils.hasEnchant(event.getEntity(), DIEnchantmentRegistry.AMPHIBIOUS)) {
                event.getEntity().setAirSupply(event.getEntity().getMaxAirSupply());
            }
            if (TameableUtils.hasEnchant(event.getEntity(), DIEnchantmentRegistry.MAGNETIC) && event.getEntity() instanceof Mob mob) {
                Entity sucking = TameableUtils.getPetAttackTarget(mob);
                if (!mob.level.isClientSide) {
                    if (mob.getTarget() == null || !mob.getTarget().isAlive() || mob.distanceTo(mob.getTarget()) < 0.5F + mob.getBbWidth() || mob.getRootVehicle() instanceof GiantBubbleEntity) {
                        if (TameableUtils.getPetAttackTargetID(mob) != -1) {
                            TameableUtils.setPetAttackTarget(mob, -1);
                        }
                    } else {
                        TameableUtils.setPetAttackTarget(mob, mob.getTarget().getId());
                    }
                } else {
                    if (sucking != null) {
                        double dist = mob.distanceTo(sucking);
                        Vec3 start = mob.position().add(0, mob.getBbHeight() * 0.5F, 0);
                        Vec3 end = sucking.position().add(0, sucking.getBbHeight() * 0.5F, 0).subtract(start);
                        for (float distStep = mob.getBbWidth() + 0.8F; distStep < (int) Math.ceil(dist); distStep++) {
                            Vec3 vec3 = start.add(end.scale(distStep / dist));
                            float f1 = 0.5F * (mob.getRandom().nextFloat() - 0.5F);
                            float f2 = 0.5F * (mob.getRandom().nextFloat() - 0.5F);
                            float f3 = 0.5F * (mob.getRandom().nextFloat() - 0.5F);
                            mob.level.addParticle(DIParticleRegistry.MAGNET.get(), vec3.x + f1, vec3.y + f2, vec3.z + f3, 0.0F, 0.0F, 0.0F);
                        }
                    }
                }
                if (sucking != null) {
                    if (mob.tickCount % 15 == 0) {
                        mob.playSound(DISoundRegistry.MAGNET_LOOP.get(), 1F, 1F);
                    }
                    mob.setDeltaMovement(mob.getDeltaMovement().multiply(0.88D, 1.0D, 0.88D));
                    Vec3 move = new Vec3(mob.getX() - sucking.getX(), mob.getY() - (double) sucking.getEyeHeight() / 2.0D - sucking.getY(), mob.getZ() - sucking.getZ());
                    sucking.setDeltaMovement(sucking.getDeltaMovement().add(move.normalize().scale(mob.isOnGround() ? 0.15D : 0.05D)));
                }
            }
            int shadowHandsLevel = TameableUtils.getEnchantLevel(event.getEntity(), DIEnchantmentRegistry.SHADOW_HANDS);
            if (shadowHandsLevel > 0 && event.getEntity() instanceof Mob mob) {
                DomesticationMod.PROXY.updateVisualDataForMob(event.getEntity(), TameableUtils.getShadowPunchTimes(mob));
                if (!mob.level.isClientSide) {
                    Entity punching = TameableUtils.getPetAttackTarget(mob);
                    int[] punchProgress = TameableUtils.getShadowPunchTimes(mob);
                    if (punching != null && punching.isAlive() && mob.hasLineOfSight(punching) && mob.distanceTo(punching) < 16) {
                        int[] striking = TameableUtils.getShadowPunchStriking(mob);
                        if (punchProgress == null || punchProgress.length < shadowHandsLevel) {
                            int[] clean = new int[shadowHandsLevel];
                            TameableUtils.setShadowPunchTimes(mob, clean);
                            TameableUtils.setShadowPunchStriking(mob, clean);
                        } else {
                            int cooldown = TameableUtils.getShadowPunchCooldown(mob);
                            if (cooldown <= 0) {
                                boolean flag = false;
                                int start = shadowHandsLevel == 1 ? 0 : mob.getRandom().nextInt(shadowHandsLevel - 1);
                                for (int i = start; i < shadowHandsLevel; i++) {
                                    if (striking[i] == 0) {
                                        striking[i] = 1;
                                        flag = true;
                                        break;
                                    }
                                }
                                if (flag) {
                                    TameableUtils.setShadowPunchCooldown(mob, 5);
                                }
                            } else {
                                TameableUtils.setShadowPunchCooldown(mob, cooldown - 1);
                            }
                            for (int i = 0; i < Math.min(shadowHandsLevel, Math.min(striking.length, punchProgress.length)); i++) {
                                if (striking[i] != 0) {
                                    if (punchProgress[i] < 10) {
                                        punchProgress[i] = punchProgress[i] + 1;
                                    } else {
                                        punching.hurt(DamageSource.mobAttack(mob), Mth.clamp(shadowHandsLevel, 2, 4));
                                        striking[i] = 0;
                                    }
                                }
                                if (striking[i] == 0 && punchProgress[i] > 0) {
                                    punchProgress[i] = punchProgress[i] - 1;
                                }
                            }
                            TameableUtils.setShadowPunchStriking(mob, striking);
                            TameableUtils.setShadowPunchTimes(mob, punchProgress);
                        }
                    } else {
                        if (punching != null) {
                            boolean flag = true;
                            for (int i = 0; i < Math.min(shadowHandsLevel, punchProgress.length); i++) {
                                if (punchProgress[i] > 0) {
                                    punchProgress[i] = punchProgress[i] - 1;
                                    flag = false;
                                }
                            }
                            TameableUtils.setShadowPunchStriking(mob, new int[shadowHandsLevel]);
                            TameableUtils.setShadowPunchTimes(mob, punchProgress);
                            if (flag) {
                                TameableUtils.setPetAttackTarget(mob, -1);
                            }
                        }
                        Entity punchingTarget = null;
                        if (mob.getTarget() != null) {
                            punchingTarget = mob.getTarget();
                        } else if (TameableUtils.getOwnerOf(mob) instanceof LivingEntity owner) {
                            if (owner.getLastHurtByMob() != null && owner.getLastHurtByMob().isAlive() && !TameableUtils.hasSameOwnerAs(mob, owner.getLastHurtByMob())) {
                                punchingTarget = owner.getLastHurtByMob();
                            }
                            if (owner.getLastHurtMob() != null && owner.getLastHurtMob().isAlive() && !TameableUtils.hasSameOwnerAs(mob, owner.getLastHurtMob())) {
                                punchingTarget = owner.getLastHurtMob();
                            }
                        }
                        if (punchingTarget != null && punchingTarget.isAlive()) {
                            TameableUtils.setPetAttackTarget(mob, punchingTarget.getId());
                        }
                    }
                }
            }
            if (TameableUtils.hasEnchant(event.getEntity(), DIEnchantmentRegistry.DISK_JOCKEY) && !event.getEntity().level.isClientSide) {
                UUID uuid = TameableUtils.getPetJukeboxUUID(event.getEntity());
                if (uuid == null || !(((ServerLevel) event.getEntity().level).getEntity(uuid) instanceof FollowingJukeboxEntity)) {
                    FollowingJukeboxEntity follower = DIEntityRegistry.FOLLOWING_JUKEBOX.get().create(event.getEntity().level);
                    follower.setFollowingUUID(event.getEntity().getUUID());
                    follower.copyPosition(event.getEntity());
                    event.getEntity().level.addFreshEntity(follower);
                    TameableUtils.setPetJukeboxUUID(event.getEntity(), follower.getUUID());
                }
            }
            if (TameableUtils.hasEnchant(event.getEntity(), DIEnchantmentRegistry.LINKED_INVENTORY) && event.getEntity() instanceof Mob mob) {
                if (!mob.canPickUpLoot()) {
                    mob.setCanPickUpLoot(true);
                }
            }
            int shepherdLvl = TameableUtils.getEnchantLevel(event.getEntity(), DIEnchantmentRegistry.SHEPHERD);
            if (shepherdLvl > 0) {
                TameableUtils.attractAnimals(event.getEntity(), shepherdLvl * 3);
            }
            if (TameableUtils.hasEnchant(event.getEntity(), DIEnchantmentRegistry.INFAMY_CURSE)) {
                TameableUtils.aggroRandomMonsters(event.getEntity());
            }
            if (TameableUtils.hasEnchant(event.getEntity(), DIEnchantmentRegistry.INTIMIDATION)) {
                TameableUtils.scareRandomMonsters(event.getEntity(), TameableUtils.getEnchantLevel(event.getEntity(), DIEnchantmentRegistry.INTIMIDATION));
            }
            if (TameableUtils.hasEnchant(event.getEntity(), DIEnchantmentRegistry.BLIGHT_CURSE)) {
                TameableUtils.destroyRandomPlants(event.getEntity());
            }
            if (TameableUtils.hasEnchant(event.getEntity(), DIEnchantmentRegistry.REJUVENATION)) {
                TameableUtils.absorbExpOrbs(event.getEntity());
            }
            if (TameableUtils.hasEnchant(event.getEntity(), DIEnchantmentRegistry.VOID_CLOUD) && !event.getEntity().isInWaterOrBubble() && event.getEntity().fallDistance > 3.0F && !event.getEntity().isOnGround()) {
                Entity owner = TameableUtils.getOwnerOf(event.getEntity());
                boolean shouldMoveToOwnerXZ = owner != null && Math.abs(owner.getY() - event.getEntity().getY()) < 1;
                double targetX = shouldMoveToOwnerXZ ? owner.getX() : event.getEntity().getX();
                double targetY = Math.max(event.getEntity().level.getMinBuildHeight() + 0.5F, owner == null ? 64F : owner.getY() < event.getEntity().getY() ? owner.getY() + 0.6F : owner.getY(1.0F) + event.getEntity().getBbHeight());
                if (owner != null && owner.getRootVehicle() == event.getEntity()) {
                    targetY = Math.min(event.getEntity().level.getMinBuildHeight() + 0.5F, event.getEntity().getY() - 0.5F);
                }
                double targetZ = shouldMoveToOwnerXZ ? owner.getZ() : event.getEntity().getZ();
                if (event.getEntity().verticalCollision) {
                    event.getEntity().setOnGround(true);
                    targetX += (event.getEntity().getRandom().nextFloat() - 0.5F) * 4;
                    targetZ += (event.getEntity().getRandom().nextFloat() - 0.5F) * 4;
                }
                Vec3 move = new Vec3(targetX - event.getEntity().getX(), targetY - event.getEntity().getY(), targetZ - event.getEntity().getZ());
                event.getEntity().setDeltaMovement(event.getEntity().getDeltaMovement().add(move.normalize().scale(0.15D)).multiply(0.5F, 0.5F, 0.5F));
                if (event.getEntity().level instanceof ServerLevel) {
                    TameableUtils.setFallDistance(event.getEntity(), event.getEntity().fallDistance);
                    ((ServerLevel) event.getEntity().level).sendParticles(ParticleTypes.REVERSE_PORTAL, event.getEntity().getRandomX(1.5F), event.getEntity().getY() - event.getEntity().getRandom().nextFloat(), event.getEntity().getRandomZ(1.5F), 0, 0, -0.2F, 0, 1.0D);
                }
            }
            int oreLvl = TameableUtils.getEnchantLevel(event.getEntity(), DIEnchantmentRegistry.ORE_SCENTING);
            if (oreLvl > 0) {
                int interval = 100 + Math.max(150, 550 - oreLvl * 100);
                TameableUtils.detectRandomOres(event.getEntity(), interval, 5 + oreLvl * 2, oreLvl * 50, oreLvl * 3);
            }
            if (TameableUtils.isZombiePet(event.getEntity()) && !event.getEntity().level.isClientSide && event.getEntity() instanceof Mob mob) {
                if (mob.getTarget() instanceof Player && ((Player) mob.getTarget()).isCreative()) {
                    mob.setTarget(null);
                }
                if (mob.getTarget() == null || !mob.getTarget().isAlive()) {
                    mob.setTarget(mob.level.getNearestPlayer(ZOMBIE_TARGET, mob));
                } else if (mob.distanceTo(mob.getTarget()) < mob.getBbWidth() + 0.5F) {
                    mob.doHurtTarget(mob.getTarget());
                } else if (mob.getNavigation().isDone()) {
                    mob.getNavigation().moveTo(mob.getTarget(), 1.0D);
                }
            }
            int psychicWallLevel = TameableUtils.getEnchantLevel(event.getEntity(), DIEnchantmentRegistry.PSYCHIC_WALL);
            if (psychicWallLevel > 0 && event.getEntity() instanceof Mob mob) {
                int cooldown = TameableUtils.getPsychicWallCooldown(mob);
                if (cooldown > 0) {
                    TameableUtils.setPsychicWallCooldown(mob, cooldown - 1);
                } else {
                    Entity blocking = null;
                    Entity blockingFrom = null;
                    if (mob.getTarget() != null) {
                        blocking = mob.getTarget();
                        blockingFrom = mob;
                    } else if (TameableUtils.getOwnerOf(mob) instanceof LivingEntity owner) {
                        if (owner.getLastHurtByMob() != null && owner.getLastHurtByMob().isAlive() && !TameableUtils.hasSameOwnerAs(mob, owner.getLastHurtByMob())) {
                            blocking = owner.getLastHurtByMob();
                            blockingFrom = owner;
                        }
                        if (owner.getLastHurtMob() != null && owner.getLastHurtMob().isAlive() && !TameableUtils.hasSameOwnerAs(mob, owner.getLastHurtMob())) {
                            blocking = owner.getLastHurtMob();
                            blockingFrom = owner;
                        }
                    }
                    if (blocking != null) {
                        int width = psychicWallLevel + 1;
                        float yAdditional = blocking.getBbHeight() * 0.5F + width * 0.5F;
                        Vec3 vec3 = blockingFrom.position().add(0, yAdditional, 0);
                        Vec3 vec32 = blocking.position().add(0, yAdditional, 0);
                        Vec3 vec33 = vec3.add(vec32);
                        Vec3 avg = new Vec3(vec33.x / 2F, Math.floor(vec33.y / 2F), vec33.z / 2F);
                        Vec3 rotationFrom = avg.subtract(vec3);
                        Direction dir = Direction.getNearest(rotationFrom.x, rotationFrom.y, rotationFrom.z);
                        PsychicWallEntity wall = DIEntityRegistry.PSYCHIC_WALL.get().create(mob.level);
                        wall.setPos(avg.x, avg.y, avg.z);
                        wall.setBlockWidth(width);
                        wall.setCreatorId(mob.getUUID());
                        wall.setLifespan(psychicWallLevel * 100);
                        wall.setWallDirection(dir);
                        mob.level.addFreshEntity(wall);
                        TameableUtils.setPsychicWallCooldown(mob, psychicWallLevel * 200 + 40);
                    }
                }
            }
            if (TameableUtils.hasEnchant(event.getEntity(), DIEnchantmentRegistry.BLAZING_PROTECTION)) {
                int bars = TameableUtils.getBlazingProtectionBars(event.getEntity());
                if (bars < 2 * TameableUtils.getEnchantLevel(event.getEntity(), DIEnchantmentRegistry.BLAZING_PROTECTION)) {
                    int cooldown = TameableUtils.getBlazingProtectionCooldown(event.getEntity());
                    if (cooldown > 0) {
                        cooldown--;
                    } else {
                        TameableUtils.setBlazingProtectionBars(event.getEntity(), bars + 1);
                        cooldown = 200;
                    }
                    TameableUtils.setBlazingProtectionCooldown(event.getEntity(), cooldown);
                }
            }
            if (TameableUtils.hasEnchant(event.getEntity(), DIEnchantmentRegistry.HEALING_AURA)) {
                int time = TameableUtils.getHealingAuraTime(event.getEntity());
                if (time > 0) {
                    List<LivingEntity> hurtNearby = TameableUtils.getAuraHealables(event.getEntity());
                    for (LivingEntity needsHealing : hurtNearby) {
                        if (!needsHealing.hasEffect(MobEffects.REGENERATION)) {
                            needsHealing.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, TameableUtils.getEnchantLevel(event.getEntity(), DIEnchantmentRegistry.HEALING_AURA) - 1));
                        }
                    }
                    time--;
                    if (time == 0) {
                        time = -600 - event.getEntity().getRandom().nextInt(600);
                    }
                } else if (time < 0) {
                    time++;
                } else if ((event.getEntity().tickCount + event.getEntity().getId()) % 200 == 0 || TameableUtils.getHealingAuraImpulse(event.getEntity())) {
                    List<LivingEntity> hurtNearby = TameableUtils.getAuraHealables(event.getEntity());
                    if (!hurtNearby.isEmpty()) {
                        time = 200;
                    }
                    TameableUtils.setHealingAuraImpulse(event.getEntity(), false);
                }
                TameableUtils.setHealingAuraTime(event.getEntity(), time);
            }
        }

        if (frozenTime > 0) {
            TameableUtils.setFrozenTimeTag(event.getEntity(), frozenTime - 1);
            AttributeInstance instance = event.getEntity().getAttribute(Attributes.MOVEMENT_SPEED);
            if (instance != null) {
                float f = -0.1F * event.getEntity().getPercentFrozen();
                if (frozenTime > 1) {
                    AttributeModifier fangModifier = new AttributeModifier(FROST_FANG_SLOW, "Frost fang slow", f, AttributeModifier.Operation.ADDITION);
                    if (!instance.hasModifier(fangModifier)) {
                        instance.addTransientModifier(fangModifier);
                    }
                } else {
                    instance.removeModifier(FROST_FANG_SLOW);
                }
            }
            for (int i = 0; i < 1 + event.getEntity().getRandom().nextInt(2); i++) {
                event.getEntity().level.addParticle(ParticleTypes.SNOWFLAKE, event.getEntity().getRandomX(0.7F), event.getEntity().getRandomY(), event.getEntity().getRandomZ(0.7F), 0.0F, 0.0F, 0.0F);
            }
        }
    }

    @SubscribeEvent
    public void onLivingHurt(LivingAttackEvent event) {
        if (TameableUtils.isTamed(event.getEntity()) && event.getSource() != DIDamageTypes.SIPHON_DAMAGE) {
            boolean flag = false;
            if (TameableUtils.hasEnchant(event.getEntity(), DIEnchantmentRegistry.IMMUNITY_FRAME)) {
                int level = TameableUtils.getEnchantLevel(event.getEntity(), DIEnchantmentRegistry.IMMUNITY_FRAME);
                if (TameableUtils.getImmuneTime(event.getEntity()) <= 0) {
                    TameableUtils.setImmuneTime(event.getEntity(), 20 + level * 20);
                } else {
                    flag = true;
                    event.setCanceled(true);
                }
            }
            if (TameableUtils.hasEnchant(event.getEntity(), DIEnchantmentRegistry.BLAZING_PROTECTION)) {
                int bars = TameableUtils.getBlazingProtectionBars(event.getEntity());
                if (bars > 0) {
                    Entity attacker = event.getSource().getEntity();
                    if (attacker instanceof LivingEntity && !TameableUtils.hasSameOwnerAs((LivingEntity) attacker, event.getEntity())) {
                        attacker.setSecondsOnFire(5 + event.getEntity().getRandom().nextInt(3));
                        ((LivingEntity) attacker).knockback(0.4, event.getEntity().getX() - attacker.getX(), event.getEntity().getZ() - attacker.getZ());
                    }
                    event.setCanceled(true);
                    flag = true;
                    for (int i = 0; i < 3 + event.getEntity().getRandom().nextInt(3); i++) {
                        attacker.level.addParticle(ParticleTypes.FLAME, event.getEntity().getRandomX(0.8F), event.getEntity().getRandomY(), event.getEntity().getRandomZ(0.8F), 0.0F, 0.0F, 0.0F);
                    }
                    event.getEntity().playSound(DISoundRegistry.BLAZING_PROTECTION.get(), 1, event.getEntity().getVoicePitch());
                    TameableUtils.setBlazingProtectionBars(event.getEntity(), bars - 1);
                    TameableUtils.setBlazingProtectionCooldown(event.getEntity(), 600);
                }
            }
            if ((event.getSource() == DamageSource.DROWN || event.getSource() == DamageSource.DRY_OUT) && TameableUtils.hasEnchant(event.getEntity(), DIEnchantmentRegistry.AMPHIBIOUS)) {
                event.setCanceled(true);
                flag = true;
            }
            if (!flag && (event.getSource().isFall() || event.getSource() == DamageSource.OUT_OF_WORLD) && TameableUtils.hasEnchant(event.getEntity(), DIEnchantmentRegistry.VOID_CLOUD)) {
                event.setCanceled(true);
                flag = true;
            }
            if (!flag && TameableUtils.hasEnchant(event.getEntity(), DIEnchantmentRegistry.HEALTH_SIPHON)) {
                Entity owner = TameableUtils.getOwnerOf(event.getEntity());
                if (owner != null && owner.isAlive() && owner.distanceTo(event.getEntity()) < 100 && owner != event.getEntity()) {
                    owner.hurt(event.getSource(), event.getAmount());
                    event.setCanceled(true);
                    flag = true;
                    event.getEntity().hurt(DIDamageTypes.SIPHON_DAMAGE, 0.0F);
                }
            }
            if (!flag && TameableUtils.hasEnchant(event.getEntity(), DIEnchantmentRegistry.TOTAL_RECALL) && event.getEntity().getHealth() - event.getAmount() <= 2.0D && !TameableUtils.isZombiePet(event.getEntity())) {
                UUID owner = TameableUtils.getOwnerUUIDOf(event.getEntity());
                if (owner != null) {
                    if (event.getEntity() instanceof Mob mob) {
                        mob.playAmbientSound();
                    }
                    event.getEntity().playSound(SoundEvents.ENDER_CHEST_CLOSE, 1.0F, 1.5F);
                    RecallBallEntity recallBall = DIEntityRegistry.RECALL_BALL.get().create(event.getEntity().level);
                    recallBall.setOwnerUUID(owner);
                    CompoundTag tag = new CompoundTag();
                    event.getEntity().addAdditionalSaveData(tag);
                    recallBall.setContainedData(tag);
                    recallBall.setContainedEntityType(ForgeRegistries.ENTITY_TYPES.getKey(event.getEntity().getType()).toString());
                    recallBall.copyPosition(event.getEntity());
                    recallBall.setYRot(event.getEntity().getYRot());
                    recallBall.setInvulnerable(true);
                    event.getEntity().stopRiding();
                    if (event.getEntity().level.addFreshEntity(recallBall)) {
                        event.getEntity().discard();
                    }
                    flag = true;
                    event.setCanceled(true);
                }
            }
        }
        if (event.getSource().getEntity() != null && TameableUtils.isTamed(event.getSource().getEntity())) {
            LivingEntity attacker = (LivingEntity) event.getSource().getEntity();
            int lightningLevel = TameableUtils.getEnchantLevel(attacker, DIEnchantmentRegistry.CHAIN_LIGHTNING);
            int bubblingLevel = TameableUtils.getEnchantLevel(attacker, DIEnchantmentRegistry.BUBBLING);
            int vampireLevel = TameableUtils.getEnchantLevel(attacker, DIEnchantmentRegistry.VAMPIRE);

            if (lightningLevel > 0) {
                ChainLightningEntity lightning = DIEntityRegistry.CHAIN_LIGHTNING.get().create(event.getEntity().level);
                lightning.setCreatorEntityID(attacker.getId());
                lightning.setFromEntityID(attacker.getId());
                lightning.setToEntityID(event.getEntity().getId());
                lightning.copyPosition(event.getEntity());
                lightning.setChainsLeft(3 + lightningLevel * 3);
                event.getEntity().level.addFreshEntity(lightning);
                event.getEntity().playSound(DISoundRegistry.CHAIN_LIGHTNING.get(), 1F, 1F);
            }
            if (TameableUtils.hasEnchant(attacker, DIEnchantmentRegistry.FROST_FANG)) {
                event.getEntity().setTicksFrozen(event.getEntity().getTicksRequiredToFreeze() + 200);
                Vec3 vec3 = event.getEntity().getEyePosition().subtract(attacker.getEyePosition()).normalize().scale(attacker.getBbWidth() + 0.5F);
                Vec3 vec32 = attacker.getEyePosition().add(vec3);
                for (int i = 0; i < 3 + attacker.getRandom().nextInt(3); i++) {
                    float f1 = 0.2F * (attacker.getRandom().nextFloat() - 1.0F);
                    float f2 = 0.2F * (attacker.getRandom().nextFloat() - 1.0F);
                    float f3 = 0.2F * (attacker.getRandom().nextFloat() - 1.0F);
                    attacker.level.addParticle(ParticleTypes.SNOWFLAKE, vec32.x + f1, vec32.y + f2, vec32.z + f3, 0.0F, 0.0F, 0.0F);
                }
                TameableUtils.setFrozenTimeTag(event.getEntity(), 60);
            }
            if (bubblingLevel > 0) {
                if (!(event.getEntity().getRootVehicle() instanceof GiantBubbleEntity) && (event.getEntity().isOnGround() || event.getEntity().isInWaterOrBubble() || event.getEntity().isInLava())) {
                    GiantBubbleEntity bubble = DIEntityRegistry.GIANT_BUBBLE.get().create(event.getEntity().level);
                    bubble.copyPosition(event.getEntity());
                    event.getEntity().startRiding(bubble, true);
                    bubble.setpopsIn(bubblingLevel * 40 + 40);
                    event.getEntity().level.addFreshEntity(bubble);
                    event.getEntity().playSound(DISoundRegistry.GIANT_BUBBLE_INFLATE.get(), 1F, 1F);

                }
            }
            if (vampireLevel > 0) {
                if (attacker.getHealth() < attacker.getMaxHealth()) {
                    float f = Mth.clamp(event.getAmount() * vampireLevel * 0.5F, 1F, 10F);
                    attacker.heal(f);
                    if (event.getEntity().level instanceof ServerLevel) {
                        for (int i = 0; i < 5 + event.getEntity().getRandom().nextInt(3); i++) {
                            double f1 = event.getEntity().getRandomX(0.7F);
                            double f2 = event.getEntity().getY(0.4F + event.getEntity().getRandom().nextFloat() * 0.2F);
                            double f3 = event.getEntity().getRandomZ(0.7F);
                            Vec3 motion = attacker.getEyePosition().subtract(f1, f2, f3).normalize().scale(0.2F);
                            ((ServerLevel) event.getEntity().level).sendParticles(DIParticleRegistry.VAMPIRE.get(), f1, f2, f3, 1, motion.x, motion.y, motion.z, 0.2F);
                        }
                    }
                }
            }
            if (!event.getEntity().level.isClientSide && TameableUtils.hasEnchant(attacker, DIEnchantmentRegistry.WARPING_BITE)) {
                for (int i = 0; i < 16; ++i) {
                    double d3 = event.getEntity().getX() + (attacker.getRandom().nextDouble() - 0.5D) * 16.0D;
                    double d4 = Mth.clamp(event.getEntity().getY() + (double) (attacker.getRandom().nextInt(16) - 8), event.getEntity().level.getMinBuildHeight(), event.getEntity().level.getMinBuildHeight() + ((ServerLevel) event.getEntity().level).getLogicalHeight() - 1);
                    double d5 = event.getEntity().getZ() + (attacker.getRandom().nextDouble() - 0.5D) * 16.0D;
                    if (event.getEntity().randomTeleport(d3, d4, d5, true)) {
                        SoundEvent soundevent = event.getEntity() instanceof Fox ? SoundEvents.FOX_TELEPORT : SoundEvents.CHORUS_FRUIT_TELEPORT;
                        event.getEntity().playSound(soundevent, 1.0F, 1.0F);
                        break;
                    }
                }
            }
        }
        if (!event.isCanceled()) {
            List<LivingEntity> nearbyHealers = TameableUtils.getNearbyHealers(event.getEntity());
            if (!nearbyHealers.isEmpty()) {
                for (LivingEntity healer : nearbyHealers) {
                    TameableUtils.setHealingAuraImpulse(healer, true);
                }
            }
        }
    }

    @SubscribeEvent
    public void onLivingDamage(LivingDamageEvent event) {
        if(TameableUtils.isTamed(event.getEntity()) && event.getSource().getDirectEntity() instanceof Player player && TameableUtils.isPetOf(player, event.getEntity()) && !player.isShiftKeyDown()){
            event.setCanceled(true);
        }
        if (event.getSource().getEntity() instanceof LivingEntity && TameableUtils.isTamed(event.getSource().getEntity())) {
            LivingEntity pet = (LivingEntity) event.getSource().getEntity();
            if (TameableUtils.hasEnchant(pet, DIEnchantmentRegistry.IMMATURITY_CURSE)) {
                event.setAmount((float) Math.ceil(event.getAmount() * 0.7F));
            }
        }
    }

    @SubscribeEvent
    public void onLivingDie(LivingDeathEvent event) {
        if (TameableUtils.isTamed(event.getEntity()) && !TameableUtils.isZombiePet(event.getEntity())) {

            BlockPos bedPos = TameableUtils.getPetBedPos(event.getEntity());
            if (bedPos != null) {
                CompoundTag data = new CompoundTag();
                event.getEntity().addAdditionalSaveData(data);
                String saveName = event.getEntity().hasCustomName() ? event.getEntity().getCustomName().getString() : "";
                RespawnRequest request = new RespawnRequest(ForgeRegistries.ENTITY_TYPES.getKey(event.getEntity().getType()).toString(), TameableUtils.getPetBedDimension(event.getEntity()), data, bedPos, event.getEntity().level.dayTime(), saveName);
                DIWorldData worldData = DIWorldData.get(event.getEntity().level);
                if (worldData != null) {
                    worldData.addRespawnRequest(request);
                }
            }
            if (!(event.getEntity() instanceof TamableAnimal)) {
                Entity owner = TameableUtils.getOwnerOf(event.getEntity());
                if (!event.getEntity().level.isClientSide && event.getEntity().level.getGameRules().getBoolean(GameRules.RULE_SHOWDEATHMESSAGES) && owner instanceof ServerPlayer) {
                    owner.sendSystemMessage(event.getEntity().getCombatTracker().getDeathMessage());
                }
            }
            if (event.getEntity() instanceof Mob mob && event.getEntity().level.getDifficulty() != Difficulty.PEACEFUL && TameableUtils.hasEnchant(mob, DIEnchantmentRegistry.UNDEAD_CURSE)) {
                Mob zombieCopy = (Mob) mob.getType().create(mob.level);
                int id = zombieCopy.getId();
                Entity owner = TameableUtils.getOwnerOf(mob);
                CompoundTag livingNbt = new CompoundTag();
                mob.addAdditionalSaveData(livingNbt);
                livingNbt.putString("DeathLootTable", BuiltInLootTables.EMPTY.toString());
                zombieCopy.readAdditionalSaveData(livingNbt);
                zombieCopy.setId(id);
                if (zombieCopy instanceof TamableAnimal tamed) {
                    tamed.setTame(false);
                    tamed.setOwnerUUID(null);
                    tamed.setOrderedToSit(false);
                }
                if (zombieCopy instanceof ModifedToBeTameable tameable) {
                    tameable.setTame(false);
                    tameable.setTameOwnerUUID(null);
                }
                if (zombieCopy instanceof IComandableMob commandableMob) {
                    commandableMob.setCommand(0);
                }
                zombieCopy.copyPosition(mob);
                zombieCopy.setTarget(owner instanceof Player && !((Player) owner).isCreative() ? (Player) owner : mob.level.getNearestPlayer(ZOMBIE_TARGET, mob));
                mob.level.addFreshEntity(zombieCopy);
                zombieCopy.setHealth(zombieCopy.getMaxHealth());
                TameableUtils.setZombiePet(zombieCopy, true);
            }
        }
    }

    @SubscribeEvent
    public void onEntityMount(EntityMountEvent event) {
        if (event.getEntityBeingMounted() instanceof GiantBubbleEntity && event.isDismounting() && event.getEntityBeingMounted().isAlive()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onInteractWithEntity(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        Entity entity = event.getTarget();
        ItemStack stack = event.getItemStack();
        if (TameableUtils.isTamed(event.getTarget())) {
            if (event.getItemStack().is(DIItemRegistry.DEED_OF_OWNERSHIP.get())) {
                CompoundTag tag = stack.getTag();
                boolean unbound = !DeedOfOwnershipItem.isBound(event.getItemStack());
                Entity currentOwner = TameableUtils.getOwnerOf(entity);
                if (TameableUtils.isTamed(entity) && currentOwner != null && currentOwner.equals(player) && unbound) {
                    CompoundTag newTag = new CompoundTag();
                    newTag.putBoolean("HasBoundEntity", true);
                    newTag.putUUID("BoundEntity", entity.getUUID());
                    newTag.putString("BoundEntityName", entity.getName().getString());
                    stack.setTag(newTag);
                    event.setCanceled(true);
                    event.setCancellationResult(InteractionResult.SUCCESS);
                    return;
                }
                if (TameableUtils.isTamed(entity) && tag != null && tag.getBoolean("HasBoundEntity") && tag.getUUID("BoundEntity") != null) {
                    UUID fromItem = tag.getUUID("BoundEntity");
                    if (entity.getUUID().equals(fromItem)) {
                        player.getCooldowns().addCooldown(stack.getItem(), 5);
                        TameableUtils.setOwnerUUIDOf(entity, player.getUUID());
                        player.displayClientMessage(Component.translatable("message.domesticationinnovation.set_owner", player.getName(), entity.getName()), true);
                        if (currentOwner instanceof Player && !currentOwner.equals(player)) {
                            ((Player) currentOwner).displayClientMessage(Component.translatable("message.domesticationinnovation.set_owner", player.getName(), entity.getName()), true);
                        }
                        stack.setTag(new CompoundTag());
                        if (!player.isCreative()) {
                            stack.shrink(1);
                        }
                        event.setCanceled(true);
                        event.setCancellationResult(InteractionResult.SUCCESS);
                    }
                }

            }
        }
        if (TameableUtils.couldBeTamed(event.getTarget()) && TameableUtils.isZombiePet((LivingEntity) event.getTarget())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            return;
        }
        if (event.getTarget() instanceof LivingEntity living && TameableUtils.isTamed(entity) && TameableUtils.hasEnchant(living, DIEnchantmentRegistry.GLUTTONOUS)) {
            if (stack.getItem().isEdible() && living.getHealth() < living.getMaxHealth() && stack.getItem().getFoodProperties() != null) {
                living.heal((float) Math.floor(stack.getItem().getFoodProperties().getNutrition() * 1.5F));
                if (!event.getEntity().isCreative()) {
                    stack.shrink(1);
                }
                living.playSound(living.getRandom().nextBoolean() ? SoundEvents.PLAYER_BURP : SoundEvents.GENERIC_EAT, 1F, living.getVoicePitch());
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
            }
        }
        if (event.getTarget() instanceof Rabbit rabbit && DomesticationMod.CONFIG.tameableRabbit.get()) {
            if (stack.getItem() == Items.HAY_BLOCK) {
                if (TameableUtils.isTamed(rabbit) && rabbit.getHealth() < rabbit.getMaxHealth()) {
                    rabbit.heal(3);
                    if (!event.getEntity().isCreative()) {
                        stack.shrink(1);
                    }
                    event.setCanceled(true);
                    event.setCancellationResult(InteractionResult.SUCCESS);
                    return;
                }
                if (!TameableUtils.isTamed(rabbit) && !rabbit.level.isClientSide) {
                    if (!event.getEntity().isCreative()) {
                        stack.shrink(1);
                    }
                    rabbit.playSound(SoundEvents.FOX_EAT);
                    if (rabbit.getRandom().nextBoolean()) {
                        for (int i = 0; i < 3; ++i) {
                            double d0 = rabbit.getRandom().nextGaussian() * 0.02D;
                            double d1 = rabbit.getRandom().nextGaussian() * 0.02D;
                            double d2 = rabbit.getRandom().nextGaussian() * 0.02D;
                            ((ServerLevel) rabbit.getLevel()).sendParticles(ParticleTypes.HEART, rabbit.getRandomX(1.0D), rabbit.getRandomY() + 0.5D, rabbit.getRandomZ(1.0D), 3, d0, d1, d2, 0.02F);
                        }
                        ((ModifedToBeTameable) rabbit).setTame(true);
                        ((ModifedToBeTameable) rabbit).setTameOwnerUUID(event.getEntity().getUUID());
                        ((IComandableMob) rabbit).setCommand(1);
                    } else {
                        for (int i = 0; i < 3; ++i) {
                            double d0 = rabbit.getRandom().nextGaussian() * 0.02D;
                            double d1 = rabbit.getRandom().nextGaussian() * 0.02D;
                            double d2 = rabbit.getRandom().nextGaussian() * 0.02D;
                            ((ServerLevel) rabbit.getLevel()).sendParticles(ParticleTypes.SMOKE, rabbit.getRandomX(1.0D), rabbit.getRandomY() + 0.5D, rabbit.getRandomZ(1.0D), 3, d0, d1, d2, 0.02F);
                        }
                    }
                    event.setCanceled(true);
                    event.setCancellationResult(InteractionResult.SUCCESS);
                    return;
                }
            }
            if (TameableUtils.isTamed(rabbit) && TameableUtils.isPetOf(event.getEntity(), rabbit)) {
                ((IComandableMob) rabbit).playerSetCommand(event.getEntity(), rabbit);
            }
        }
        if (event.getTarget() instanceof LivingEntity living && TameableUtils.isPetOf(event.getEntity(), entity) && !living.getType().is(DITagRegistry.REFUSES_COLLAR_TAGS)) {
            if (event.getItemStack().is(DIItemRegistry.COLLAR_TAG.get()) && DomesticationMod.CONFIG.collarTag.get()) {
                if (!event.getEntity().level.isClientSide && living.isAlive()) {
                    Map<Enchantment, Integer> itemEnchantments = EnchantmentHelper.deserializeEnchantments(stack.getEnchantmentTags());
                    Map<ResourceLocation, Integer> entityEnchantments = TameableUtils.getEnchants(living);
                    if (stack.hasCustomHoverName() && living.hasCustomName() && stack.getHoverName().equals(living.getCustomName())) {
                        boolean hasSameEnchants = itemEnchantments.isEmpty();
                        if (entityEnchantments != null) {
                            hasSameEnchants = true;
                            for (Map.Entry<Enchantment, Integer> itemEntry : itemEnchantments.entrySet()) {
                                ResourceLocation name = ForgeRegistries.ENCHANTMENTS.getKey(itemEntry.getKey());
                                if (entityEnchantments.get(name) == null || !entityEnchantments.get(name).equals(itemEntry.getValue())) {
                                    hasSameEnchants = false;
                                }
                            }
                        }
                        if (hasSameEnchants) {
                            event.setCanceled(true);
                            event.setCancellationResult(InteractionResult.FAIL);
                            return;
                        }
                    }
                    if (stack.hasCustomHoverName()) {
                        living.setCustomName(stack.getHoverName());
                    }
                    if (!event.getEntity().isCreative()) {
                        stack.shrink(1);
                    }
                    blockCollarTick(living);
                    if (TameableUtils.hasCollar(living)) {
                        ItemStack collarFrom = new ItemStack(DIItemRegistry.COLLAR_TAG.get());
                        if (entityEnchantments != null) {
                            collarFrom.getOrCreateTag();
                            if (!collarFrom.getTag().contains("Enchantments", 9)) {
                                collarFrom.getTag().put("Enchantments", new ListTag());
                            }

                            ListTag listtag = collarFrom.getTag().getList("Enchantments", 10);
                            for (Map.Entry<ResourceLocation, Integer> entry : entityEnchantments.entrySet()) {
                                listtag.add(EnchantmentHelper.storeEnchantment(entry.getKey(), entry.getValue()));
                            }
                        } else {
                            collarFrom.setTag(null);
                        }
                        living.spawnAtLocation(collarFrom);
                    }
                    living.playSound(DISoundRegistry.COLLAR_TAG.get(), 1, 1);
                    if (itemEnchantments.isEmpty()) {
                        TameableUtils.clearEnchants(living);
                    } else {
                        ListTag listTag = new ListTag();
                        for (Map.Entry<Enchantment, Integer> entry : itemEnchantments.entrySet()) {
                            TameableUtils.addEnchant(living, new EnchantmentInstance(entry.getKey(), entry.getValue()), listTag);
                        }
                    }
                }
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
            }
        }
    }

    @SubscribeEvent
    public void onLootTableLoad(LootTableLoadEvent event) {
        if (event.getName().equals(BuiltInLootTables.WOODLAND_MANSION) && DomesticationMod.CONFIG.sinisterCarrotLootChance.get() > 0) {
            LootPoolEntryContainer.Builder item = LootItem.lootTableItem(DIItemRegistry.SINISTER_CARROT.get()).setWeight(1);
            LootPool.Builder builder = new LootPool.Builder().name("di_bubbling_book").add(item).when(LootItemRandomChanceCondition.randomChance(DomesticationMod.CONFIG.sinisterCarrotLootChance.get().floatValue())).setRolls(UniformGenerator.between(0, 1)).setBonusRolls(UniformGenerator.between(0, 1));
            event.getTable().addPool(builder.build());
        }
        if (event.getName().equals(BuiltInLootTables.BURIED_TREASURE) && DomesticationMod.CONFIG.isEnchantEnabled(DIEnchantmentRegistry.BUBBLING) && DomesticationMod.CONFIG.bubblingLootChance.get() > 0) {
            LootPoolEntryContainer.Builder item = LootItem.lootTableItem(Items.BOOK).setWeight(5).apply((new EnchantRandomlyFunction.Builder()).withEnchantment(DIEnchantmentRegistry.BUBBLING)).setWeight(1);
            LootPool.Builder builder = new LootPool.Builder().name("di_bubbling_book").add(item).when(LootItemRandomChanceCondition.randomChance(DomesticationMod.CONFIG.bubblingLootChance.get().floatValue())).setRolls(UniformGenerator.between(0, 1)).setBonusRolls(UniformGenerator.between(0, 1));
            event.getTable().addPool(builder.build());
        }
        if (event.getName().equals(BuiltInLootTables.WOODLAND_MANSION) && DomesticationMod.CONFIG.isEnchantEnabled(DIEnchantmentRegistry.VAMPIRE) && DomesticationMod.CONFIG.vampirismLootChance.get() > 0) {
            LootPoolEntryContainer.Builder item = LootItem.lootTableItem(Items.BOOK).setWeight(5).apply((new EnchantRandomlyFunction.Builder()).withEnchantment(DIEnchantmentRegistry.VAMPIRE)).setWeight(1);
            LootPool.Builder builder = new LootPool.Builder().name("di_vampire_book").add(item).when(LootItemRandomChanceCondition.randomChance(DomesticationMod.CONFIG.vampirismLootChance.get().floatValue())).setRolls(UniformGenerator.between(0, 1)).setBonusRolls(UniformGenerator.between(0, 1));
            event.getTable().addPool(builder.build());
        }
        if (event.getName().equals(BuiltInLootTables.END_CITY_TREASURE) && DomesticationMod.CONFIG.isEnchantEnabled(DIEnchantmentRegistry.VOID_CLOUD) && DomesticationMod.CONFIG.voidCloudLootChance.get() > 0) {
            LootPoolEntryContainer.Builder item = LootItem.lootTableItem(Items.BOOK).setWeight(5).apply((new EnchantRandomlyFunction.Builder()).withEnchantment(DIEnchantmentRegistry.VOID_CLOUD)).setWeight(1);
            LootPool.Builder builder = new LootPool.Builder().name("di_void_cloud_book").add(item).when(LootItemRandomChanceCondition.randomChance(DomesticationMod.CONFIG.voidCloudLootChance.get().floatValue())).setRolls(UniformGenerator.between(0, 1)).setBonusRolls(UniformGenerator.between(0, 1));
            event.getTable().addPool(builder.build());
        }
        if (event.getName().equals(BuiltInLootTables.ABANDONED_MINESHAFT) && DomesticationMod.CONFIG.isEnchantEnabled(DIEnchantmentRegistry.ORE_SCENTING) && DomesticationMod.CONFIG.oreScentingLootChance.get() > 0) {
            LootPoolEntryContainer.Builder item = LootItem.lootTableItem(Items.BOOK).setWeight(5).apply((new EnchantRandomlyFunction.Builder()).withEnchantment(DIEnchantmentRegistry.ORE_SCENTING)).setWeight(1);
            LootPool.Builder builder = new LootPool.Builder().name("di_ore_scenting_book").add(item).when(LootItemRandomChanceCondition.randomChance(DomesticationMod.CONFIG.oreScentingLootChance.get().floatValue())).setRolls(UniformGenerator.between(0, 1)).setBonusRolls(UniformGenerator.between(0, 1));
            event.getTable().addPool(builder.build());
        }
        if (event.getName().equals(BuiltInLootTables.ANCIENT_CITY) && DomesticationMod.CONFIG.isEnchantEnabled(DIEnchantmentRegistry.MUFFLED) && DomesticationMod.CONFIG.muffledLootChance.get() > 0) {
            LootPoolEntryContainer.Builder item = LootItem.lootTableItem(Items.BOOK).setWeight(5).apply((new EnchantRandomlyFunction.Builder()).withEnchantment(DIEnchantmentRegistry.MUFFLED)).setWeight(1);
            LootPool.Builder builder = new LootPool.Builder().name("di_muffled_book").add(item).when(LootItemRandomChanceCondition.randomChance(DomesticationMod.CONFIG.muffledLootChance.get().floatValue())).setRolls(UniformGenerator.between(0, 1)).setBonusRolls(UniformGenerator.between(0, 1));
            event.getTable().addPool(builder.build());
        }
        if (event.getName().equals(BuiltInLootTables.NETHER_BRIDGE) && DomesticationMod.CONFIG.isEnchantEnabled(DIEnchantmentRegistry.BLAZING_PROTECTION) && DomesticationMod.CONFIG.blazingProtectionLootChance.get() > 0) {
            LootPoolEntryContainer.Builder item = LootItem.lootTableItem(Items.BOOK).setWeight(5).apply((new EnchantRandomlyFunction.Builder()).withEnchantment(DIEnchantmentRegistry.BLAZING_PROTECTION)).setWeight(1);
            LootPool.Builder builder = new LootPool.Builder().name("di_blazing_protection_book").add(item).when(LootItemRandomChanceCondition.randomChance(DomesticationMod.CONFIG.blazingProtectionLootChance.get().floatValue())).setRolls(UniformGenerator.between(0, 1)).setBonusRolls(UniformGenerator.between(0, 1));
            event.getTable().addPool(builder.build());
        }
    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getState().getBlock() instanceof PetBedBlock) {
            if (event.getLevel().getBlockEntity(event.getPos()) instanceof PetBedBlockEntity entity1) {
                entity1.removeAllRequestsFor(event.getPlayer());
                entity1.resetBedsForNearbyPets();
            }
        }
    }

    @SubscribeEvent
    public void onEntityJoinWorld(LivingSpawnEvent.SpecialSpawn event) {
        try {
            if (event.getEntity() != null && event.getEntity() instanceof Ravager && DomesticationMod.CONFIG.rabbitsScareRavagers.get()) {
                Ravager ravager = (Ravager) event.getEntity();
                ravager.goalSelector.addGoal(4, new AvoidEntityGoal(ravager, Rabbit.class, 13.0F, 1.5D, 2.0D, EntitySelector.NO_SPECTATORS));
            }
        } catch (Exception e) {
            DomesticationMod.LOGGER.warn("could not add ai tasks to ravager");
        }
    }

    @SubscribeEvent
    public void onVillagerTrades(VillagerTradesEvent event) {
        if (event.getType() == DIVillagerRegistry.ANIMAL_TAMER.get()) {
            List<VillagerTrades.ItemListing> level1 = new ArrayList<>();
            List<VillagerTrades.ItemListing> level2 = new ArrayList<>();
            List<VillagerTrades.ItemListing> level3 = new ArrayList<>();
            List<VillagerTrades.ItemListing> level4 = new ArrayList<>();
            List<VillagerTrades.ItemListing> level5 = new ArrayList<>();
            level1.add(new BuyingItemTrade(Items.TROPICAL_FISH, 10, 2, 10, 2));
            level1.add(new SellingItemTrade(Items.BONE, 3, 10, 6, 4));
            level1.add(new BuyingItemTrade(Items.HAY_BLOCK, 7, 1, 9, 1));
            level1.add(new SellingItemTrade(Items.COD, 2, 7, 6, 3));
            level1.add(new SellingItemTrade(Items.EGG, 4, 2, 9, 3));
            level1.add(new SellingItemTrade(DIItemRegistry.FEATHER_ON_A_STICK.get(), 3, 1, 2, 3));
            level2.add(new SellingItemTrade(Items.TROPICAL_FISH_BUCKET, 2, 1, 6, 7));
            level2.add(new BuyingItemTrade(DIItemRegistry.COLLAR_TAG.get(), 5, 1, 12, 7));
            level2.add(new SellingItemTrade(Items.APPLE, 4, 12, 3, 7));
            level2.add(new SellingOneOfTheseItemsTrade(ImmutableSet.of(
                    DIBlockRegistry.WHITE_PET_BED.get(), DIBlockRegistry.ORANGE_PET_BED.get(), DIBlockRegistry.MAGENTA_PET_BED.get(), DIBlockRegistry.LIGHT_BLUE_PET_BED.get(), DIBlockRegistry.YELLOW_PET_BED.get(), DIBlockRegistry.LIME_PET_BED.get(), DIBlockRegistry.PINK_PET_BED.get(), DIBlockRegistry.GRAY_PET_BED.get(), DIBlockRegistry.LIGHT_GRAY_PET_BED.get(), DIBlockRegistry.CYAN_PET_BED.get(), DIBlockRegistry.PURPLE_PET_BED.get(), DIBlockRegistry.BLUE_PET_BED.get(), DIBlockRegistry.BROWN_PET_BED.get(), DIBlockRegistry.GREEN_PET_BED.get(), DIBlockRegistry.RED_PET_BED.get(), DIBlockRegistry.BLACK_PET_BED.get()
            ), 2, 1, 6, 7));
            level2.add(new SellingItemTrade(DIItemRegistry.DEED_OF_OWNERSHIP.get(), 3, 1, 2, 7));
            level3.add(new SellingItemTrade(DIItemRegistry.ROTTEN_APPLE.get(), 4, 1, 1, 10));
            level3.add(new SellingItemTrade(Items.CARROT_ON_A_STICK, 3, 1, 2, 10));
            level3.add(new SellingItemTrade(Items.LEAD, 3, 2, 5, 10));
            level3.add(new SellingItemTrade(Items.LEATHER_HORSE_ARMOR, 4, 1, 3, 11));
            level3.add(new SellingItemTrade(DIBlockRegistry.DRUM.get(), 2, 3, 7, 11));
            level3.add(new SellingItemTrade(Items.TADPOLE_BUCKET, 6, 1, 4, 13));
            level3.add(new EnchantItemTrade(DIItemRegistry.COLLAR_TAG.get(), 20, 2, 8, 3, 10));
            level4.add(new SellingItemTrade(Items.IRON_HORSE_ARMOR, 8, 1, 2, 15));
            level4.add(new SellingItemTrade(Items.AXOLOTL_BUCKET, 11, 1, 2, 15));
            level4.add(new SellingItemTrade(Items.TURTLE_EGG, 26, 1, 2, 15));
            level4.add(new EnchantItemTrade(DIItemRegistry.COLLAR_TAG.get(), 40, 3, 18, 3, 15));
            level5.add(new SellingItemTrade(Items.GOLDEN_HORSE_ARMOR, 13, 1, 1, 18));
            level5.add(new SellingItemTrade(Items.SCUTE, 21, 1, 3, 18));
            level5.add(new EnchantItemTrade(DIItemRegistry.COLLAR_TAG.get(), 50, 4, 38, 3, 20));
            level5.add(new SellingEnchantedBook(DIEnchantmentRegistry.CHARISMA, 3, 12, 1, 18, 0.02F));
            event.getTrades().put(1, level1);
            event.getTrades().put(2, level2);
            event.getTrades().put(3, level3);
            event.getTrades().put(4, level4);
            event.getTrades().put(5, level5);
        }
    }

    @SubscribeEvent
    public void onLivingDrops(LivingDropsEvent event) {
        if (TameableUtils.isTamed(event.getEntity()) && TameableUtils.getPetBedPos(event.getEntity()) != null) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onExplosion(ExplosionEvent.Start event) {
        float dist = 30;
        Vec3 center = event.getExplosion().getPosition();
        Vec3 bottom = center.add(-dist, -dist, -dist);
        Vec3 top = center.add(dist, dist, dist);
        Predicate<Entity> defusal = (animal) -> TameableUtils.isTamed(animal) && TameableUtils.hasEnchant((LivingEntity) animal, DIEnchantmentRegistry.DEFUSAL);
        boolean flag = false;
        for (LivingEntity defuser : event.getLevel().getEntitiesOfClass(LivingEntity.class, new AABB(bottom, top), EntitySelector.NO_SPECTATORS.and(defusal))) {
            float level = 10 * TameableUtils.getEnchantLevel(defuser, DIEnchantmentRegistry.DEFUSAL);
            if (defuser.distanceToSqr(center) <= level * level) {
                flag = true;
                break;
            }
        }
        if (flag) {
            event.setCanceled(true);
            float pitch = 1.5F + new Random().nextFloat();
            event.getLevel().playSound(null, center.x, center.y, center.z, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1, pitch);
            if (event.getLevel() instanceof ServerLevel serverLevel) {
                for (int i = 0; i < 5; i++) {
                    serverLevel.sendParticles(ParticleTypes.CLOUD, center.x, center.y + 1.0F, center.z, 5, 0, 0F, 0, 0.2F);
                }
            }
        }
    }

    private void teleportNearbyPets(Player owner, Vec3 fromPos, Vec3 toPos, Level fromLevel, Level toLevel) {
        double dist = 20;
        boolean removeAndReadd = fromLevel.dimension() != toLevel.dimension();
        Predicate<Entity> enchantedPet = (animal) -> animal instanceof Mob && TameableUtils.isPetOf(owner, animal) && TameableUtils.isValidTeleporter(owner, (Mob) animal);
        for (Mob entity : fromLevel.getEntitiesOfClass(Mob.class, new AABB(fromPos.x - dist, fromPos.y - dist, fromPos.z - dist, fromPos.x + dist, fromPos.y + dist, fromPos.z + dist), EntitySelector.NO_SPECTATORS.and(enchantedPet))) {
            if (removeAndReadd) {
                teleportingPets.add(new Triple(entity, toLevel, owner.getUUID()));
            } else {
                EntityDimensions dimensions = entity.getDimensions(entity.getPose());
                AABB suffocationBox = new AABB(-dimensions.width / 2.0F, 0, -dimensions.width / 2.0F, dimensions.width / 2.0F, dimensions.height, dimensions.width / 2.0F);
                while (!toLevel.noCollision(entity, suffocationBox.move(toPos.x, toPos.y, toPos.z)) && toPos.y < 300) {
                    toPos = toPos.add(0, 1, 0);
                }
                entity.fallDistance = 0.0F;
                entity.teleportToWithTicket(toPos.x, toPos.y, toPos.z);
                entity.setPortalCooldown();
            }
        }
    }

    @SubscribeEvent
    public void onEntityTeleport(EntityTeleportEvent event) {
        if (event.getEntity() instanceof Player) {
            teleportNearbyPets((Player) event.getEntity(), event.getPrev(), event.getTarget(), event.getEntity().level, event.getEntity().level);
        }
    }

    @SubscribeEvent
    public void onEntityTravelToDimension(EntityTravelToDimensionEvent event) {
        if (!event.isCanceled()) {
            if (event.getEntity().level instanceof ServerLevel serverLevel && event.getEntity() instanceof Player) {
                MinecraftServer server = serverLevel.getServer();
                Level toLevel = server.getLevel(event.getDimension());
                if (toLevel != null) {
                    teleportNearbyPets((Player) event.getEntity(), event.getEntity().position(), event.getEntity().position(), event.getEntity().level, toLevel);
                }
            }
        }
    }

    @SubscribeEvent
    public void onSetAttackTarget(LivingSetAttackTargetEvent event) {
        if (TameableUtils.isTamed(event.getEntity()) && event.getTarget() instanceof Player player && TameableUtils.isPetOf(player, event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onUpdateAnvil(AnvilUpdateEvent event) {
        if(event.getLeft().is(DIItemRegistry.COLLAR_TAG.get()) && !event.getLeft().getAllEnchantments().isEmpty() && event.getRight().is(DIItemRegistry.COLLAR_TAG.get()) && !event.getRight().getAllEnchantments().isEmpty()){

            Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(event.getLeft());
            Map<Enchantment, Integer> map1 = EnchantmentHelper.getEnchantments(event.getRight());
            boolean canCombine = true;
            int i = 0;
            for(Enchantment enchantment1 : map1.keySet()) {
                if (enchantment1 != null) {
                    int i2 = map.getOrDefault(enchantment1, 0);
                    int j2 = map1.get(enchantment1);
                    j2 = i2 == j2 ? j2 + 1 : Math.max(j2, i2);

                    for(Enchantment enchantment : map.keySet()) {
                        if (enchantment != enchantment1 && !enchantment1.isCompatibleWith(enchantment)) {
                            canCombine = false;
                            ++i;
                        }
                    }

                    if (canCombine) {
                        if (j2 > enchantment1.getMaxLevel()) {
                            j2 = enchantment1.getMaxLevel();
                        }

                        map.put(enchantment1, j2);
                        int k3 = 0;
                        switch (enchantment1.getRarity()) {
                            case COMMON:
                                k3 = 1;
                                break;
                            case UNCOMMON:
                                k3 = 2;
                                break;
                            case RARE:
                                k3 = 4;
                                break;
                            case VERY_RARE:
                                k3 = 8;
                        }
                        i += k3 * j2;
                    }
                }
            }
            event.setCost(i);
            ItemStack copy = event.getLeft().copy();
            EnchantmentHelper.setEnchantments(map, copy);
            event.setOutput(copy);
        }
    }
}
