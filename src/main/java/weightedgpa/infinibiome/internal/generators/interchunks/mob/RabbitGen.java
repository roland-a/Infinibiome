package weightedgpa.infinibiome.internal.generators.interchunks.mob;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.RabbitEntity;
import net.minecraft.world.IWorld;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.internal.misc.MCHelper;
import weightedgpa.infinibiome.api.posdata.PosDataHelper;
import weightedgpa.infinibiome.api.pos.InterChunkPos;
import net.minecraft.util.math.BlockPos;

import java.util.Random;

import static weightedgpa.infinibiome.internal.generators.utils.condition.ConditionHelper.*;

//spawns almost everywhere, including deserts
public final class RabbitGen extends MobGenBase {
    public RabbitGen(DependencyInjector di) {
        super(di, Infinibiome.MOD_ID + ":rabbit");

        config = initConfig()
            .getEntity(
                this::getEntity
            )
            .setGroupCount(1)
            .setBabyChance(MobHelper.COMMON_BABY_RATE)
            .alwaysAboveWater()
            .neverInMushroomIsland()
            .anyNonHighSlope()
            .onlyOnNonBeachLand()
            .anyTemperatureIncludingFreezing()
            .anyHumidityIncludingDesert()
            .setChancePerChunk(
                1/20d
            )
            .addExtraConditions(
                onlyIfNotNear(
                    di,
                    2,
                    MobGenBase::canSpawnAtInterChunk,
                    WolfGen.class,
                    FoxGen.class,
                    OcelotGen.class,
                    PolarBearGen.class
                )
            );
    }

    private AnimalEntity getEntity(BlockPos mobPos, InterChunkPos interChunkPos, IWorld world) {
        //System.out.println("new rabbit{");
        RabbitEntity result = new RabbitEntity(EntityType.RABBIT, world.getWorld());
        //System.out.println("}");

        //System.out.println("type{");
        int variant = getRabbitType(mobPos, world);
        //System.out.println("}");

        result.setRabbitType(variant);

        return result;
    }

    @SuppressWarnings("toobroadscope")
    private int getRabbitType(BlockPos rabbitPos, IWorld world) {
        final int brown = 0;
        final int white = 1;
        final int black = 2;
        final int blackAndWhite = 3;
        final int gold = 4;
        final int saltAndPepper = 5;
        final int killer = 99;

        Random random = randomGen.getRandom(rabbitPos.getX(), rabbitPos.getZ());

        if (random.nextInt(100) == 0) {
            return killer;
        }

        double temperature = PosDataHelper.getTemperature(rabbitPos, posData);

        if (PosDataHelper.FREEZE_INTERVAL.contains(temperature)) {
            return white;
        }

        //todo fix bug where getting block can cause deadlocking
        /*
        System.out.println("getBlock{");
        BlockState groundBlock = world.getBlockState(rabbitPos.down());
        System.out.println("}");
         */

        double humidity = PosDataHelper.getHumidity(MCHelper.to2D(rabbitPos), posData);

        if (PosDataHelper.DRY_INTERVAL.contains(humidity)){
            return gold;
        }

        /*
        if (groundBlock.equals(Blocks.SAND.getDefaultState()) || groundBlock.equals(Blocks.SANDSTONE.getDefaultState())) {
            return gold;
        }

         */

        int[] options = {brown, black, blackAndWhite, saltAndPepper};
        int randomIndex = random.nextInt(options.length);
        return options[randomIndex];
    }
}
