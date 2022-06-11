package com.github.alexthe668.domesticationinnovation.server.block;

import com.github.alexthe666.citadel.server.entity.IComandableMob;
import com.github.alexthe668.domesticationinnovation.server.entity.TameableUtils;
import com.github.alexthe668.domesticationinnovation.server.misc.DISoundRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.UUID;
import java.util.function.Predicate;

public class DrumBlock extends BaseEntityBlock {

    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final IntegerProperty COMMAND = IntegerProperty.create("command", 0, 2);
    private static Random random = new Random();

    public DrumBlock() {
        super(Properties.of(Material.WOOD).sound(SoundType.WOOD).strength(1F).noOcclusion());
        this.registerDefaultState(this.stateDefinition.any().setValue(COMMAND, 0).setValue(POWERED, Boolean.valueOf(false)));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> context) {
        context.add(COMMAND, POWERED);
    }

    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        if(player.isShiftKeyDown()){
            return InteractionResult.PASS;
        }else{
            int currentCommand = state.getValue(COMMAND);
            level.setBlockAndUpdate(pos, state.cycle(COMMAND));
            int count = issueCommand(level, pos, currentCommand, player.getUUID());
            if(count > 0){
                player.displayClientMessage(Component.translatable("message.domesticationinnovation.drum_command_" + currentCommand, count), true);
            }
            player.playSound(DISoundRegistry.DRUM.get(), 3, 0.3F + 0.4F * random.nextFloat());
            level.gameEvent(player, GameEvent.NOTE_BLOCK_PLAY, pos);
            return InteractionResult.SUCCESS;
        }
    }

    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @javax.annotation.Nullable LivingEntity livingEntity, ItemStack stack) {
        BlockEntity blockentity = level.getBlockEntity(pos);
        if (livingEntity != null && blockentity instanceof DrumBlockEntity drum) {
            drum.setPlacerUUID(livingEntity.getUUID());
        }
    }

    public int issueCommand(Level level, BlockPos pos, int command, UUID issuer){
        int count = 0;
        if(issuer != null){
            Predicate<Entity> tames = (animal) -> TameableUtils.isTamed((LivingEntity) animal) && TameableUtils.getOwnerUUIDOf(animal) != null && TameableUtils.getOwnerUUIDOf(animal).equals(issuer);
            AABB area = new AABB(pos.offset(-32, -32, -32), pos.offset(32, 32, 32));
            for(Animal animal : level.getEntitiesOfClass(Animal.class, area, EntitySelector.NO_SPECTATORS.and(tames))){
                if(animal instanceof IComandableMob){
                    ((IComandableMob) animal).setCommand(command);
                    count++;
                }
                if(animal instanceof TamableAnimal){
                    if(command != 0){
                        ((TamableAnimal)animal).setOrderedToSit(command == 1);
                        ((TamableAnimal)animal).setInSittingPose(command == 1);
                        if(!(animal instanceof IComandableMob)){
                            count++;
                        }
                    }
                }
                animal.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0));
            }
        }
        return count;
    }

    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos pos2, boolean b) {
        boolean flag = level.hasNeighborSignal(pos);
        if (flag != state.getValue(POWERED)) {
            if (flag) {
                UUID uuid = null;
                if(level.getBlockEntity(pos) instanceof DrumBlockEntity drum){
                    uuid = drum.getPlacerUUID();
                }
                this.issueCommand(level, pos, state.getValue(COMMAND), uuid);
                level.playSound(null, pos, DISoundRegistry.DRUM.get(), SoundSource.BLOCKS, 3, 0.3F + 0.4F * random.nextFloat());
                level.gameEvent(null, GameEvent.NOTE_BLOCK_PLAY, pos);
            }
            level.setBlock(pos, state.setValue(POWERED, Boolean.valueOf(flag)), 3);
        }
    }

    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float f) {
        entity.playSound(DISoundRegistry.DRUM.get(), 3, 0.6F + 0.4F * random.nextFloat());
        super.fallOn(level, state, pos, entity, f);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DrumBlockEntity(pos, state);
    }
}

