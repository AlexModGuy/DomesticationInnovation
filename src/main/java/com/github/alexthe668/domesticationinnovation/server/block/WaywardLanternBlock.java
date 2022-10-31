package com.github.alexthe668.domesticationinnovation.server.block;

import com.github.alexthe668.domesticationinnovation.server.misc.DIParticleRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class WaywardLanternBlock extends BaseEntityBlock {

    private static final VoxelShape SHAPE = Stream.of(
            Block.box(6.5, 15.5, 6.5, 9.5, 19.5, 9.5),
            Block.box(6, 5, 6, 10, 6, 10),
            Block.box(6, 14.5, 6, 10, 15.5, 10),
            Block.box(2, 13.5, 2, 14, 14.5, 14),
            Block.box(4, 7, 4, 12, 14, 12),
            Block.box(7, 0, 7, 9, 10, 9)
    ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public WaywardLanternBlock() {
        super(BlockBehaviour.Properties.of(Material.HEAVY_METAL).sound(SoundType.LANTERN).strength(0.9F).lightLevel((i) -> 13).noOcclusion());
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, Boolean.valueOf(false)));
    }

    public VoxelShape getShape(BlockState p_49038_, BlockGetter p_49039_, BlockPos p_49040_, CollisionContext p_49041_) {
        return SHAPE;
    }

    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(4) == 0) {
            level.addParticle(DIParticleRegistry.LANTERN_BUGS.get(), (double) pos.getX() + 0.5D + random.nextGaussian() * 1, (double) pos.getY() + 0.5D + random.nextGaussian() * 1, (double) pos.getZ() + 0.5D + random.nextGaussian() * 1, (double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D);
        }
        if (random.nextInt(2) == 0) {
            level.addParticle(ParticleTypes.FLAME, (double) pos.getX() + 0.5D, (double) pos.getY() + 0.65D, (double) pos.getZ() + 0.5D, 0, 0, 0);
        }
    }

    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidstate = context.getLevel().getFluidState(context.getClickedPos());
        boolean flag = fluidstate.getType() == Fluids.WATER;
        return this.defaultBlockState().setValue(WATERLOGGED, Boolean.valueOf(flag));
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

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> context) {
        context.add(WATERLOGGED);
    }
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WaywardLanternBlockEntity(pos, state);
    }

    @javax.annotation.Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level p_152180_, BlockState p_152181_, BlockEntityType<T> p_152182_) {
        return p_152180_.isClientSide ? null : createTickerHelper(p_152182_, DITileEntityRegistry.WAYWARD_LANTERN.get(), WaywardLanternBlockEntity::tick);
    }


}
