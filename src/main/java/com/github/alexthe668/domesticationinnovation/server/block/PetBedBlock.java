package com.github.alexthe668.domesticationinnovation.server.block;

import com.github.alexthe668.domesticationinnovation.DomesticationMod;
import com.github.alexthe668.domesticationinnovation.server.entity.TameableUtils;
import com.github.alexthe668.domesticationinnovation.server.misc.DIParticleRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class PetBedBlock extends BaseEntityBlock {

    private static final VoxelShape COLLIDE_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 7.0D, 16.0D);
    private static final VoxelShape SELECT_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    private static Random random = new Random();
    public PetBedBlock(String variantName, DyeColor color) {
        super(BlockBehaviour.Properties.of(Material.WOOL, color).sound(SoundType.WOOD).strength(0.8F).noOcclusion());
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, Boolean.valueOf(false)).setValue(FACING, Direction.NORTH));
    }


    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if(TameableUtils.isTamed(entity) && !level.isClientSide && DomesticationMod.CONFIG.petBedRespawns.get()){
           if((entity.tickCount + entity.getId()) % 10 == 0 && random.nextInt(6) == 0){
               TameableUtils.setPetBedPos((LivingEntity) entity, pos);
               TameableUtils.setPetBedDimension((LivingEntity) entity, level.dimension().toString());
               Vec3 look = new Vec3(0, 0, -entity.getBbWidth()).yRot((float)Math.toRadians(180f - entity.getYHeadRot()));
               Vec3 vec3 = entity.getEyePosition().add(look);
               Vec3 vec32 = look.scale(0.5F);
               for (int i = 0; i < 2 + random.nextInt(2); i++){
                   vec3 = vec3.subtract(vec32);
                   double d1 = (1.0F - random.nextFloat()) * 0.6F;
                   double d2 = (1.0F - random.nextFloat()) * 0.6F;
                   double d3 = (1.0F - random.nextFloat()) * 0.6F;
                   ((ServerLevel)level).sendParticles(DIParticleRegistry.ZZZ.get(), vec3.x + d1, vec3.y + d2, vec3.z + d3, 1,  0, 0,  0, 0.0D);
               }
           }
        }
        super.entityInside(state, level, pos, entity);
    }

    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidstate = context.getLevel().getFluidState(context.getClickedPos());
        boolean flag = fluidstate.getType() == Fluids.WATER;
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(WATERLOGGED, Boolean.valueOf(flag));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> context) {
        context.add(FACING, WATERLOGGED);
    }

    public BlockState rotate(BlockState state, Rotation mirror) {
        return state.setValue(FACING, mirror.rotate(state.getValue(FACING)));
    }

    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    public VoxelShape getShape(BlockState p_49038_, BlockGetter p_49039_, BlockPos p_49040_, CollisionContext p_49041_) {
        return SELECT_SHAPE;
    }

    public VoxelShape getCollisionShape(BlockState p_52357_, BlockGetter p_52358_, BlockPos p_52359_, CollisionContext p_52360_) {
        return COLLIDE_SHAPE;
    }

    public BlockState updateShape(BlockState state, Direction direction, BlockState state2, LevelAccessor accessor, BlockPos pos, BlockPos pos2) {
        if (state.getValue(WATERLOGGED)) {
            accessor.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(accessor));
        }

        return super.updateShape(state, direction, state2, accessor, pos, pos2);
    }

    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PetBedBlockEntity(pos, state);
    }

    @javax.annotation.Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level p_152180_, BlockState p_152181_, BlockEntityType<T> p_152182_) {
        return p_152180_.isClientSide ? null : createTickerHelper(p_152182_, DITileEntityRegistry.PET_BED.get(), PetBedBlockEntity::tick);
    }
}

