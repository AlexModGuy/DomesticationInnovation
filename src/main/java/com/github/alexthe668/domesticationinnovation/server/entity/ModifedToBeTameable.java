package com.github.alexthe668.domesticationinnovation.server.entity;

import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;
import java.util.UUID;

public interface ModifedToBeTameable {
    boolean isTame();
    void setTame(boolean value);
    @Nullable
    UUID getTameOwnerUUID();
    void setTameOwnerUUID(@Nullable UUID uuid);
    @Nullable
    LivingEntity getTameOwner();

    boolean isStayingStill();
    boolean isFollowingOwner();

    boolean isValidAttackTarget(LivingEntity target);
}
