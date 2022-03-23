package com.github.alexthe668.domesticationinnovation.server.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.ArrayList;
import java.util.List;

public class DIWorldData extends SavedData {

    private static final String IDENTIFIER = "domesticationinnovation_world_data";
    private final List<RespawnRequest> respawnRequestList = new ArrayList<>();

    private DIWorldData() {
        super();
    }

    public static DIWorldData get(Level world) {
        if (world instanceof ServerLevel) {
            ServerLevel overworld = world.getServer().getLevel(Level.OVERWORLD);
            DimensionDataStorage storage = overworld.getDataStorage();
            DIWorldData data = storage.computeIfAbsent(DIWorldData::load, DIWorldData::new, IDENTIFIER);
            if (data != null) {
                data.setDirty();
            }
            return data;
        }
        return null;
    }

    public static DIWorldData load(CompoundTag nbt) {
        DIWorldData data = new DIWorldData();
        if (nbt.contains("RespawnList")) {
            ListTag listtag = nbt.getList("RespawnList", 10);
            for (int i = 0; i < listtag.size(); ++i) {
                CompoundTag innerTag = listtag.getCompound(i);
                data.respawnRequestList.add(new RespawnRequest(innerTag.getString("EntityType"), innerTag.getString("DimensionIn"), innerTag.getCompound("EntityData"),
                        new BlockPos(innerTag.getInt("X"), innerTag.getInt("Y"), innerTag.getInt("Z")), innerTag.getLong("Timestamp"), innerTag.getString("EntityNametag")));
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag compound) {
        if (!this.respawnRequestList.isEmpty()) {
            ListTag listTag = new ListTag();
            for(RespawnRequest request : respawnRequestList){
                CompoundTag tag = new CompoundTag();
                tag.putString("EntityType", request.getEntityTypeLoc());
                tag.putString("DimensionIn", request.getDimension());
                tag.put("EntityData", request.getEntityData());
                tag.putInt("X", request.getBedPosition().getX());
                tag.putInt("Y", request.getBedPosition().getY());
                tag.putInt("Z", request.getBedPosition().getZ());
                tag.putLong("Timestamp", request.getTimestamp());
                tag.putString("EntityNametag", request.getNametag());
                listTag.add(tag);
            }
            compound.put("RespawnList", listTag);
        }
        return compound;
    }

    public void addRespawnRequest(RespawnRequest request){
        this.respawnRequestList.add(request);
    }

    public void removeRespawnRequest(RespawnRequest request){
        this.respawnRequestList.remove(request);
    }

    public List<RespawnRequest> getRequestsFor(Level level, BlockPos pos){
        List<RespawnRequest> list = new ArrayList<>();
        String dimension = level.dimension().toString();
        for(RespawnRequest request : this.respawnRequestList){
            if(dimension.equals(request.getDimension()) && pos.equals(request.getBedPosition())){
                list.add(request);
            }
        }
        return list;
    }
}
