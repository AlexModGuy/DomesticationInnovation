package com.github.alexthe668.domesticationinnovation.server.block;

import com.github.alexthe668.domesticationinnovation.DomesticationMod;
import com.github.alexthe668.domesticationinnovation.server.entity.CommandableMob;
import com.github.alexthe668.domesticationinnovation.server.entity.TameableUtils;
import com.github.alexthe668.domesticationinnovation.server.misc.DIWorldData;
import com.github.alexthe668.domesticationinnovation.server.misc.RespawnRequest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

public class PetBedBlockEntity extends BlockEntity {

    public PetBedBlockEntity(BlockPos pos, BlockState state) {
        super(DITileEntityRegistry.PET_BED.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, PetBedBlockEntity blockEntity) {
        long time = level.dayTime() % 24000L;
        if(time == 1){
            DIWorldData data = DIWorldData.get(level);
            if(data != null){
               List<RespawnRequest> requestList = data.getRequestsFor(level, pos);
               for(RespawnRequest request : requestList){
                    if(addAndRemoveEntity(level, pos, state.getValue(PetBedBlock.FACING), request)){
                        data.removeRespawnRequest(request);
                    }
               }
            }
        }
    }

    public void removeAllRequestsFor(@Nullable Player message){
        DIWorldData data = DIWorldData.get(level);
        if(data != null){
            List<RespawnRequest> requestList = data.getRequestsFor(level, this.getBlockPos());
            for(RespawnRequest request : requestList){
                data.removeRespawnRequest(request);
                if(message != null){
                    message.displayClientMessage(new TranslatableComponent("message.domesticationinnovation.goodbye", request.getNametag()), false);
                }
            }
        }
    }

    private static boolean addAndRemoveEntity(Level level, BlockPos pos, Direction dir, RespawnRequest request) {
        EntityType type = request.getEntityType();
        if(type != null && DomesticationMod.CONFIG.petBedRespawns.get()){
            Entity entity = type.create(level);
            if(entity instanceof LivingEntity living){
                living.readAdditionalSaveData(request.getEntityData());
                living.setPos(Vec3.upFromBottomCenterOf(pos, 0.8F));
                living.setHealth(living.getMaxHealth());
                if(!request.getNametag().isEmpty()){
                    living.setCustomName(new TextComponent(request.getNametag()));
                }
                switch (dir){
                    case NORTH:
                        living.setYRot(180);
                        break;
                    case EAST:
                        living.setYRot(-90);
                        break;
                    case SOUTH:
                        living.setYRot(0);
                        break;
                    case WEST:
                        living.setYRot(90);
                        break;
                }
                if(living instanceof CommandableMob){
                    ((CommandableMob) living).setCommand(1);
                }
                if(living instanceof TamableAnimal){
                    ((TamableAnimal)living).setOrderedToSit(true);
                }
                level.addFreshEntity(living);
                Entity owner = TameableUtils.getOwnerOf(entity);
                if(owner instanceof Player){
                    ((Player)owner).displayClientMessage(new TranslatableComponent("message.domesticationinnovation.respawn", entity.getName()), false);
                }
                return true;
            }
        }
        return false;
    }

    public void resetBedsForNearbyPets() {
        Predicate<Entity> pet = (animal) -> TameableUtils.isTamed(animal) && TameableUtils.getPetBedPos((LivingEntity)animal) != null && TameableUtils.getPetBedPos((LivingEntity)animal).equals(this.getBlockPos());
        List<LivingEntity> list = level.getEntitiesOfClass(LivingEntity.class, new AABB(this.getBlockPos().offset(-10, -5, -10), this.getBlockPos().offset(10, 5, 10)), EntitySelector.NO_SPECTATORS.and(pet));
        for (LivingEntity entity : list){
            Entity owner = TameableUtils.getOwnerOf(entity);
            if(owner instanceof Player){
                ((Player)owner).displayClientMessage(new TranslatableComponent("message.domesticationinnovation.remove_respawn", entity.getName()), false);
                TameableUtils.removePetBedPos(entity);
            }
        }
    }
}
