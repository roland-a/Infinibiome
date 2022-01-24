package weightedgpa.infinibiome.internal.generators.interchunks.struct;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.world.gen.feature.BlockStateFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.generators.nonworldgen.Locatable;
import weightedgpa.infinibiome.api.posdata.LandmassInfo;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;
import weightedgpa.infinibiome.internal.generators.interchunks.SmallObjectGenBase;
import weightedgpa.infinibiome.internal.generators.utils.GenHelper;
import weightedgpa.infinibiome.internal.misc.MCHelper;

import static weightedgpa.infinibiome.internal.generators.utils.condition.ConditionHelper.*;

public final class IceburgGen extends SmallObjectGenBase implements Locatable.HasPointsProvider {
    private final Type type;

    public IceburgGen(Type type, DependencyInjector di) {
        super(di, Infinibiome.MOD_ID + ":iceburg" + type.name());

        this.type = type;

        BlockStateFeatureConfig featureConfig = new BlockStateFeatureConfig(type.block);

        this.config = initConfig()
            .setWithFeature(
                Feature.ICEBERG,
                featureConfig
            )
            .setCount(1)
            .setAttemptsPerCount(4)
            .customHeightFunc(
                (pos, world, random) -> MCHelper.getHighestTerrainHeight(pos, world)
            )
            .setChancePerChunk(type.rate)
            .addExtraConditions(
                onlyInTemperature(di, GenHelper.LOWER_FREEZE_INTERVAL),
                onlyInHeight(di, new Interval(0, MCHelper.WATER_HEIGHT - 20)),
                onlyInLandMass(di, LandmassInfo::isOcean)
            );
    }

    @Override
    public String toString() {
        return "Iceburg{" +
            "type=" + type +
            '}';
    }

    public enum Type{
        ICE(Blocks.ICE.getDefaultState(), 1/16d),
        BLUE_ICE(Blocks.BLUE_ICE.getDefaultState(), 1/200d),;

        private final BlockState block;
        private final double rate;

        Type(BlockState block, double rate) {
            this.block = block;
            this.rate = rate;
        }
    }

}
