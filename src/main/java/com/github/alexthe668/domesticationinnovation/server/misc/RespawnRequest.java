package com.github.alexthe668.domesticationinnovation.server.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.logging.Level;

public class RespawnRequest {
    private String entityType;
    private String dimension;
    private CompoundTag entityData;
    private BlockPos bedPosition;
    private long timestamp;
    private String nametag;

    public RespawnRequest(String entityType, String dimension, CompoundTag entityData, BlockPos bedPosition, long timestamp, String nametag) {
        this.entityType = entityType;
        this.dimension = dimension;
        this.entityData = entityData;
        this.bedPosition = bedPosition;
        this.timestamp = timestamp;
        this.nametag = nametag;
    }

    public String getEntityTypeLoc() {
        return this.entityType;
    }

    public EntityType getEntityType() {
        return ForgeRegistries.ENTITIES.getValue(new ResourceLocation(this.entityType));
    }

    public String getDimension() {
        return dimension;
    }

    public CompoundTag getEntityData() {
        return entityData;
    }

    public BlockPos getBedPosition() {
        return bedPosition;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public static String processDimension(ResourceKey<Level> key){
        return key.toString();
    }

    public String getNametag() {
        return this.nametag;
    }

}
