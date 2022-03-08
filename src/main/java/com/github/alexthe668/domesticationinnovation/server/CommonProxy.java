package com.github.alexthe668.domesticationinnovation.server;

import com.github.alexthe668.domesticationinnovation.DomesticationMod;
import com.github.alexthe668.domesticationinnovation.server.block.PetBedBlock;
import com.github.alexthe668.domesticationinnovation.server.block.PetBedBlockEntity;
import com.github.alexthe668.domesticationinnovation.server.enchantment.DIEnchantmentRegistry;
import com.github.alexthe668.domesticationinnovation.server.entity.*;
import com.github.alexthe668.domesticationinnovation.server.item.DIItemRegistry;
import com.github.alexthe668.domesticationinnovation.server.misc.*;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = DomesticationMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CommonProxy {

    private static final UUID FROST_FANG_SLOW = UUID.fromString("1eaf83ff-7207-4596-b37a-d7a07b3ec4cf");
    private static final TargetingConditions ZOMBIE_TARGET = TargetingConditions.forCombat().range(32.0D);

    public void init() {
    }

    public void clientInit() {
    }

    public void setupParticles() {
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
            if (TameableUtils.isTamed(hit) && TameableUtils.hasEnchant((LivingEntity) hit, DIEnchantmentRegistry.DEFLECTION)) {
                event.setCanceled(true);
                float xRot = event.getProjectile().getXRot();
                float yRot = event.getProjectile().yRotO;
                Vec3 vec3 = event.getProjectile().position().subtract(hit.position()).normalize().scale(hit.getBbWidth() + 0.5F);
                Vec3 vec32 = hit.position().add(vec3);
                hit.level.addParticle(DIParticleRegistry.DEFLECTION_SHIELD, vec32.x, vec32.y, vec32.z, xRot, yRot, 0.0F);
                event.getProjectile().setDeltaMovement(event.getProjectile().getDeltaMovement().scale(-0.2D));
                event.getProjectile().setYRot(yRot + 180);
                event.getProjectile().setXRot(xRot + 180);
            }
        }
    }

    @SubscribeEvent
    public void onItemDespawnEvent(ItemExpireEvent event) {
        if (event.getEntityItem().getItem().getItem() == Items.APPLE && DomesticationMod.CONFIG.rottenApple.get()) {
            if (new Random().nextFloat() < 0.1F * event.getEntityItem().getItem().getCount()) {
                event.getEntityItem().getItem().shrink(1);
                event.setExtraLife(10);
                ItemEntity rotten = new ItemEntity(event.getEntityItem().level, event.getEntityItem().getX(), event.getEntityItem().getY(), event.getEntityItem().getZ(), new ItemStack(DIItemRegistry.ROTTEN_APPLE.get()));
                event.getEntityItem().getLevel().addFreshEntity(rotten);
            }
        }
    }

    @SubscribeEvent
    public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        int frozenTime = TameableUtils.getFrozenTime(event.getEntityLiving());
        if (TameableUtils.couldBeTamed(event.getEntityLiving())) {
            if (TameableUtils.hasEnchant(event.getEntityLiving(), DIEnchantmentRegistry.IMMUNITY_FRAME)) {
                int i = TameableUtils.getImmuneTime(event.getEntityLiving());
                if (i > 0) {
                    TameableUtils.setImmuneTime(event.getEntityLiving(), i - 1);
                }
            }
            if (event.getEntityLiving().hasEffect(MobEffects.POISON) && TameableUtils.hasEnchant(event.getEntityLiving(), DIEnchantmentRegistry.POISON_RESISTANCE)) {
                event.getEntityLiving().removeEffect(MobEffects.POISON);
            }
            if (TameableUtils.hasEnchant(event.getEntityLiving(), DIEnchantmentRegistry.AMPHIBIOUS)) {
                event.getEntityLiving().setAirSupply(event.getEntityLiving().getMaxAirSupply());
            }
            if (TameableUtils.hasEnchant(event.getEntityLiving(), DIEnchantmentRegistry.MAGNETIC) && event.getEntityLiving() instanceof Mob mob) {
                Entity sucking = TameableUtils.getMagnetSuctionEntity(mob);
                if (!mob.level.isClientSide) {
                    if (mob.getTarget() == null || !mob.getTarget().isAlive() || mob.distanceTo(mob.getTarget()) < 0.5F + mob.getBbWidth() || mob.getRootVehicle() instanceof GiantBubbleEntity) {
                        if (TameableUtils.getMagnetSuctionEntityID(mob) != -1) {
                            TameableUtils.setMagnetSuctionEntityID(mob, -1);
                        }
                    } else {
                        TameableUtils.setMagnetSuctionEntityID(mob, mob.getTarget().getId());
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
                            mob.level.addParticle(DIParticleRegistry.MAGNET, vec3.x + f1, vec3.y + f2, vec3.z + f3, 0.0F, 0.0F, 0.0F);
                        }
                    }
                }
                if (sucking != null) {
                    if(mob.tickCount % 15 == 0){
                        mob.playSound(DISoundRegistry.MAGNET_LOOP, 1F, 1F);
                    }
                    mob.setDeltaMovement(mob.getDeltaMovement().multiply(0.88D, 1.0D, 0.88D));
                    Vec3 move = new Vec3(mob.getX() - sucking.getX(), mob.getY() - (double) sucking.getEyeHeight() / 2.0D - sucking.getY(), mob.getZ() - sucking.getZ());
                    sucking.setDeltaMovement(sucking.getDeltaMovement().add(move.normalize().scale(0.15D)));
                }
            }
            if (TameableUtils.hasEnchant(event.getEntityLiving(), DIEnchantmentRegistry.LINKED_INVENTORY) && event.getEntityLiving() instanceof Mob mob) {
                if (!mob.canPickUpLoot()) {
                    mob.setCanPickUpLoot(true);
                }
            }
            int shepherdLvl = TameableUtils.getEnchantLevel(event.getEntityLiving(), DIEnchantmentRegistry.SHEPHERD);
            if (shepherdLvl > 0) {
                TameableUtils.attractAnimals(event.getEntityLiving(), shepherdLvl * 3);
            }
            if (TameableUtils.hasEnchant(event.getEntityLiving(), DIEnchantmentRegistry.INFAMY_CURSE)) {
                TameableUtils.aggroRandomMonsters(event.getEntityLiving());
            }
            if (TameableUtils.hasEnchant(event.getEntityLiving(), DIEnchantmentRegistry.VOID_CLOUD) && !event.getEntityLiving().isInWaterOrBubble() && event.getEntityLiving().fallDistance > 3.0F && !event.getEntityLiving().isOnGround()) {
                Entity owner = TameableUtils.getOwnerOf(event.getEntityLiving());
                boolean shouldMoveToOwnerXZ = owner != null && Math.abs(owner.getY() - event.getEntityLiving().getY()) < 1;
                double targetX = shouldMoveToOwnerXZ ? owner.getX() : event.getEntityLiving().getX();
                double targetY = Math.max(event.getEntityLiving().level.getMinBuildHeight() + 0.5F, owner == null ? 64F : owner.getY() < event.getEntityLiving().getY() ? owner.getY() + 0.6F : owner.getY(1.0F) + event.getEntityLiving().getBbHeight());
                if(owner != null && owner.getRootVehicle() == event.getEntityLiving()){
                    targetY = Math.min(event.getEntityLiving().level.getMinBuildHeight() + 0.5F, event.getEntityLiving().getY() - 0.5F);
                }
                double targetZ = shouldMoveToOwnerXZ ? owner.getZ() : event.getEntityLiving().getZ();
                if (event.getEntityLiving().verticalCollision) {
                    event.getEntityLiving().setOnGround(true);
                    targetX += (event.getEntityLiving().getRandom().nextFloat() - 0.5F) * 4;
                    targetZ += (event.getEntityLiving().getRandom().nextFloat() - 0.5F) * 4;
                }
                Vec3 move = new Vec3(targetX - event.getEntityLiving().getX(), targetY - event.getEntityLiving().getY(), targetZ - event.getEntityLiving().getZ());
                event.getEntityLiving().setDeltaMovement(event.getEntityLiving().getDeltaMovement().add(move.normalize().scale(0.15D)).multiply(0.5F, 0.5F, 0.5F));
                if (event.getEntityLiving().level instanceof ServerLevel) {
                    TameableUtils.setFallDistance(event.getEntityLiving(), event.getEntityLiving().fallDistance);
                    ((ServerLevel) event.getEntityLiving().level).sendParticles(ParticleTypes.REVERSE_PORTAL, event.getEntityLiving().getRandomX(1.5F), event.getEntityLiving().getY() - event.getEntityLiving().getRandom().nextFloat(), event.getEntityLiving().getRandomZ(1.5F), 0, 0, -0.2F, 0, 1.0D);
                }
            }
            if (TameableUtils.isZombiePet(event.getEntityLiving()) && !event.getEntityLiving().level.isClientSide && event.getEntityLiving() instanceof Mob mob) {
                if(mob.getTarget() instanceof Player && ((Player) mob.getTarget()).isCreative()){
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
        }
        if (frozenTime > 0) {
            TameableUtils.setFrozenTimeTag(event.getEntityLiving(), frozenTime - 1);
            AttributeInstance instance = event.getEntityLiving().getAttribute(Attributes.MOVEMENT_SPEED);
            if (instance != null) {
                float f = -0.1F * event.getEntityLiving().getPercentFrozen();
                if (frozenTime > 1) {
                    AttributeModifier fangModifier = new AttributeModifier(FROST_FANG_SLOW, "Frost fang slow", f, AttributeModifier.Operation.ADDITION);
                    if (!instance.hasModifier(fangModifier)) {
                        instance.addTransientModifier(fangModifier);
                    }
                } else {
                    instance.removeModifier(FROST_FANG_SLOW);
                }
            }
            for (int i = 0; i < 1 + event.getEntityLiving().getRandom().nextInt(2); i++) {
                event.getEntityLiving().level.addParticle(ParticleTypes.SNOWFLAKE, event.getEntityLiving().getRandomX(0.7F), event.getEntityLiving().getRandomY(), event.getEntityLiving().getRandomZ(0.7F), 0.0F, 0.0F, 0.0F);
            }
        }
    }

    @SubscribeEvent
    public void onLivingHurt(LivingAttackEvent event) {
        if (TameableUtils.isTamed(event.getEntityLiving()) && event.getSource() != DIDamageTypes.SIPHON_DAMAGE) {
            boolean flag = false;
            if (TameableUtils.hasEnchant(event.getEntityLiving(), DIEnchantmentRegistry.IMMUNITY_FRAME)) {
                int level = TameableUtils.getEnchantLevel(event.getEntityLiving(), DIEnchantmentRegistry.IMMUNITY_FRAME);
                if (TameableUtils.getImmuneTime(event.getEntityLiving()) <= 0) {
                    TameableUtils.setImmuneTime(event.getEntityLiving(), 20 + level * 20);
                } else {
                    flag = true;
                    event.setCanceled(true);
                }
            }
            if ((event.getSource() == DamageSource.DROWN || event.getSource() == DamageSource.DRY_OUT) && TameableUtils.hasEnchant(event.getEntityLiving(), DIEnchantmentRegistry.AMPHIBIOUS)) {
                event.setCanceled(true);
                flag = true;
            }
            if (!flag && (event.getSource().isFall() || event.getSource() == DamageSource.OUT_OF_WORLD) && TameableUtils.hasEnchant(event.getEntityLiving(), DIEnchantmentRegistry.VOID_CLOUD)) {
                event.setCanceled(true);
                flag = true;
            }
            if (!flag && TameableUtils.hasEnchant(event.getEntityLiving(), DIEnchantmentRegistry.HEALTH_SIPHON)) {
                Entity owner = TameableUtils.getOwnerOf(event.getEntityLiving());
                if (owner != null && owner.isAlive() && owner.distanceTo(event.getEntityLiving()) < 100 && owner != event.getEntityLiving()) {
                    owner.hurt(event.getSource(), event.getAmount());
                    event.setCanceled(true);
                    flag = true;
                    event.getEntityLiving().hurt(DIDamageTypes.SIPHON_DAMAGE, 0.0F);
                }
            }
            if (!flag && TameableUtils.hasEnchant(event.getEntityLiving(), DIEnchantmentRegistry.TOTAL_RECALL) && event.getEntityLiving().getHealth() - event.getAmount() <= 2.0D && !TameableUtils.isZombiePet(event.getEntityLiving())) {
                UUID owner = TameableUtils.getOwnerUUIDOf(event.getEntityLiving());
                if (owner != null) {
                    if (event.getEntityLiving() instanceof Mob mob) {
                        mob.playAmbientSound();
                    }
                    event.getEntityLiving().playSound(SoundEvents.ENDER_CHEST_CLOSE, 1.0F, 1.5F);
                    RecallBallEntity recallBall = DIEntityRegistry.RECALL_BALL.get().create(event.getEntityLiving().level);
                    recallBall.setOwnerUUID(owner);
                    CompoundTag tag = new CompoundTag();
                    event.getEntityLiving().addAdditionalSaveData(tag);
                    recallBall.setContainedData(tag);
                    recallBall.setContainedEntityType(event.getEntityLiving().getType().getRegistryName().toString());
                    recallBall.copyPosition(event.getEntityLiving());
                    recallBall.setYRot(event.getEntityLiving().getYRot());
                    recallBall.setInvulnerable(true);
                    if (event.getEntityLiving().level.addFreshEntity(recallBall)) {
                        event.getEntityLiving().discard();
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
                ChainLightningEntity lightning = DIEntityRegistry.CHAIN_LIGHTNING.get().create(event.getEntityLiving().level);
                lightning.setCreatorEntityID(attacker.getId());
                lightning.setFromEntityID(attacker.getId());
                lightning.setToEntityID(event.getEntityLiving().getId());
                lightning.copyPosition(event.getEntityLiving());
                lightning.setChainsLeft(3 + lightningLevel * 3);
                event.getEntityLiving().level.addFreshEntity(lightning);
                event.getEntityLiving().playSound(DISoundRegistry.CHAIN_LIGHTNING, 1F, 1F);
            }
            if (TameableUtils.hasEnchant(attacker, DIEnchantmentRegistry.FROST_FANG)) {
                event.getEntityLiving().setTicksFrozen(event.getEntityLiving().getTicksRequiredToFreeze() + 200);
                Vec3 vec3 = event.getEntityLiving().getEyePosition().subtract(attacker.getEyePosition()).normalize().scale(attacker.getBbWidth() + 0.5F);
                Vec3 vec32 = attacker.getEyePosition().add(vec3);
                for (int i = 0; i < 3 + attacker.getRandom().nextInt(3); i++) {
                    float f1 = 0.2F * (attacker.getRandom().nextFloat() - 1.0F);
                    float f2 = 0.2F * (attacker.getRandom().nextFloat() - 1.0F);
                    float f3 = 0.2F * (attacker.getRandom().nextFloat() - 1.0F);
                    attacker.level.addParticle(ParticleTypes.SNOWFLAKE, vec32.x + f1, vec32.y + f2, vec32.z + f3, 0.0F, 0.0F, 0.0F);
                }
                TameableUtils.setFrozenTimeTag(event.getEntityLiving(), 60);
            }
            if (bubblingLevel > 0) {
                if (!(event.getEntityLiving().getRootVehicle() instanceof GiantBubbleEntity) && (event.getEntityLiving().isOnGround() || event.getEntityLiving().isInWaterOrBubble() || event.getEntityLiving().isInLava())) {
                    GiantBubbleEntity bubble = DIEntityRegistry.GIANT_BUBBLE.get().create(event.getEntityLiving().level);
                    bubble.copyPosition(event.getEntityLiving());
                    event.getEntityLiving().startRiding(bubble, true);
                    bubble.setpopsIn(bubblingLevel * 40 + 40);
                    event.getEntityLiving().level.addFreshEntity(bubble);
                    event.getEntityLiving().playSound(DISoundRegistry.GIANT_BUBBLE_INFLATE, 1F, 1F);

                }
            }
            if (vampireLevel > 0) {
                if (attacker.getHealth() < attacker.getMaxHealth()) {
                    float f = Mth.clamp(event.getAmount() * vampireLevel * 0.5F, 1F, 10F);
                    attacker.heal(f);
                    if (event.getEntityLiving().level instanceof ServerLevel) {
                        for (int i = 0; i < 5 + event.getEntityLiving().getRandom().nextInt(3); i++) {
                            double f1 = event.getEntityLiving().getRandomX(0.7F);
                            double f2 = event.getEntityLiving().getY(0.4F + event.getEntityLiving().getRandom().nextFloat() * 0.2F);
                            double f3 = event.getEntityLiving().getRandomZ(0.7F);
                            Vec3 motion = attacker.getEyePosition().subtract(f1, f2, f3).normalize().scale(0.2F);
                            ((ServerLevel) event.getEntityLiving().level).sendParticles(DIParticleRegistry.VAMPIRE, f1, f2, f3, 1, motion.x, motion.y, motion.z, 0.2F);
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onLivingDie(LivingDeathEvent event) {
        if (TameableUtils.isTamed(event.getEntityLiving()) && !TameableUtils.isZombiePet(event.getEntityLiving())) {
            BlockPos bedPos = TameableUtils.getPetBedPos(event.getEntityLiving());
            if (bedPos != null) {
                CompoundTag data = new CompoundTag();
                event.getEntityLiving().addAdditionalSaveData(data);
                String saveName = event.getEntityLiving().hasCustomName() ? event.getEntityLiving().getCustomName().getString() : "";
                RespawnRequest request = new RespawnRequest(event.getEntityLiving().getType().getRegistryName().toString(), TameableUtils.getPetBedDimension(event.getEntityLiving()), data, bedPos, event.getEntityLiving().level.dayTime(), saveName);
                DIWorldData worldData = DIWorldData.get(event.getEntityLiving().level);
                if (worldData != null) {
                    worldData.addRespawnRequest(request);
                }
            }
            if (event.getEntityLiving() instanceof Mob mob && event.getEntityLiving().level.getDifficulty() != Difficulty.PEACEFUL && TameableUtils.hasEnchant(mob, DIEnchantmentRegistry.UNDEAD_CURSE)) {
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
                if(zombieCopy instanceof CommandableMob commandableMob){
                    commandableMob.setCommand(0);
                }
                zombieCopy.copyPosition(mob);
                zombieCopy.setTarget(owner instanceof Player && !((Player) owner).isCreative() ? (Player) owner : mob.level.getNearestPlayer(ZOMBIE_TARGET, mob));
                mob.level.addFreshEntity(zombieCopy);
                zombieCopy.setHealth(zombieCopy.getMaxHealth());
                TameableUtils.setZombiePet(zombieCopy, true);
            }
        }else if(TameableUtils.isTamed(event.getEntityLiving()) && !(event.getEntityLiving() instanceof TamableAnimal)){
            Entity owner = TameableUtils.getOwnerOf(event.getEntityLiving());
            if (!event.getEntityLiving().level.isClientSide && event.getEntityLiving().level.getGameRules().getBoolean(GameRules.RULE_SHOWDEATHMESSAGES) && owner instanceof ServerPlayer) {
                owner.sendMessage(event.getEntityLiving().getCombatTracker().getDeathMessage(), Util.NIL_UUID);
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
        if(TameableUtils.couldBeTamed(event.getTarget()) && TameableUtils.isZombiePet((LivingEntity) event.getTarget())){
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            return;
        }
        if(event.getTarget() instanceof Rabbit rabbit && DomesticationMod.CONFIG.tameableRabbit.get()){
            ItemStack stack = event.getItemStack();
            if(stack.getItem() == Items.CARROT || stack.getItem() == Items.GOLDEN_CARROT){
                if (TameableUtils.isTamed(rabbit) && rabbit.getHealth() < rabbit.getMaxHealth()) {
                    rabbit.heal(3);
                    if(!event.getPlayer().isCreative()){
                        stack.shrink(1);
                    }
                    event.setCanceled(true);
                    event.setCancellationResult(InteractionResult.SUCCESS);
                    return;
                }
                if(!TameableUtils.isTamed(rabbit) && !rabbit.level.isClientSide){
                    if(!event.getPlayer().isCreative()){
                        stack.shrink(1);
                    }
                    if(rabbit.getRandom().nextInt(4) == 0){
                        for(int i = 0; i < 3; ++i) {
                            double d0 = rabbit.getRandom().nextGaussian() * 0.02D;
                            double d1 = rabbit.getRandom().nextGaussian() * 0.02D;
                            double d2 = rabbit.getRandom().nextGaussian() * 0.02D;
                            ((ServerLevel)rabbit.getLevel()).sendParticles(ParticleTypes.HEART, rabbit.getRandomX(1.0D), rabbit.getRandomY() + 0.5D, rabbit.getRandomZ(1.0D), 3, d0, d1, d2, 0.02F);
                        }
                        ((ModifedToBeTameable)rabbit).setTame(true);
                        ((ModifedToBeTameable)rabbit).setTameOwnerUUID(event.getPlayer().getUUID());
                        ((CommandableMob)rabbit).setCommand(1);
                    }else{
                        for(int i = 0; i < 3; ++i) {
                            double d0 = rabbit.getRandom().nextGaussian() * 0.02D;
                            double d1 = rabbit.getRandom().nextGaussian() * 0.02D;
                            double d2 = rabbit.getRandom().nextGaussian() * 0.02D;
                            ((ServerLevel)rabbit.getLevel()).sendParticles(ParticleTypes.SMOKE, rabbit.getRandomX(1.0D), rabbit.getRandomY() + 0.5D, rabbit.getRandomZ(1.0D), 3, d0, d1, d2, 0.02F);
                        }
                    }
                    event.setCanceled(true);
                    event.setCancellationResult(InteractionResult.SUCCESS);
                    return;
                }
            }
            if (TameableUtils.isTamed(rabbit) && TameableUtils.isPetOf(event.getPlayer(), rabbit)){
                ((CommandableMob)rabbit).playerSetCommand(event.getPlayer(), rabbit);
            }
        }
        if (event.getTarget() instanceof LivingEntity entity && TameableUtils.isPetOf(event.getPlayer(), entity) && event.getItemStack().is(DIItemRegistry.COLLAR_TAG.get()) && DomesticationMod.CONFIG.collarTag.get()) {
            ItemStack stack = event.getItemStack();
            if (!event.getPlayer().level.isClientSide && entity.isAlive()) {
                Map<Enchantment, Integer> itemEnchantments = EnchantmentHelper.deserializeEnchantments(stack.getEnchantmentTags());
                Map<ResourceLocation, Integer> entityEnchantments = TameableUtils.getEnchants(entity);
                if(stack.hasCustomHoverName() && entity.hasCustomName() && stack.getHoverName().equals(entity.getCustomName())){
                    boolean hasSameEnchants = itemEnchantments.isEmpty();
                    if(entityEnchantments != null){
                        hasSameEnchants = true;
                        for(Map.Entry<Enchantment, Integer> itemEntry : itemEnchantments.entrySet()){
                            ResourceLocation name = itemEntry.getKey().getRegistryName();
                            if(entityEnchantments.get(name) == null || !entityEnchantments.get(name).equals(itemEntry.getValue())){
                                hasSameEnchants = false;
                            }
                        }
                    }
                    if(hasSameEnchants){
                        event.setCanceled(true);
                        event.setCancellationResult(InteractionResult.FAIL);
                        return;
                    }
                }
                if(stack.hasCustomHoverName()){
                    entity.setCustomName(stack.getHoverName());
                }
                if(!event.getPlayer().isCreative()){
                    stack.shrink(1);
                }
                if(TameableUtils.hasCollar(entity)){
                    ItemStack collarFrom = new ItemStack(DIItemRegistry.COLLAR_TAG.get());
                    if(entityEnchantments != null){
                        collarFrom.getOrCreateTag();
                        if (!collarFrom.getTag().contains("Enchantments", 9)) {
                            collarFrom.getTag().put("Enchantments", new ListTag());
                        }

                        ListTag listtag = collarFrom.getTag().getList("Enchantments", 10);
                        for(Map.Entry<ResourceLocation, Integer> entry : entityEnchantments.entrySet()){
                            listtag.add(EnchantmentHelper.storeEnchantment(entry.getKey(), entry.getValue()));
                        }
                    }else{
                        collarFrom.setTag(null);
                    }
                    entity.spawnAtLocation(collarFrom);
                }
                TameableUtils.clearEnchants(entity);
                for (Map.Entry<Enchantment, Integer> entry : itemEnchantments.entrySet()){
                    TameableUtils.addEnchant(entity, new EnchantmentInstance(entry.getKey(), entry.getValue()));
                }
                TameableUtils.setHasCollar(entity, true);
            }
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
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

    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event){
        if(event.getState().getBlock() instanceof PetBedBlock){
            if(event.getWorld().getBlockEntity(event.getPos()) instanceof PetBedBlockEntity entity1){
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
        }catch (Exception e){
            DomesticationMod.LOGGER.warn("could not add ai tasks to ravager");
        }
    }
}
