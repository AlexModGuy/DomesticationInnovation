package com.github.alexthe668.domesticationinnovation.server.entity.ai;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;

public class AquaticMoveControl extends MoveControl {
    private final Mob entity;

    public AquaticMoveControl(Mob entity) {
        super(entity);
        this.entity = entity;
    }

    public void tick() {
        if (this.operation == Operation.MOVE_TO && !entity.getNavigation().isDone()) {
            double d0 = this.wantedX - this.entity.getX();
            double d1 = this.wantedY - this.entity.getY();
            double d2 = this.wantedZ - this.entity.getZ();
            double d3 = (double) Mth.sqrt((float) (d0 * d0 + d1 * d1 + d2 * d2));
            d1 /= d3;
            float f = (float)(Mth.atan2(d2, d0) * 57.2957763671875D) - 90.0F;
            this.entity.setYRot(this.rotlerp(this.entity.getYRot(), f, this.entity.getMaxHeadYRot()));
            this.entity.yBodyRot = this.entity.getYRot();
            float speed =(float)(this.speedModifier * this.entity.getAttributeValue(Attributes.MOVEMENT_SPEED) * 3.0F);
            this.entity.setSpeed(speed * 0.4F);
            this.entity.setDeltaMovement(this.entity.getDeltaMovement().add(0.0D, (double)this.entity.getSpeed() * d1 * 0.6D, 0.0D));
        } else {
            this.entity.setSpeed(0.0F);
        }
    }
}
