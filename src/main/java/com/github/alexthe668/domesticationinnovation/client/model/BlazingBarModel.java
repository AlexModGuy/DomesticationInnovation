package com.github.alexthe668.domesticationinnovation.client.model;

import com.github.alexthe666.citadel.client.model.AdvancedEntityModel;
import com.github.alexthe666.citadel.client.model.AdvancedModelBox;
import com.github.alexthe666.citadel.client.model.basic.BasicModelPart;
import com.google.common.collect.ImmutableList;
import net.minecraft.world.entity.LivingEntity;

public class BlazingBarModel extends AdvancedEntityModel<LivingEntity> {
    private final AdvancedModelBox bar;

    public BlazingBarModel() {
        texWidth = 64;
        texHeight = 32;
        bar = new AdvancedModelBox(this);
        bar.setRotationPoint(0.0F, 14.0F, 0.0F);
        bar.setTextureOffset(0, 16).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 8.0F, 2.0F, false);
        this.updateDefaultPose();
    }

    @Override
    public Iterable<AdvancedModelBox> getAllParts() {
        return ImmutableList.of(bar);
    }

    @Override
    public Iterable<BasicModelPart> parts() {
        return ImmutableList.of(bar);
    }

    @Override
    public void setupAnim(LivingEntity recallBallEntity, float limbSwing, float limbSwingAmount, float age, float yaw, float pitch) {
        this.resetToDefaultPose();
    }

    public void animateBar(float rotY) {
        this.resetToDefaultPose();
        this.bar.rotateAngleY -= (float)Math.toRadians(rotY);
    }
}