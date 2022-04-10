package com.github.alexthe668.domesticationinnovation.server.block;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public class DrumBlockEntity extends BlockEntity {

    private UUID placerUUID;

    public DrumBlockEntity(BlockPos pos, BlockState state) {
        super(DITileEntityRegistry.DRUM.get(), pos, state);
    }

    public UUID getPlacerUUID() {
        return placerUUID;
    }

    public void setPlacerUUID(UUID placerUUID) {
        this.placerUUID = placerUUID;
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        if (compound.contains("PlacerUUID")) {
            this.placerUUID = compound.getUUID("PlacerUUID");
        }
    }

    @Override
    protected void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
        if (this.placerUUID != null) {
            compound.putUUID("PlacerUUID", placerUUID);
        }
    }
}
