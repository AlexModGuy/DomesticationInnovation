package com.github.alexthe668.domesticationinnovation.client.model;

import com.github.alexthe666.citadel.client.model.AdvancedEntityModel;
import com.github.alexthe666.citadel.client.model.AdvancedModelBox;
import com.github.alexthe666.citadel.client.model.basic.BasicModelPart;
import com.github.alexthe668.domesticationinnovation.server.entity.RecallBallEntity;
import com.google.common.collect.ImmutableList;

public class HighlightedBlockModel extends AdvancedEntityModel<RecallBallEntity> {
    private final AdvancedModelBox box;

    public HighlightedBlockModel() {
        texWidth = 64;
        texHeight = 64;

        box = new AdvancedModelBox(this);
        box.setPos(0.0F, 0.0F, 0.0F);
        box.setTextureOffset(0, 0).addBox(-8, -8, -8, 16.0F, 16.0F, 16.0F, 0.0F, false);

        this.updateDefaultPose();
    }

    @Override
    public Iterable<BasicModelPart> parts() {
        return ImmutableList.of(box);
    }

    @Override
    public void setupAnim(RecallBallEntity recallBallEntity, float limbSwing, float limbSwingAmount, float age, float yaw, float pitch) {
        this.resetToDefaultPose();
    }

    @Override
    public Iterable<AdvancedModelBox> getAllParts() {
        return ImmutableList.of(box);
    }

}
