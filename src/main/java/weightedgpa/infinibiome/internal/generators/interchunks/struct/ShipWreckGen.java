package weightedgpa.infinibiome.internal.generators.interchunks.struct;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.structure.ShipwreckConfig;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import weightedgpa.infinibiome.api.posdata.PosDataKeys;
import weightedgpa.infinibiome.internal.misc.MCHelper;

import static weightedgpa.infinibiome.internal.generators.utils.condition.ConditionHelper.*;

public final class ShipWreckGen extends StructGenBase {
    public ShipWreckGen(DependencyInjector di) {
        super(di, Infinibiome.MOD_ID + ":shipwreck");

        baseConfig = initConfig()
            .withStructAndFunc(
                Feature.SHIPWRECK,
                this::getShipWreckConfig
            )
            .withChance(
                Config.class, 1
            )
            .addExtraConditions(
                onlyInLandMass(di, 0, l -> !l.isLand())
            );
    }

    private ShipwreckConfig getShipWreckConfig(ChunkPos chunkPos) {
        BlockPos2D pos = MCHelper.lowestPos(chunkPos);

        double height = posData.get(PosDataKeys.MAPPED_HEIGHT, pos);

        if (height > MCHelper.WATER_HEIGHT - 5) {
            return new ShipwreckConfig(true);
        }
        return new ShipwreckConfig(false);
    }


    public static final class Config extends StructConfigBase{
        public Config(DependencyInjector di){
            super(di);
        }

        @Override
        String name() {
            return "ship_wreck_rate";
        }

        @Override
        double defaultRate() {
            return 0.001;
        }
    }
}
