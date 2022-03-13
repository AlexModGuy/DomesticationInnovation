package com.github.alexthe668.domesticationinnovation.server.misc;

import com.github.alexthe668.domesticationinnovation.DomesticationMod;
import com.github.alexthe668.domesticationinnovation.server.entity.DIVillagerRegistry;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pools.LegacySinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

public class PetshopStructurePoolElement extends LegacySinglePoolElement {

    public static final ResourceLocation CHEST = new ResourceLocation(DomesticationMod.MODID, "chests/petshop_chest");

    public static final Codec<PetshopStructurePoolElement> CODEC = RecordCodecBuilder.create((p_210357_) -> {
        return p_210357_.group(templateCodec(), processorsCodec(), projectionCodec()).apply(p_210357_, PetshopStructurePoolElement::new);
    });

    protected PetshopStructurePoolElement(Either<ResourceLocation, StructureTemplate> either, Holder<StructureProcessorList> p_210349_, StructureTemplatePool.Projection p_210350_) {
        super(either, p_210349_, p_210350_);
    }

    public PetshopStructurePoolElement(ResourceLocation resourceLocation, Holder<StructureProcessorList> processors) {
        super(Either.left(resourceLocation), processors, StructureTemplatePool.Projection.RIGID);
    }

    @Override
    public void handleDataMarker(LevelAccessor levelAccessor, StructureTemplate.StructureBlockInfo structureBlockInfo, BlockPos pos, Rotation rotation, Random random, BoundingBox box) {
        String contents = structureBlockInfo.nbt.getString("metadata");
        // System.out.println(structureBlockInfo.pos);
        System.out.println(contents);
        switch (contents) {
            case "petshop_water":
                BlockState state = Blocks.WATER.defaultBlockState();
                float f = random.nextFloat();
                if (f < 0.5F) {
                    state = Blocks.SEAGRASS.defaultBlockState();
                } else if (f < 0.75F) {
                    Block coralBlock;
                    switch (random.nextInt(5)) {
                        case 1:
                            coralBlock = Blocks.TUBE_CORAL;
                            break;
                        case 2:
                            coralBlock = Blocks.BRAIN_CORAL;
                            break;
                        case 3:
                            coralBlock = Blocks.BUBBLE_CORAL;
                            break;
                        case 4:
                            coralBlock = Blocks.FIRE_CORAL;
                            break;
                        default:
                            coralBlock = Blocks.HORN_CORAL;
                            break;
                    }
                    state = coralBlock.defaultBlockState().setValue(BaseCoralPlantTypeBlock.WATERLOGGED, true);
                }
                spawnAnimalsAt(levelAccessor, structureBlockInfo.pos, 2,  random, EntityType.TROPICAL_FISH);
                levelAccessor.setBlock(structureBlockInfo.pos, state, 2);
                break;
            case "petshop_chest":
                levelAccessor.setBlock(structureBlockInfo.pos, Blocks.AIR.defaultBlockState(), 2);
                RandomizableContainerBlockEntity.setLootTable(levelAccessor, random, structureBlockInfo.pos.below(), CHEST);
                break;
            case "petshop_cage_0"://wolf, rabbit or cat
                spawnAnimalsAt(levelAccessor, structureBlockInfo.pos, 1 + random.nextInt(1), random, EntityType.WOLF, EntityType.CAT, EntityType.RABBIT);
                levelAccessor.setBlock(structureBlockInfo.pos, Blocks.AIR.defaultBlockState(), 4);
                break;
            case "petshop_cage_1"://desert terrarium
                spawnAnimalsAt(levelAccessor, structureBlockInfo.pos, 1 + random.nextInt(2), random, EntityType.RABBIT);
                levelAccessor.setBlock(structureBlockInfo.pos, Blocks.AIR.defaultBlockState(), 2);
                break;
            case "petshop_cage_2"://ice terrarium
                spawnAnimalsAt(levelAccessor, structureBlockInfo.pos, 1 + random.nextInt(1), random, EntityType.FOX);
                levelAccessor.setBlock(structureBlockInfo.pos, Blocks.AIR.defaultBlockState(), 2);
                break;
            case "petshop_cage_3"://parrot
                spawnAnimalsAt(levelAccessor, structureBlockInfo.pos, 1, random, EntityType.PARROT);
                levelAccessor.setBlock(structureBlockInfo.pos, Blocks.AIR.defaultBlockState(), 2);
                break;
        }
    }

    public void spawnAnimalsAt(LevelAccessor accessor, BlockPos at, int count, Random random, EntityType... types) {
        if (types.length > 0 && count > 0 && accessor.getBlockState(at).getBlock() == Blocks.STRUCTURE_BLOCK && accessor instanceof ServerLevelAccessor serverLevel) {
            for (int i = 0; i < count; i++) {
                int index = types.length == 1 ? 0 : random.nextInt(types.length - 1);
                Entity entity = types[index].create(serverLevel.getLevel());
                entity.setPos(Vec3.atBottomCenterOf(at));
                entity.setYRot(random.nextInt(360) - 180);
                entity.setXRot(random.nextInt(360) - 180);
                if (entity instanceof Mob mob) {
                    mob.setPersistenceRequired();
                    mob.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.STRUCTURE, null, null);
                }
                serverLevel.addFreshEntityWithPassengers(entity);
            }
        }
    }

    @Override
    public boolean place(StructureManager p_210435_, WorldGenLevel p_210436_, StructureFeatureManager p_210437_, ChunkGenerator p_210438_, BlockPos p_210439_, BlockPos p_210440_, Rotation p_210441_, BoundingBox p_210442_, Random p_210443_, boolean p_210444_) {
        StructureTemplate structuretemplate = p_210435_.getOrCreate(template.left().get());
        StructurePlaceSettings structureplacesettings = this.getSettings(p_210441_, p_210442_, p_210444_);
        if (!structuretemplate.placeInWorld(p_210436_, p_210439_, p_210440_, structureplacesettings, p_210443_, 18)) {
            return false;
        } else {
            for (StructureTemplate.StructureBlockInfo structuretemplate$structureblockinfo : StructureTemplate.processBlockInfos(p_210436_, p_210439_, p_210440_, structureplacesettings, this.getDataMarkers(p_210435_, p_210439_, p_210441_, false), structuretemplate)) {
                this.handleDataMarker(p_210436_, structuretemplate$structureblockinfo, p_210439_, p_210441_, p_210443_, p_210442_);
            }
            return true;
        }
    }

    @Override
    protected StructurePlaceSettings getSettings(Rotation p_210421_, BoundingBox p_210422_, boolean p_210423_) {
        StructurePlaceSettings structureplacesettings = new StructurePlaceSettings();
        structureplacesettings.setBoundingBox(p_210422_);
        structureplacesettings.setRotation(p_210421_);
        structureplacesettings.setKnownShape(true);
        structureplacesettings.setIgnoreEntities(false);
        structureplacesettings.setFinalizeEntities(true);
        if (!p_210423_) {
            structureplacesettings.addProcessor(JigsawReplacementProcessor.INSTANCE);
        }
        this.processors.value().list().forEach(structureplacesettings::addProcessor);
        this.getProjection().getProcessors().forEach(structureplacesettings::addProcessor);
        return structureplacesettings;
    }

    public StructurePoolElementType<?> getType() {
        return DIVillagerRegistry.PETSHOP_TYPE;
    }

    public String toString() {
        return "PetShop[" + this.template + "]";
    }
}
