package weightedgpa.infinibiome.internal.generators.interchunks.struct;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.RandomWalkingGoal;
import net.minecraft.entity.monster.EvokerEntity;
import net.minecraft.entity.monster.IllusionerEntity;
import net.minecraft.entity.monster.PillagerEntity;
import net.minecraft.entity.monster.VindicatorEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Items;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.raid.Raid;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.generators.nonworldgen.SpawnPointBlacklist;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import weightedgpa.infinibiome.api.pos.InterChunkPos;
import weightedgpa.infinibiome.api.posdata.LandmassInfo;
import weightedgpa.infinibiome.api.posdata.PosDataKeys;
import weightedgpa.infinibiome.internal.misc.EntityHelper;
import weightedgpa.infinibiome.internal.misc.MCHelper;
import weightedgpa.infinibiome.internal.misc.MathHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static weightedgpa.infinibiome.internal.generators.utils.condition.ConditionHelper.onlyIfNotNear;
import static weightedgpa.infinibiome.internal.generators.utils.condition.ConditionHelper.onlyInLandMass;

public final class IllagerOutpostGen extends StructGenBase implements SpawnPointBlacklist {
    private static final List<SpawnEntity> entitiesOutsideStruct = new ArrayList<>();
    private static final List<SpawnEntity> entitiesInsideStruct = new ArrayList<>();

    private static final double INSIDE_STRUCT_PROBABILITY = 0.05;
    private static final double OUTSIDE_STRUCT_PROBABILITY = 0.0005;

    static {
        entitiesOutsideStruct.addAll(
            Collections.nCopies(
                10,
                IllagerOutpostGen::getPillager
            )
        );
        entitiesOutsideStruct.addAll(
            Collections.nCopies(
                5,
                IllagerOutpostGen::getVindicator
            )
        );
        entitiesOutsideStruct.addAll(
            Collections.nCopies(
                2,
                IllagerOutpostGen::getIllusioner
            )
        );

        entitiesInsideStruct.addAll(
            Collections.nCopies(
                10,
                IllagerOutpostGen::getVindicator
            )
        );
        entitiesInsideStruct.addAll(
            Collections.nCopies(
                5,
                IllagerOutpostGen::getPillager
            )
        );
        entitiesInsideStruct.addAll(
            Collections.nCopies(
                2,
                IllagerOutpostGen::getEvoker
            )
        );
    }

    public IllagerOutpostGen(DependencyInjector di) {
        super(di, Infinibiome.MOD_ID + ":outpost");

        baseConfig = initConfig()
            .withStruct(
                Feature.PILLAGER_OUTPOST
            )
            .withChance(
                Config.class, 2
            )
            .addExtraConditions(
                onlyInLandMass(di, LandmassInfo::isLand),
                StructHelper.alwaysAboveWater(
                    di,
                    20
                ),
                onlyIfNotNear(
                    di,
                    10,
                    (v, p) -> v.baseConfig.conditions.canBeHere(p),
                    VillageGen.class
                )
            );
    }

    @Override
    public void postGenerate(InterChunkPos pos, IWorld world) {
        StructHelper.placeDirtUnderStruct(pos, world, posData);

        spawnIllagers(pos, world);

        spawnLeaderPillager(pos, world);
    }

    private void spawnIllagers(InterChunkPos interChunkPos, IWorld world){
        Random random = randomGen.getRandom(interChunkPos.getX(), interChunkPos.getZ());

        interChunkPos.forEachCenterPos(
            pos -> {
                List<Integer> validHeights = getValidHeights(pos, world);

                boolean insideStruct = validHeights.size() > 1;

                double probability = getProbability(insideStruct);

                for (int height: validHeights){
                    if (!MathHelper.randomBool(probability, random)) return;

                    spawnIllager(
                        pos.to3D(height),
                        world,
                        random,
                        insideStruct
                    );
                }
            }
        );
    }

    private double getProbability(boolean inSideStruct){
        if (inSideStruct){
            return INSIDE_STRUCT_PROBABILITY;
        }
        return OUTSIDE_STRUCT_PROBABILITY;
    }

    private List<Integer> getValidHeights(BlockPos2D pos, IWorld world){
        List<Integer> result = new ArrayList<>();

        int minHeight = (int)posData.get(PosDataKeys.MAPPED_HEIGHT, pos);

        for (int y = minHeight; y < 255; y++){
            BlockState floor = world.getBlockState(pos.to3D(y - 1));
            BlockState air1 = world.getBlockState(pos.to3D(y));
            BlockState air2 = world.getBlockState(pos.to3D(y + 1));

            if (BlockTags.LEAVES.contains(floor.getBlock())) continue;

            if (!MCHelper.isSolid(floor)) continue;

            if (!MCHelper.isMostlyAir(air1)) continue;

            if (!MCHelper.isMostlyAir(air2)) continue;

            result.add(y);
        }
        return result;
    }

    private void spawnIllager(BlockPos pos, IWorld world, Random random, boolean insideStruct){
        List<SpawnEntity> list;

        if (insideStruct){
            list = entitiesInsideStruct;
        }
        else {
            list = entitiesOutsideStruct;
        }

        Entity entity = list.get(random.nextInt(list.size())).get(world);

        if (insideStruct){
            removeWanderingAI((MobEntity) entity);
        }

        MCHelper.spawnEntity(
            entity,
            pos,
            world
        );
    }

    private void spawnLeaderPillager(InterChunkPos interChunkPos, IWorld world){
        interChunkPos.forEachCenterPos(
            pos -> {
                int lowestY = (int)posData.get(PosDataKeys.MAPPED_HEIGHT, pos);

                int highestY = MCHelper.getHighestNonAirY(pos, world);

                for (int y = lowestY; y < highestY; y++){
                    Block block = world.getBlockState(pos.to3D(y)).getBlock();

                    if (!block.equals(Blocks.CHEST)) continue;

                    MCHelper.spawnEntity(
                        getPillagerLeader(world),
                        pos.to3D(y),
                        world
                    );

                    return;
                }
            }
        );

    }

    private interface SpawnEntity{
        Entity get(IWorld world);
    }

    private static PillagerEntity getPillagerLeader(IWorld world){
        PillagerEntity pillager = getPillager(world);

        pillager.setItemStackToSlot(
            EquipmentSlotType.HEAD,
            Raid.createIllagerBanner()
        );

        removeWanderingAI(pillager);

        return pillager;
    }

    private static PillagerEntity getPillager(IWorld world){
        PillagerEntity pillager = new PillagerEntity(EntityType.PILLAGER, world.getWorld());

        pillager.enablePersistence();

        pillager.setItemStackToSlot(
            EquipmentSlotType.MAINHAND,
            Items.CROSSBOW.getDefaultInstance()
        );

        return pillager;
    }

    private static Entity getVindicator(IWorld world){
        VindicatorEntity vindicator = new VindicatorEntity(EntityType.VINDICATOR, world.getWorld());

        vindicator.enablePersistence();

        vindicator.setItemStackToSlot(
            EquipmentSlotType.MAINHAND,
            Items.IRON_AXE.getDefaultInstance()
        );

        return vindicator;
    }

    private static Entity getEvoker(IWorld world){
        EvokerEntity evoker = new EvokerEntity(EntityType.EVOKER, world.getWorld());

        evoker.enablePersistence();

        return evoker;
    }

    private static IllusionerEntity getIllusioner(IWorld world){
        IllusionerEntity illusioner = new IllusionerEntity(EntityType.ILLUSIONER, world.getWorld());

        illusioner.enablePersistence();

        return illusioner;
    }

    private static void removeWanderingAI(MobEntity entity){
        EntityHelper.removeGoals(
            entity.goalSelector,
            g -> g instanceof RandomWalkingGoal
        );
    }

    @Override
    public boolean canSpawnHere(BlockPos2D pos) {
        for (BlockPos2D ignored : getAllLocations().getBoundedPoints(pos, 32)){
            return false;
        }
        return true;
    }

    public static final class Config extends StructConfigBase{
        public Config(DependencyInjector di){
            super(di);
        }

        @Override
        String name() {
            return "illager_outpost_rate";
        }

        @Override
        double defaultRate() {
            return 0.001;
        }

    }
}
