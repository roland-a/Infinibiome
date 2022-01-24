package weightedgpa.infinibiome.internal.generators.posdata;

import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.generators.ClimateConfig;
import weightedgpa.infinibiome.api.generators.PosDataTimings;
import weightedgpa.infinibiome.api.generators.Seed;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import weightedgpa.infinibiome.api.posdata.ClimateValue;
import weightedgpa.infinibiome.api.posdata.PosDataKeys;

import weightedgpa.infinibiome.api.posdata.PosDataTable;
import weightedgpa.infinibiome.internal.floatfunc.FloatFunc;
import weightedgpa.infinibiome.internal.floatfunc.generators.SimplexNoise;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;
import weightedgpa.infinibiome.internal.floatfunc.util.PercentileTable;
import weightedgpa.infinibiome.internal.misc.Helper;
import weightedgpa.infinibiome.internal.misc.MathHelper;


public final class ClimateGen extends DataGeneratorBase {
    private static final double TEMPERATURE_CHANGE_PER_Y = -0.001f;

    private final FloatFunc<BlockPos2D> humidityFunc;
    private final FloatFunc<BlockPos2D> temperatureFunc;

    private static final PercentileTable PERCENTILE_TABLE = PercentileTable.deserialize(
        Helper.getResource("/climatePercentileTable")
    );

    public ClimateGen(DependencyInjector di) {
        super(
            di,
            Infinibiome.MOD_ID + ":climate",
            PosDataTimings.CLIMATE
        );

        ClimateConfig config = di.get(ClimateConfig.class);

        this.temperatureFunc = initClimateNoise(
            seed.newSeed("temperature"),
            config.scale
        )._setDebuggable(
            "climate",
            "temperature",
            p -> p
        );

        this.humidityFunc = initClimateNoise(
            seed.newSeed("humidity"),
            config.scale
        )._setDebuggable(
            "climate",
            "humidity",
            p -> p
        );
    }

    private static FloatFunc<BlockPos2D> initClimateNoise(Seed seed, double scale){
        seed = seed.newSeed("base");

        SimplexNoise<BlockPos2D> base = new SimplexNoise<>(seed, scale, BlockPos2D.INFO);

        return new FloatFunc<BlockPos2D>() {
            @Override
            public double getOutput(BlockPos2D input) {
                return MathHelper.fractal(
                    i -> base.getOutput(input, i),
                    base.getOutputInterval(),
                    4,
                    0.5,
                    0.5
                );
            }

            @Override
            public Interval getOutputInterval() {
                return base.getOutputInterval();
            }
        }
        .toUniform(PERCENTILE_TABLE);
    }

    @Override
    public void generateData(PosDataTable data) {
        double temperature = temperatureFunc.getOutput(data.getPos());

        data.set(
            PosDataKeys.TEMPERATURE,
            new ClimateValue(
                temperature,
                TEMPERATURE_CHANGE_PER_Y
            )
        );

        double humidity = humidityFunc.getOutput(data.getPos());

        data.set(
            PosDataKeys.HUMIDITY,
            new ClimateValue(
                humidity,
                0
            )
        );
    }
}
