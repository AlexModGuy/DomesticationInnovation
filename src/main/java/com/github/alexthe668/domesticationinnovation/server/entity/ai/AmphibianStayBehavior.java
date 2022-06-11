package com.github.alexthe668.domesticationinnovation.server.entity.ai;

import com.github.alexthe668.domesticationinnovation.server.entity.ModifedToBeTameable;
import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.axolotl.Axolotl;

public class AmphibianStayBehavior<T extends Animal> extends Behavior<T> {

    public AmphibianStayBehavior() {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED), 400);
    }

    protected boolean checkExtraStartConditions(ServerLevel level, T axolotl) {
        return ((ModifedToBeTameable)axolotl).isStayingStill();
    }


    protected boolean canStillUse(ServerLevel level, T axolotl, long gameTime) {
        return ((ModifedToBeTameable)axolotl).isStayingStill();

    }

    protected void stop(ServerLevel p_23492_, T axolotl, long gameTime) {
    }

    protected void tick(ServerLevel p_23503_, T axolotl, long gameTime) {
        axolotl.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        axolotl.getNavigation().stop();
    }
}
