package com.github.alexthe668.domesticationinnovation.client.model;

import com.github.alexthe666.citadel.client.model.AdvancedEntityModel;
import com.github.alexthe666.citadel.client.model.AdvancedModelBox;
import com.github.alexthe666.citadel.client.model.basic.BasicModelPart;
import com.google.common.collect.ImmutableList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public class ShadowHandModel extends AdvancedEntityModel<LivingEntity> {
    private final AdvancedModelBox palm;
    private final AdvancedModelBox finger1;
    private final AdvancedModelBox finger2;
    private final AdvancedModelBox finger3;
    private final AdvancedModelBox thumb;

    public ShadowHandModel() {
        texWidth = 32;
        texHeight = 32;

        palm = new AdvancedModelBox(this);
        palm.setRotationPoint(0.0F, 19.0F, 0.0F);
        setRotationAngle(palm, -0.5672F, 0.0F, 0.0F);
        palm.setTextureOffset(0, 0).addBox(-3.0F, -2.5F, -2.0F, 6.0F, 3.0F, 5.0F, 0.0F, false);

        finger1 = new AdvancedModelBox(this);
        finger1.setRotationPoint(3.0F, 0.0F, -1.0F);
        palm.addChild(finger1);
        setRotationAngle(finger1, 0.1745F, -0.3491F, 0.0F);
        finger1.setTextureOffset(0, 8).addBox(-2.0F, -2.0F, -6.0F, 2.0F, 2.0F, 7.0F, 0.0F, false);

        finger2 = new AdvancedModelBox(this);
        finger2.setRotationPoint(0.0F, 0.0F, -2.0F);
        palm.addChild(finger2);
        setRotationAngle(finger2, 0.1745F, 0.0F, 0.0F);
        finger2.setTextureOffset(14, 10).addBox(-1.0F, -2.0F, -6.7F, 2.0F, 2.0F, 7.0F, 0.0F, false);

        finger3 = new AdvancedModelBox(this);
        finger3.setRotationPoint(-3.0F, 0.0F, -1.0F);
        palm.addChild(finger3);
        setRotationAngle(finger3, 0.1745F, 0.3491F, 0.0F);
        finger3.setTextureOffset(14, 19).addBox(0.0F, -2.0F, -6.0F, 2.0F, 2.0F, 7.0F, 0.0F, false);

        thumb = new AdvancedModelBox(this);
        thumb.setRotationPoint(3.0F, 0.0F, 3.0F);
        palm.addChild(thumb);
        setRotationAngle(thumb, 0.1745F, -0.8727F, 0.0F);
        thumb.setTextureOffset(1, 17).addBox(-2F, -2.0F, -4.0F, 2.0F, 2.0F, 4.0F, 0.0F, false);
        this.updateDefaultPose();
    }

    @Override
    public Iterable<AdvancedModelBox> getAllParts() {
        return ImmutableList.of(palm, finger1, finger2, finger3, thumb);
    }

    @Override
    public Iterable<BasicModelPart> parts() {
        return ImmutableList.of(palm);
    }

    @Override
    public void setupAnim(LivingEntity recallBallEntity, float limbSwing, float limbSwingAmount, float age, float yaw, float pitch) {
        this.resetToDefaultPose();
    }


    public void setRotationAngle(AdvancedModelBox AdvancedModelBox, float x, float y, float z) {
        AdvancedModelBox.rotateAngleX = x;
        AdvancedModelBox.rotateAngleY = y;
        AdvancedModelBox.rotateAngleZ = z;
    }

    public void animateShadowHand(float punch, int handIndex, int shadowHandCount, float ageInTicks) {
        this.resetToDefaultPose();
        boolean left = handIndex >= shadowHandCount / 2F;
        float leftMod = left ? -1 : 1;
        punch = Mth.clamp(punch, 0, 0.25F) * 4F;
        float still = 1F - punch;
        if (left) {
            thumb.setRotationPoint(-1.5F, 0.0F, 1.5F);
            setRotationAngle(thumb, 0.1745F, 0.8727F, 0.0F);
        } else {
            thumb.setRotationPoint(3.0F, 0.0F, 3.0F);
            setRotationAngle(thumb, 0.1745F, -0.8727F, 0.0F);
        }
        this.walk(palm, leftMod * 0.2F, 0.1F, false, handIndex - 1F, 0F, ageInTicks, still);
        this.walk(finger1, leftMod * 0.2F, 0.2F, false, 1F + handIndex, 0.2F, ageInTicks, still);
        this.walk(finger2, leftMod * 0.2F, 0.2F, false, 3F + handIndex, 0.2F, ageInTicks, still);
        this.walk(finger3, leftMod * 0.2F, 0.2F, false, 5F + handIndex, 0.2F, ageInTicks, still);
        this.swing(thumb, leftMod * 0.2F, 0.2F, false, 5F + handIndex, leftMod * -0.2F, ageInTicks, still);

        progressRotationPrev(finger1, punch, (float) Math.toRadians(90), 0, 0, 1F);
        progressRotationPrev(finger2, punch, (float) Math.toRadians(90), 0, 0, 1F);
        progressRotationPrev(finger3, punch, (float) Math.toRadians(90), 0, 0, 1F);
        progressRotation(palm, punch, 0, 0, 0, 1F);
        progressRotationPrev(thumb, punch, (float) Math.toRadians(90), 0, (float) Math.toRadians(30) * leftMod, 1F);
        progressPositionPrev(finger1, punch, 0, -2, 0.5F, 1F);
        progressPositionPrev(finger2, punch, 0, -2, 0.5F, 1F);
        progressPositionPrev(finger3, punch, 0, -2, 0.5F, 1F);
        progressPositionPrev(thumb, punch, leftMod * 2, -2, -1F, 1F);
        progressPositionPrev(palm, punch, 0, 1, 1, 1F);

    }
}