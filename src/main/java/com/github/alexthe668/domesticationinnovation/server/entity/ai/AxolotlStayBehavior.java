package com.github.alexthe668.domesticationinnovation.server.entity.ai;

import com.github.alexthe668.domesticationinnovation.server.entity.ModifedToBeTameable;
import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.axolotl.Axolotl;

public class AxolotlStayBehavior  extends Behavior<Axolotl> {

    public AxolotlStayBehavior() {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED), 400);
    }

    protected boolean checkExtraStartConditions(ServerLevel level, Axolotl axolotl) {
        return ((ModifedToBeTameable)axolotl).isStayingStill();
    }


    protected boolean canStillUse(ServerLevel level, Axolotl axolotl, long gameTime) {
        return ((ModifedToBeTameable)axolotl).isStayingStill();

    }

    protected void stop(ServerLevel p_23492_, Axolotl axolotl, long gameTime) {
    }

    protected void tick(ServerLevel p_23503_, Axolotl axolotl, long gameTime) {
        axolotl.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        axolotl.getNavigation().stop();
    }
}
