package com.github.alexthe668.domesticationinnovation.server.entity.ai;

import com.github.alexthe668.domesticationinnovation.server.entity.ModifedToBeTameable;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Fox;

import java.util.EnumSet;

public class Sit2Goal extends Goal {
    private final Animal mob;

    public Sit2Goal(Animal animal) {
        this.mob = animal;
        this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
    }

    public boolean canContinueToUse() {
        return ((ModifedToBeTameable)this.mob).isTame() && ((ModifedToBeTameable)this.mob).isStayingStill();
    }

    public boolean canUse() {
        if (!((ModifedToBeTameable)this.mob).isTame()) {
            return false;
        } else if (this.mob.isInWaterOrBubble()) {
            return false;
        } else if (!this.mob.isOnGround()) {
            return false;
        } else {
            return ((ModifedToBeTameable)this.mob).isStayingStill();
        }
    }

    public void start() {
        this.mob.getNavigation().stop();
        if(this.mob instanceof Fox){
            ((Fox) this.mob).setSitting(true);
        }
    }

    public void stop() {
    }
}
