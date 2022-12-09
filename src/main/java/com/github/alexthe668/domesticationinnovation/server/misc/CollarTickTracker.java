package com.github.alexthe668.domesticationinnovation.server.misc;

import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class CollarTickTracker {

    private final Map<UUID, Integer> blockedCollarTagUpdates = new HashMap<>();


    public void addBlockedEntityTick(UUID uuid, int duration){
        this.blockedCollarTagUpdates.put(uuid, duration);
    }

    public boolean isEntityBlocked(Entity entity){
        return this.blockedCollarTagUpdates.getOrDefault(entity.getUUID(), 0) > 0;
    }

    public void tick(){
        if(!blockedCollarTagUpdates.isEmpty()){
            Set<UUID> blockedUUIDs = blockedCollarTagUpdates.keySet();
            for(UUID uuid : blockedUUIDs){
                int set = blockedCollarTagUpdates.get(uuid) - 1;
                if(set < 0){
                    blockedCollarTagUpdates.remove(uuid);
                }else{
                    blockedCollarTagUpdates.put(uuid, set);
                }
            }
        }
    }
}
