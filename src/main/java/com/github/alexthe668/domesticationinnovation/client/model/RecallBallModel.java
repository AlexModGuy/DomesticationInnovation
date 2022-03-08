package com.github.alexthe668.domesticationinnovation.client.model;

import com.github.alexthe666.citadel.client.model.AdvancedEntityModel;
import com.github.alexthe666.citadel.client.model.AdvancedModelBox;
import com.github.alexthe666.citadel.client.model.basic.BasicModelPart;
import com.github.alexthe668.domesticationinnovation.server.entity.RecallBallEntity;
import com.google.common.collect.ImmutableList;

public class RecallBallModel extends AdvancedEntityModel<RecallBallEntity> {
    private final AdvancedModelBox bottom;
    private final AdvancedModelBox top;

    public RecallBallModel() {
        texWidth = 64;
        texHeight = 64;

        bottom = new AdvancedModelBox(this);
        bottom.setPos(0.0F, 24.0F, 0.0F);
        bottom.setTextureOffset(0, 0).addBox(-4.5F, -6.0F, -4.5F, 9.0F, 6.0F, 9.0F, 0.0F, false);
        top = new AdvancedModelBox(this);
        top.setPos(0.0F, -6F, 4.5F);
        bottom.addChild(top);
        top.setTextureOffset(0, 15).addBox(-4.5F, -3, -9, 9.0F, 3.0F, 9.0F, 0.0F, false);
        this.updateDefaultPose();
    }

    @Override
    public Iterable<BasicModelPart> parts() {
        return ImmutableList.of(bottom);
    }

    @Override
    public void setupAnim(RecallBallEntity recallBallEntity, float limbSwing, float limbSwingAmount, float age, float yaw, float pitch) {
        this.resetToDefaultPose();
    }

    public void animateBall(RecallBallEntity entity, float partialTick){
        this.bottom.setShouldScaleChildren(true);
        this.resetToDefaultPose();
        float open = entity.getOpenProgress(partialTick);
        this.top.rotateAngleX -= open * Math.PI * 0.75F;
        this.bottom.rotateAngleX += open * Math.PI * 0.25F;
        if(entity.isFinished()){
            this.bottom.setScale(open, open * open, open);
        }else{
            this.bottom.setScale(1, 1, 1);
        }
    }

    @Override
    public Iterable<AdvancedModelBox> getAllParts() {
        return ImmutableList.of(bottom, top);
    }

}
